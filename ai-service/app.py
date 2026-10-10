import os,time,math,threading
from collections import defaultdict
from typing import Optional
import cv2,numpy as np,requests
from fastapi import FastAPI,UploadFile,File,Form
from fastapi.middleware.cors import CORSMiddleware
try:
    from ultralytics import YOLO, YOLOWorld
except Exception:
    from ultralytics import YOLO
    YOLOWorld=None

app=FastAPI(title="Barber Garage AI",version="2.0")
app.add_middleware(CORSMiddleware,allow_origins=["*"],allow_credentials=False,allow_methods=["*"],allow_headers=["*"])
BACKEND=os.getenv("BACKEND_URL","http://localhost:8093").rstrip("/")
TOKEN=os.getenv("AI_SERVICE_TOKEN","dev-ai-token-change-me")
CONF=float(os.getenv("AI_CONF","0.25")); MIN_SERVICE=int(os.getenv("MIN_SERVICE_SECONDS","120")); IDLE=int(os.getenv("SERVICE_IDLE_SECONDS","20"))
ROI=tuple(float(x) for x in os.getenv("CHAIR_ROI","0.22,0.25,0.78,0.95").split(","))
CUSTOM=os.getenv("CUSTOM_TOOLS_MODEL","models/barber_tools.pt"); POSE_PATH=os.getenv("POSE_MODEL","yolov8n-pose.pt"); WORLD_PATH=os.getenv("WORLD_MODEL","yolov8s-worldv2.pt")
lock=threading.Lock(); custom_model=pose_model=world_model=None
TOOL_PROMPTS=["hair clipper","barber clipper","hair trimmer","scissors","hairdressing scissors","comb","hair dryer","blow dryer","razor"]
ALIASES={"hair clipper":"maquina_cortar","barber clipper":"maquina_cortar","hair trimmer":"maquina_cortar","clipper":"maquina_cortar","scissors":"tijeras","hairdressing scissors":"tijeras","comb":"peine","hair dryer":"secadora","blow dryer":"secadora","razor":"navaja","maquina_cortar":"maquina_cortar","tijeras":"tijeras","peine":"peine","secadora":"secadora","navaja":"navaja"}
DISPLAY={"maquina_cortar":"Máquina de cortar","tijeras":"Tijeras","peine":"Peine","secadora":"Secadora","navaja":"Navaja"}
sessions=defaultdict(lambda:{"seated_since":None,"last_seen":None,"qualified":False,"last_tools":{},"last_event":{}})
face_cascade=cv2.CascadeClassifier(cv2.data.haarcascades+"haarcascade_frontalface_default.xml")

def get_models():
 global custom_model,pose_model,world_model
 with lock:
  if pose_model is None:
   try: pose_model=YOLO(POSE_PATH)
   except Exception as e: print("POSE unavailable",e)
  if custom_model is None and os.path.exists(CUSTOM):
   try: custom_model=YOLO(CUSTOM)
   except Exception as e: print("CUSTOM unavailable",e)
  if world_model is None and YOLOWorld is not None:
   try:
    world_model=YOLOWorld(WORLD_PATH); world_model.set_classes(TOOL_PROMPTS)
   except Exception as e: print("WORLD unavailable",e)
 return custom_model,pose_model,world_model

def decode(data):
 arr=np.frombuffer(data,np.uint8); img=cv2.imdecode(arr,cv2.IMREAD_COLOR)
 if img is None: raise ValueError("Imagen inválida")
 return img

def face_quality(img):
 gray=cv2.cvtColor(img,cv2.COLOR_BGR2GRAY); faces=face_cascade.detectMultiScale(gray,1.1,5,minSize=(70,70))
 if len(faces)==0:return {"faceDetected":False,"faceBox":None,"faceQuality":0.0}
 x,y,w,h=max(faces,key=lambda b:b[2]*b[3]); roi=gray[y:y+h,x:x+w]; brightness=float(np.mean(roi)); area=(w*h)/(img.shape[0]*img.shape[1]); center=np.array([x+w/2,y+h/2]); ideal=np.array([img.shape[1]/2,img.shape[0]/2]); centered=max(0,1-np.linalg.norm(center-ideal)/np.linalg.norm(ideal)); q=min(1.0,.35*min(area/.08,1)+.35*centered+.3*(1-abs(brightness-130)/130));return {"faceDetected":True,"faceBox":[int(x),int(y),int(x+w),int(y+h)],"faceQuality":round(max(0,q),3)}

def box_iou_roi(box,w,h):
 x1,y1,x2,y2=box; rx1,ry1,rx2,ry2=ROI[0]*w,ROI[1]*h,ROI[2]*w,ROI[3]*h; ix=max(0,min(x2,rx2)-max(x1,rx1));iy=max(0,min(y2,ry2)-max(y1,ry1));inter=ix*iy;area=max(1,(x2-x1)*(y2-y1));return inter/area

def pose_info(img):
 _,pose,_=get_models(); out={"clientSeated":False,"people":0,"faceKeypoints":[],"personBoxes":[],"barberBox":None}
 if pose is None:return out
 try:
  r=pose.predict(img,conf=.35,verbose=False)[0]; out["people"]=0 if r.boxes is None else len(r.boxes)
  if r.boxes is not None:
   h,w=img.shape[:2]
   boxes=r.boxes.xyxy.cpu().numpy()
   out["personBoxes"]=[[int(v) for v in b] for b in boxes]
   barber_candidates=[]
   for b in boxes:
    overlap=box_iou_roi(b,w,h)
    if overlap>.30 and (b[3]-b[1])>(b[2]-b[0])*1.05: out["clientSeated"]=True
    else: barber_candidates.append(b)
   if barber_candidates:
    bb=max(barber_candidates,key=lambda b:(b[2]-b[0])*(b[3]-b[1])); out["barberBox"]=[int(v) for v in bb]
  if r.keypoints is not None and len(r.keypoints.xy):
   pts=r.keypoints.xy[0].cpu().numpy();out["faceKeypoints"]=[[round(float(x),1),round(float(y),1)] for x,y in pts[:5]]
 except Exception as e: print("pose",e)
 return out

def canonical(label):
 k=str(label).lower().strip().replace("-"," ");return ALIASES.get(k,ALIASES.get(k.replace(" ","_")))

def detect_green_comb(img):
 # Fallback visual para el peine verde usado en las pruebas de Barber Garage.
 # No reemplaza al modelo entrenado, pero permite una evidencia estable mientras
 # se recopilan y etiquetan imágenes para barber_tools.pt.
 hsv=cv2.cvtColor(img,cv2.COLOR_BGR2HSV)
 mask=cv2.inRange(hsv,np.array([35,55,45],dtype=np.uint8),np.array([95,255,255],dtype=np.uint8))
 kernel=np.ones((5,5),np.uint8)
 mask=cv2.morphologyEx(mask,cv2.MORPH_OPEN,kernel)
 mask=cv2.morphologyEx(mask,cv2.MORPH_CLOSE,kernel,iterations=2)
 contours,_=cv2.findContours(mask,cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_SIMPLE)
 if not contours:return None
 h,w=img.shape[:2]; image_area=float(w*h)
 best=None
 for c in contours:
  area=cv2.contourArea(c)
  if area < image_area*.006: continue
  x,y,bw,bh=cv2.boundingRect(c)
  if bw<45 or bh<18: continue
  ratio=max(bw/max(bh,1),bh/max(bw,1))
  # Un peine de demostración suele ser alargado. Mantener umbral flexible
  # porque los dientes pueden fragmentar el contorno.
  score=min(0.99,0.55 + min(area/(image_area*.10),1)*0.32 + min(ratio/5,1)*0.12)
  item={"key":"peine","label":DISPLAY["peine"],"confidence":round(float(score),3),"box":[int(x),int(y),int(x+bw),int(y+bh)],"source":"opencv-green-comb"}
  if best is None or item["confidence"]>best["confidence"]: best=item
 return best

def detect_tools(img):
 custom,_,world=get_models(); found={}
 def consume(result,names,source):
  if result.boxes is None:return
  for b in result.boxes:
   conf=float(b.conf[0]); cls=int(b.cls[0]); label=names[cls] if isinstance(names,(list,tuple)) else names.get(cls,str(cls));tool=canonical(label)
   if tool and (tool not in found or conf>found[tool]["confidence"]):
    xy=[int(v) for v in b.xyxy[0].tolist()]; found[tool]={"key":tool,"label":DISPLAY.get(tool,tool),"confidence":round(conf,3),"box":xy,"source":source}
 try:
  if custom is not None:
   r=custom.predict(img,conf=CONF,verbose=False)[0];consume(r,r.names,"custom")
  if world is not None:
   r=world.predict(img,conf=max(.15,CONF-.08),verbose=False)[0];consume(r,r.names,"world")
 except Exception as e: print("tools",e)
 # Fallback específico para la evidencia actual del peine verde.
 # Solo se agrega si los modelos no detectaron ya un peine.
 if "peine" not in found:
  green_comb=detect_green_comb(img)
  if green_comb is not None: found["peine"]=green_comb
 return list(found.values())

def color_match(img,people_box,expected):
 if not expected or people_box is None:return {"uniformColor":None,"uniformMatch":None}
 x1,y1,x2,y2=[int(v) for v in people_box]; h=max(1,y2-y1); torso=img[y1+int(.18*h):y1+int(.58*h),x1:x2]
 if torso.size==0:return {"uniformColor":None,"uniformMatch":None}
 bgr=np.median(torso.reshape(-1,3),axis=0); rgb=bgr[::-1]; actual='#'+''.join(f'{int(v):02x}' for v in rgb)
 try:
  exp=np.array([int(expected[i:i+2],16) for i in (1,3,5)]);d=float(np.linalg.norm(rgb-exp));match=d<95
 except:match=None
 return {"uniformColor":actual,"uniformMatch":match}

def post_event(barber_id,station,event_type,tool=None,action=None,confidence=None,duration=None,detail=None,snapshot=None):
 body={"barberId":barber_id,"station":station,"eventType":event_type,"tool":tool,"action":action,"confidence":confidence,"durationSeconds":duration,"detail":detail,"snapshotDataUrl":snapshot}
 try: requests.post(BACKEND+"/api/ai/events",json=body,headers={"X-AI-Token":TOKEN},timeout=2.5)
 except Exception: pass

def maybe_emit(state,key,cooldown,*args,**kwargs):
 now=time.time(); last=state["last_event"].get(key,0)
 if now-last>=cooldown: state["last_event"][key]=now; post_event(*args,**kwargs)

@app.get('/health')
def health():
 return {
  "ok":True,
  "customModel":os.path.exists(CUSTOM),
  "customModelPath":CUSTOM,
  "poseModel":POSE_PATH,
  "worldModel":WORLD_PATH,
  "minServiceSeconds":MIN_SERVICE,
  "toolPrompts":TOOL_PROMPTS
 }

@app.post('/face-presence')
async def face_presence(file:UploadFile=File(...)):
 img=decode(await file.read());return face_quality(img)

@app.post('/analyze-frame')
async def analyze_frame(file:UploadFile=File(...),barber_id:int=Form(...),station:str=Form('Estacion_1'),expected_uniform_color:Optional[str]=Form(None)):
 img=decode(await file.read()); now=time.time(); p=pose_info(img); face=face_quality(img); tools=detect_tools(img); state=sessions[station]
 clothing=color_match(img,p.get("barberBox"),expected_uniform_color)
 if p['clientSeated']:
  if state['seated_since'] is None: state['seated_since']=now;maybe_emit(state,'seated',20,barber_id,station,'CLIENTE_SENTADO',detail='Cliente detectado dentro de la zona de silla')
  state['last_seen']=now
 else:
  if state['last_seen'] and now-state['last_seen']>IDLE:
   if state['seated_since'] is not None: maybe_emit(state,'finished',5,barber_id,station,'SERVICIO_FINALIZADO',duration=int(now-state['seated_since']),detail='Cliente dejó la zona de atención')
   state.update({"seated_since":None,"last_seen":None,"qualified":False,"last_tools":{},"last_event":{}})
 for t in tools:
  state['last_tools'][t['key']]=now;maybe_emit(state,'tool:'+t['key'],8,barber_id,station,'HERRAMIENTA_DETECTADA',tool=t['key'],confidence=t['confidence'],detail=t['label'])
 elapsed=int(now-state['seated_since']) if state['seated_since'] else 0
 active_tool=any(now-v<5 for v in state['last_tools'].values())
 if state['seated_since'] and active_tool and elapsed>=MIN_SERVICE and not state['qualified']:
  state['qualified']=True;post_event(barber_id,station,'CORTE_CONTABILIZADO',action='Servicio mínimo alcanzado',duration=elapsed,detail=f'Servicio contabilizado al superar {MIN_SERVICE} segundos con actividad de herramientas')
 action=None
 keys={x['key'] for x in tools}
 if 'peine' in keys:action='PEINADO / ACOMODO'
 elif 'maquina_cortar' in keys:action='CORTE CON MÁQUINA'
 elif 'tijeras' in keys:action='CORTE CON TIJERAS'
 elif 'secadora' in keys:action='SECADO / MOLDEADO'
 if action:maybe_emit(state,'action:'+action,10,barber_id,station,'MOVIMIENTO_DETECTADO',action=action,duration=elapsed)
 return {**p,**face,**clothing,"tools":tools,"action":action,"serviceSeconds":elapsed,"qualifiedCut":state['qualified'],"minimumSeconds":MIN_SERVICE,"station":station,"privacyMode":"No facial identity; face presence only","modelDiagnostics":{"customModel":custom_model is not None,"poseModel":pose_model is not None,"worldModel":world_model is not None}}

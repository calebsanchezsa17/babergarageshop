import React,{useEffect,useRef,useState}from'react';
import{Camera,CameraOff,ScanFace,Scissors,TimerReset,UserRoundCheck,Wifi,WifiOff}from'lucide-react';
import{AI}from'../services/api';

const FRAME_W=640,FRAME_H=360;
const COLORS={tool:'#77ff92',person:'#4fb6ff',barber:'#ffb454',face:'#ffffff'};

export default function CameraMonitor({barberId,station='Estacion_1',expectedUniformColor,onDetection}){
 const video=useRef(),canvas=useRef(),streamRef=useRef();
 const[active,setActive]=useState(false),[status,setStatus]=useState(null),[err,setErr]=useState(''),[aiOnline,setAiOnline]=useState(null);
 const busy=useRef(false);

 async function checkAi(){
  try{const r=await fetch(AI+'/health');setAiOnline(r.ok);if(!r.ok)setErr('El servicio de IA respondió con error '+r.status)}
  catch(e){setAiOnline(false);setErr('No se puede conectar con la IA en '+AI+'. Verifica Uvicorn en el puerto 8000.')}
 }

 async function start(){
  await checkAi();
  try{
   const s=await navigator.mediaDevices.getUserMedia({video:{width:{ideal:1280},height:{ideal:720},facingMode:'user'},audio:false});
   streamRef.current=s;video.current.srcObject=s;await video.current.play();setActive(true);setErr('');
  }catch(e){setErr('No se pudo acceder a la cámara. Revisa permisos del navegador.')}
 }
 function stop(){streamRef.current?.getTracks().forEach(t=>t.stop());streamRef.current=null;setActive(false);setStatus(null)}
 useEffect(()=>()=>stop(),[]);

 useEffect(()=>{
  if(!active)return;
  const id=setInterval(async()=>{
   if(busy.current||!video.current?.videoWidth)return;
   busy.current=true;
   try{
    const c=canvas.current;c.width=FRAME_W;c.height=FRAME_H;
    c.getContext('2d').drawImage(video.current,0,0,c.width,c.height);
    const blob=await new Promise(r=>c.toBlob(r,'image/jpeg',.82));
    const fd=new FormData();fd.append('file',blob,'frame.jpg');fd.append('barber_id',String(barberId));fd.append('station',station);
    if(expectedUniformColor)fd.append('expected_uniform_color',expectedUniformColor);
    const r=await fetch(AI+'/analyze-frame',{method:'POST',body:fd});
    if(!r.ok){const t=await r.text();throw new Error(`IA ${r.status}: ${t||'respuesta inválida'}`)}
    const d=await r.json();setStatus(d);setAiOnline(true);setErr('');onDetection?.(d);
   }catch(e){setAiOnline(false);setErr(e?.message||'No se pudo analizar el fotograma')}
   finally{busy.current=false}
  },800);
  return()=>clearInterval(id)
 },[active,barberId,station,expectedUniformColor,onDetection]);

 const tools=status?.tools||[];
 const boxes=status?.personBoxes||[];
 return <div className="camera-card">
  <div className="camera-stage">
   <video ref={video} muted playsInline className={active?'':'hidden'}/>
   {!active&&<div className="camera-placeholder"><Camera size={42}/><b>Cámara de estación</b><span>Activa la cámara para iniciar el análisis</span></div>}
   <canvas ref={canvas} className="hidden"/>
   {active&&<svg className="detection-layer" viewBox={`0 0 ${FRAME_W} ${FRAME_H}`} preserveAspectRatio="none">
    <rect x="141" y="90" width="358" height="252" className="roi-box"/>
    {boxes.map((b,i)=><rect key={'p'+i} x={b[0]} y={b[1]} width={b[2]-b[0]} height={b[3]-b[1]} fill="none" stroke={COLORS.person} strokeWidth="2"/>) }
    {status?.barberBox&&<rect x={status.barberBox[0]} y={status.barberBox[1]} width={status.barberBox[2]-status.barberBox[0]} height={status.barberBox[3]-status.barberBox[1]} fill="none" stroke={COLORS.barber} strokeWidth="3"/>}
    {status?.faceBox&&<rect x={status.faceBox[0]} y={status.faceBox[1]} width={status.faceBox[2]-status.faceBox[0]} height={status.faceBox[3]-status.faceBox[1]} fill="none" stroke={COLORS.face} strokeWidth="2"/>}
    {tools.map((t,i)=><g key={t.key+i}><rect x={t.box[0]} y={t.box[1]} width={t.box[2]-t.box[0]} height={t.box[3]-t.box[1]} fill="none" stroke={COLORS.tool} strokeWidth="3"/><rect x={t.box[0]} y={Math.max(0,t.box[1]-22)} width={Math.max(100,t.label.length*9)} height="22" fill="#111d"/><text x={t.box[0]+5} y={Math.max(15,t.box[1]-6)} fill={COLORS.tool} fontSize="14" fontWeight="700">{t.label} {Math.round(t.confidence*100)}%</text></g>)}
   </svg>}
   <div className="camera-overlay">
    <span className={status?.clientSeated?'pill green':'pill'}><UserRoundCheck size={14}/>{status?.clientSeated?'Cliente sentado':'Esperando cliente'}</span>
    {status?.faceDetected&&<span className="pill blue"><ScanFace size={14}/>Rostro visible</span>}
    <span className={aiOnline?'pill green':'pill'}>{aiOnline?<Wifi size={14}/>:<WifiOff size={14}/>}IA {aiOnline?'conectada':'sin conexión'}</span>
   </div>
  </div>
  <div className="camera-controls">
   {active?<button className="btn danger" onClick={stop}><CameraOff size={16}/>Detener cámara</button>:<button className="btn" onClick={start}><Camera size={16}/>Activar cámara</button>}
   <div className="live-facts">
    <span><Scissors size={15}/>Herramientas: {tools.length?tools.map(t=>`${t.label} ${Math.round(t.confidence*100)}%`).join(', '):'—'}</span>
    <span><TimerReset size={15}/>Tiempo: {status?.serviceSeconds??0}s · {status?.qualifiedCut?'Contabilizado':'En observación'}</span>
   </div>
  </div>
  {status?.modelDiagnostics&&<div className="ai-debug">Modelos: Pose {status.modelDiagnostics.poseModel?'✓':'✗'} · YOLO-World {status.modelDiagnostics.worldModel?'✓':'✗'} · Personalizado {status.modelDiagnostics.customModel?'✓':'✗'}</div>}
  {err&&<div className="alert error">{err}</div>}
 </div>
}

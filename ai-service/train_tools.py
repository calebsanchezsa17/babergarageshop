"""Entrena un modelo personalizado con clases de herramientas de barbería.
Estructura esperada del dataset: datasets/barber_tools/{images,labels}/{train,val}
"""
from ultralytics import YOLO

model=YOLO("yolov8n.pt")
model.train(data="barber_tools.yaml",epochs=80,imgsz=640,batch=16,project="runs",name="barber_tools")
print("Modelo final: runs/barber_tools/weights/best.pt")

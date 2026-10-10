# Barber Garage Pro

Versión profesional del proyecto Barber Garage con:

- **Frontend:** React + Vite.
- **Backend:** Java 17 + Spring Boot + Spring Security + JWT + BCrypt.
- **Base de datos:** MySQL.
- **IA:** Python + FastAPI + OpenCV + Ultralytics YOLO / YOLO-Pose / YOLO-World.
- **Persistencia en tiempo real:** eventos de IA guardados en MySQL y visibles en el panel.
- **Personalización:** logo, fondo de login, fondo del sistema y color principal editables por el administrador.
- **Alta de barberos:** el administrador crea nombre, correo, usuario y contraseña; el trabajador completa su perfil con una captura de cámara en su primer ingreso.
- **Seguridad:** bloqueo por intentos fallidos, cambio de contraseña, roles ADMIN/BARBER.
- **Monitoreo:** cliente sentado por zona de silla, herramientas, acciones básicas y corte contabilizado después de un mínimo configurable.

## Importante sobre rostro

Esta versión **NO identifica personas por su rostro**. El onboarding comprueba que haya un rostro visible y guarda una foto de perfil con consentimiento. Para iniciar sesión se usa usuario/contraseña. Esto evita convertir el sistema en un mecanismo biométrico de identificación laboral.

## Detecciones de herramientas

El sistema intenta reconocer:

- peine
- tijeras
- máquina de cortar / trimmer
- secadora
- navaja

Para precisión real debes entrenar `ai-service/models/barber_tools.pt` con imágenes de tus herramientas. YOLO-World se usa como respaldo y no garantiza la misma precisión que un modelo personalizado.

## Regla de los 2 minutos

`MIN_SERVICE_SECONDS=120` determina el tiempo mínimo para generar `CORTE_CONTABILIZADO`. Puedes cambiarlo sin modificar código.

## Ejecutar localmente

### Opción simple con Docker

```bash
docker compose up --build
```

- Frontend: http://localhost:5173
- Backend: http://localhost:8093
- IA: http://localhost:8000
- MySQL: localhost:3307

### Credenciales de desarrollo

- Usuario: `barbergarage`
- Correo: `barbergarage@gmail.com`
- Contraseña local por defecto: `barbergarage2026`

**Cámbiala antes de publicar.** Esa contraseña ya no debe considerarse secreta.

## Flujo del trabajador

1. El administrador crea la cuenta del barbero.
2. El barbero entra con usuario y contraseña.
3. Como su onboarding está pendiente, el sistema abre la captura de foto.
4. La IA verifica que exista un rostro visible y con calidad básica.
5. Se guarda la foto de perfil y el barbero accede a `Mi estación`.
6. El barbero activa la webcam.
7. El frontend envía frames al servicio de IA.
8. IA registra eventos en el backend.
9. El jefe ve los eventos en `Monitoreo IA`.

## Entrenar herramientas

1. Etiqueta imágenes con estas clases: `maquina_cortar`, `tijeras`, `peine`, `secadora`, `navaja`.
2. Exporta formato YOLO.
3. Ajusta `barber_tools.yaml` si cambias la ubicación.
4. Ejecuta:

```bash
cd ai-service
python train_tools.py
```

5. Copia `best.pt` a `ai-service/models/barber_tools.pt`.

## Variables de producción

Ver `DEPLOY_RAILWAY.md`.

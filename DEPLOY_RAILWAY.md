# Despliegue en Railway

El proyecto está preparado como monorepo con **3 servicios de aplicación + MySQL**.

## 1. Sube el proyecto a GitHub

Sube la carpeta `BarberGaragePro` a un repositorio privado. No subas `.env` ni contraseñas reales.

## 2. Crea el proyecto en Railway

Agrega una base de datos **MySQL** y tres servicios desde el mismo repositorio:

- `backend` → Root Directory: `/backend`
- `ai-service` → Root Directory: `/ai-service`
- `frontend` → Root Directory: `/frontend`

Cada carpeta contiene su Dockerfile.

## 3. Backend

Variables sugeridas:

```text
SPRING_DATASOURCE_URL=jdbc:mysql://<MYSQL_HOST>:<MYSQL_PORT>/<MYSQL_DATABASE>?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=<MYSQL_USER>
SPRING_DATASOURCE_PASSWORD=<MYSQL_PASSWORD>
JWT_SECRET=<secreto aleatorio de 48+ caracteres>
AI_SERVICE_TOKEN=<otro secreto aleatorio>
CORS_ALLOWED_ORIGINS=https://TU-FRONTEND.up.railway.app
ADMIN_USERNAME=barbergarage
ADMIN_EMAIL=barbergarage@gmail.com
ADMIN_PASSWORD=<NUEVA_CONTRASEÑA_SEGURA>
JPA_DDL_AUTO=update
```

## 4. AI Service

```text
BACKEND_URL=https://TU-BACKEND.up.railway.app
AI_SERVICE_TOKEN=<exactamente el mismo token del backend>
MIN_SERVICE_SECONDS=120
AI_CONF=0.25
```

Para usar el modelo personalizado, incluye `models/barber_tools.pt` en el repositorio (si el tamaño lo permite) o descárgalo en el build desde almacenamiento privado.

## 5. Frontend

El frontend utiliza variables en tiempo de compilación:

```text
VITE_API_URL=https://TU-BACKEND.up.railway.app
VITE_AI_URL=https://TU-AI.up.railway.app
```

Configúralas como build variables del servicio antes de desplegar.

## 6. Seguridad antes de publicar

- Cambia la contraseña del administrador: la contraseña de ejemplo quedó expuesta durante el desarrollo.
- Usa JWT_SECRET y AI_SERVICE_TOKEN aleatorios.
- Mantén el repositorio privado mientras existan credenciales o fotos de empleados.
- Solicita consentimiento informado para almacenar fotos de perfil.
- Define una política de retención/borrado de snapshots y fotos.
- Activa HTTPS (Railway lo proporciona en los dominios públicos).

## 7. Rendimiento de IA

El análisis de video consume CPU. El frontend envía aproximadamente **1 frame por segundo** para reducir carga. Si necesitas video de alta frecuencia, considera un servicio con GPU o ejecutar el agente de IA cerca de la cámara y enviar solo eventos al backend.

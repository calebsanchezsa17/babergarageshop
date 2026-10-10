# Cambios implementados en Barber Garage Pro

## Administración
- Cuenta inicial de administrador configurable por variables de entorno.
- Registro de barberos con nombre, correo, usuario, contraseña y color de uniforme/mandil.
- Bloqueo temporal tras intentos fallidos y desbloqueo desde el panel.
- Deshabilitar/activar cuentas.
- Cambio de contraseña.
- Foto de perfil capturada por cámara en el primer ingreso.
- Personalización de logo, fondo del login, fondo general y color principal.

## Monitoreo
- Detección de persona/cliente dentro de una zona configurable de silla.
- Seguimiento corporal mediante YOLO-Pose.
- Detección de peine, tijeras, máquina de cortar, secadora y navaja.
- Modelo personalizado opcional `barber_tools.pt` + YOLO-World como respaldo.
- Movimientos básicos: Peinado/Acomodo, Corte con máquina, Corte con tijeras y Secado/Moldeado.
- Regla configurable de tiempo mínimo (120 s por defecto) antes de contabilizar un corte.
- Eventos persistidos en MySQL y visibles en el panel del administrador.
- Detección aproximada de color de vestimenta/mandil para apoyo de estación, sin usarla como identidad biométrica.

## Rostro
- Captura de foto de perfil desde cámara.
- Verificación de presencia/calidad básica de rostro.
- Puntos faciales básicos de YOLO-Pose cuando están disponibles.
- No se implementa reconocimiento/identificación biométrica de empleados por rostro.

## Railway
- Dockerfile independiente para frontend, backend y servicio IA.
- Monorepo listo para configurar con Root Directory `/frontend`, `/backend`, `/ai-service`.
- Guía `DEPLOY_RAILWAY.md`.

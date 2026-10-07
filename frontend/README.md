# Data-Health · Frontend

SPA en Angular 20 para el backend de Data-Health (Spring Boot + Oracle). Repositorio independiente del backend.

## Cómo correrlo

```bash
npm install
npm start          # http://localhost:4200
```

El backend debe estar en `http://localhost:8080` (se cambia en `src/environments/environment.development.ts`).
El CORS del backend ya permite `http://localhost:4200` (`app.cors.allowed-origin`).
La base de datos y el administrador de prueba (`admin@demo.com` / `Admin12345*`) se crean con los scripts de `backend/database/`.

## Estructura (espejo del backend)

```
src/app/
├── core/                    transversal (equivale a security/config/exception del backend)
│   ├── models/              Rol, sesión, login
│   ├── services/            AuthService (JWT en sessionStorage), AccesibilidadService
│   ├── interceptors/        jwtInterceptor (Bearer) · errorInterceptor (401 → cerrar sesión)
│   ├── guards/              authGuard · invitadoGuard · rolGuard (RF-04)
│   ├── http/                ApiError {fecha, estado, mensaje, ruta} y mensajeDeError()
│   └── navigation/          MODULOS: qué rol ve qué módulo
├── layout/                  Shell: menú por rol, tamaño de texto y contraste alto (RNF-08)
└── features/                un módulo por módulo del backend
    ├── auth/                M1 · login
    ├── usuarios/            M1 · registrar usuarios (solo ADMIN_ENTIDAD)
    ├── pacientes/           M2 · buscar por documento / registrar (Proceso 1)
    ├── historia-clinica/    M3 · línea de tiempo de atenciones
    └── ordenes/             M4 · pendiente (HU-07)
```

Cada feature tiene `*.models.ts` (DTOs), `*.service.ts` (HTTP) y componentes.

## Decisiones que siguen los ADR

- **ADR-03**: nunca se envía `id_eps` desde el cliente; el backend lo toma del JWT.
- **ADR-04**: JWT en `sessionStorage`, logout solo del lado del cliente, mensajes de login genéricos.
- **ADR-07**: prefijo `/api/v1`, `Authorization: Bearer`, errores con formato uniforme.

## Contratos supuestos (verificar contra el backend)

Los controladores y DTOs del backend estaban vacíos en el zip; estos contratos son supuestos y se ajustan en un solo lugar cada uno:

| Qué | Supuesto | Dónde ajustar |
| --- | --- | --- |
| Login | `POST /auth/login` `{email, password}` → `{token}` | `auth.models.ts`, `auth.service.ts` |
| Claims del JWT | `sub`, `rol`, `id_eps`, `nombre` (opcional), `exp` | `AuthService.decodificar()` |
| Usuarios | `POST /usuarios` `{nombre, email, password, rol}` | `usuarios.models.ts` |
| Pacientes | `GET /pacientes?documento=` (404 si no existe), `POST /pacientes` | `pacientes.service.ts` |
| Historia | `GET /historia-clinica`, `GET /pacientes/{id}/historia-clinica` | `historia-clinica.service.ts` |

## Pendiente

- Módulo de órdenes y resultados (HU-07) y listado/baja de usuarios.
- Mostrar al administrador las alertas de bloqueo por intentos fallidos (RF-05).
- Pruebas por módulo (RNF-20).

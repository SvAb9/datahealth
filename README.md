# Data-Health

Plataforma de gestión de información médica de adultos mayores, con aislamiento total de datos por EPS.
Repositorio único (ADR-01): backend y frontend conviven en carpetas separadas.

```
datahealth-main/
├── backend/     Java 21 + Spring Boot (Maven) + Oracle
└── frontend/    Angular 20 (SPA)
```

## Backend

```bash
cd backend
./mvnw spring-boot:run     # http://localhost:8080
```

Requiere las variables de entorno `DB_USER`, `DB_PASSWORD` y `JWT_SECRET`, y Oracle en `localhost:1521/XEPDB1`.

## Frontend

```bash
cd frontend
npm install
npm start                  # http://localhost:4200
```

El CORS del backend ya permite `http://localhost:4200`. Más detalles en `frontend/README.md`.

## Base de datos

Scripts SQL para Oracle en `backend/database/`. Docker: `backend/DOCKER.md`.

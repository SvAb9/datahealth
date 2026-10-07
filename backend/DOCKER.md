# Dockerizar el backend de Data-Health (Spring Boot + Oracle)

Guía adaptada de "Dockerizar una API Spring Boot", cambiando MySQL por **Oracle** (el mismo que administras con **Oracle SQL Developer**).

**Fase 2 de la guía:** el backend corre en un contenedor y Oracle sigue en tu computadora.

```
Navegador / Postman ──► localhost:8080 ──► [ Contenedor: Spring Boot ] ──► host.docker.internal:1521 ──► Oracle (tu PC)
```

## Qué cambia respecto a la guía de MySQL

| Tema | Guía original (MySQL) | Data-Health (Oracle) |
| --- | --- | --- |
| URL JDBC | `jdbc:mysql://host.docker.internal:3306/db` | `jdbc:oracle:thin:@//host.docker.internal:1521/XEPDB1` |
| Puerto | 3306 | 1521 |
| Qué identifica la base | nombre de la base (`db`) | **nombre de servicio** (`XEPDB1`), no es un SID |
| Driver | `mysql-connector` | `ojdbc17`, **ya está en tu `pom.xml`** |
| Usuario y clave | en el `.properties` | **no van en el `.properties` ni en la imagen**: llegan por variables de entorno |
| Tablas | las crea Hibernate | **deben existir ya**: `ddl-auto=validate` solo verifica, no crea |
| Nombre del JAR | `api-medicamentos-0.0.1-SNAPSHOT.jar` | `datahealth-0.0.1-SNAPSHOT.jar` |

## 0. Requisitos

- Docker Desktop instalado y abierto (`docker --version` debe responder).
- Oracle corriendo en tu PC, con el esquema de Data-Health ya creado y tus tablas existentes.
- En SQL Developer, abre las propiedades de tu conexión y anota **Nombre de servicio**, **puerto**, **usuario** y **contraseña**. Si el servicio no es `XEPDB1`, cámbialo en el paso 1.

## 1. Archivos que se agregaron al proyecto (carpeta `backend/`)

| Archivo | Para qué sirve |
| --- | --- |
| `src/main/resources/application-docker.properties` | Perfil `docker`: cambia `localhost` por `host.docker.internal` |
| `Dockerfile` | La receta de la imagen |
| `.dockerignore` | Qué no enviar a Docker (**`target/` sí debe ir**) |
| `.env.example` | Plantilla de variables secretas |

El perfil docker solo sobrescribe la URL; usuario, clave y JWT siguen saliendo de `DB_USER`, `DB_PASSWORD` y `JWT_SECRET`, igual que en `application.properties`.

Crea tu archivo de secretos (ya está ignorado por Git y por Docker):

```bash
cd backend
cp .env.example .env      # en Windows: copy .env.example .env
# edita .env con tus valores reales
```

## 2. Pruebas y JAR

La guía pide pasar las pruebas antes de dockerizar:

```bash
./mvnw test
```

> `DatahealthApplicationTests` arranca el contexto de Spring completo, así que necesita Oracle encendido y `DB_USER`, `DB_PASSWORD` y `JWT_SECRET` definidos en tu terminal. Si falla por eso y no por tu código, es esperado.

Genera el JAR:

```bash
./mvnw clean package
```

Resultado: `target/datahealth-0.0.1-SNAPSHOT.jar`. (Si no tienes Oracle a mano en ese momento, usa `./mvnw clean package -DskipTests`.)

## 3. Construir la imagen

```bash
docker build -t datahealth-api .
docker images             # debe aparecer datahealth-api
```

El punto final `.` significa "usa la carpeta actual como contexto".

## 4. Ejecutar el contenedor

```bash
docker run -d --name datahealth-api -p 8080:8080 --env-file .env datahealth-api
```

- `-d`: segundo plano.
- `-p 8080:8080`: puerto de tu PC : puerto del contenedor.
- `--env-file .env`: inyecta `DB_USER`, `DB_PASSWORD` y `JWT_SECRET` sin meterlos en la imagen.
- `--name`: nombre fijo para no depender del ID.

**En Linux** `host.docker.internal` no existe por defecto; agrega:

```bash
docker run -d --name datahealth-api -p 8080:8080 --env-file .env \
  --add-host=host.docker.internal:host-gateway datahealth-api
```

## 5. Verificar

```bash
docker ps                          # el contenedor debe estar "Up"
docker logs -f datahealth-api      # busca "Started DatahealthApplication"
```

Luego prueba desde Postman o el frontend (`http://localhost:8080/api/v1/...`).
El frontend en `npm start` sigue funcionando igual: el CORS permite `http://localhost:4200`.

Recorrido de una petición: Postman → puerto 8080 de tu PC → puerto 8080 del contenedor → Spring Boot → `host.docker.internal:1521` → Oracle local.

## 6. Gestión y limpieza

| Comando | Qué hace |
| --- | --- |
| `docker ps` | Lista contenedores activos |
| `docker stop datahealth-api` | Detiene el contenedor |
| `docker rm datahealth-api` | Elimina el contenedor (la imagen sigue) |
| `docker rmi datahealth-api` | Elimina la imagen |

Cada vez que cambies código: `./mvnw clean package` → `docker rm -f datahealth-api` → `docker build -t datahealth-api .` → `docker run ...`.

## 7. Errores comunes con Oracle

| Síntoma en `docker logs` | Causa probable | Solución |
| --- | --- | --- |
| `Connection refused` / `IO Error: The Network Adapter could not establish the connection` | El listener no acepta la conexión desde Docker | Verifica con `lsnrctl status` que escucha en 1521; en Windows, crea una regla de entrada del firewall para el puerto 1521 |
| `ORA-12514` | El nombre de servicio no existe | Copia el **Nombre de servicio** exacto desde SQL Developer y ajusta `application-docker.properties` |
| `ORA-01017: invalid username/password` | `.env` con datos incorrectos | Revisa `DB_USER` y `DB_PASSWORD`; sin comillas ni espacios sobrantes |
| `UnknownHostException: host.docker.internal` | Estás en Linux | Usa `--add-host=host.docker.internal:host-gateway` |
| `Schema-validation: missing table ...` | Las tablas no existen en ese usuario/esquema | Ejecuta antes tu script de creación en SQL Developer, con el mismo usuario de `DB_USER` |
| `Could not resolve placeholder 'JWT_SECRET'` | Falta la variable | Revisa que `.env` exista y que uses `--env-file .env` |
| La clave JWT es rechazada por corta | `JWT_SECRET` con menos de 32 caracteres | Usa una clave más larga |

## 8. Siguiente paso (Fase 3)

La URL ya admite la variable `DB_URL`, así que el mismo contenedor puede conectarse a un Oracle en la nube sin reconstruir la imagen:

```bash
docker run -d -p 8080:8080 --env-file .env \
  -e DB_URL="jdbc:oracle:thin:@//mi-servidor:1521/MI_SERVICIO" datahealth-api
```

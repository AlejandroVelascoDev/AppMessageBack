# Mobile App Backend

Backend REST API para la aplicacion de mensajeria movil. Construido con Spring Boot 3, PostgreSQL y WebSocket.

---

## Requisitos previos

- [Docker](https://www.docker.com/get-started) y Docker Compose
- [Java 17](https://adoptium.net/) o superior
- [Maven 3.8+](https://maven.apache.org/download.cgi) (o usar el wrapper `./mvnw`)

---

## Levantar la base de datos con Docker

El proyecto requiere PostgreSQL corriendo antes de iniciar la aplicacion. Con Docker Compose es inmediato:

```bash
docker-compose up -d
```

Esto levanta un contenedor `mobile_app_db` con:

| Parametro  | Valor          |
|------------|----------------|
| Host       | localhost:5432  |
| Base datos | mobile_app_db  |
| Usuario    | postgres       |
| Password   | postgres       |

Para detener y eliminar el contenedor:

```bash
docker-compose down
```

Para eliminar tambien el volumen de datos:

```bash
docker-compose down -v
```

---

## Ejecutar la aplicacion

```bash
./mvnw spring-boot:run
```

O si tienes Maven instalado globalmente:

```bash
mvn spring-boot:run
```

La API quedara disponible en `http://localhost:8080`.

Al arrancar por primera vez con la base de datos vacia, el sistema inserta automaticamente datos de prueba:

### Usuarios de prueba

| Username | Email               | Password    |
|----------|---------------------|-------------|
| alice    | alice@example.com   | password123 |
| bob      | bob@example.com     | password123 |
| charlie  | charlie@example.com | password123 |

### Datos precargados

- 1 chat directo entre **alice** y **bob** con 3 mensajes
- 1 chat grupal **"Grupo de prueba"** con alice, bob y charlie con 2 mensajes

---

## Endpoints principales

### Autenticacion

```bash
# Registro
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "usuario", "email": "usuario@example.com", "password": "password123"}'

# Login (devuelve JWT)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "alice@example.com", "password": "password123"}'
```

### Usuarios

```bash
# Perfil propio
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer <JWT>"

# Usuario por ID
curl http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer <JWT>"
```

---

## Configuracion

El archivo de configuracion se encuentra en `src/main/resources/application.properties`.

Las variables clave son:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mobile_app_db
spring.datasource.username=postgres
spring.datasource.password=postgres
jwt.secret=<clave secreta>
jwt.expiration=86400000
```

---

## Ejecutar todo con Docker

El `Dockerfile` construye la imagen de la API y `docker-compose.yml` levanta la API junto a PostgreSQL:

```bash
docker compose up --build
```

La API queda disponible en `http://localhost:8080`. El contenedor de la app espera a que PostgreSQL este saludable antes de arrancar.

---

## Base de datos en Neon

Neon es el PostgreSQL administrado que usaremos en produccion. El proyecto incluye el esquema en `db/neon-schema.sql`.

### 1. Crear el proyecto y la base de datos

Opcion A: desde el panel de Neon (`https://console.neon.tech`).

1. Crea un proyecto y elige la region mas cercana a tus usuarios.
2. Neon crea una base de datos `neondb` (puedes crear otra).
3. En **Connect** copia dos cadenas: la **pooled** (host con `-pooler`) y la **directa**.

Opcion B: con el CLI de Neon (requiere `neon login`):

```bash
npm i -g neon
neon login
neon link --org-id <org-id> --project-name appmessage --region-id aws-us-east-1
neon connection-string --pooled
neon connection-string
```

### 2. Crear el esquema

Ejecuta `db/neon-schema.sql` en el **SQL Editor** de Neon, o con `psql` usando la cadena directa (sin `-pooler`):

```bash
psql "<direct-connection-string>" -f db/neon-schema.sql
```

Con el esquema creado, configura `JPA_DDL_AUTO=validate` para que Hibernate no modifique la estructura en produccion.

### 3. Variables de entorno

Copia `.env.example` a `.env` para desarrollo local contra Neon (o define las variables en Vercel). Spring necesita la URL en formato JDBC y sin credenciales:

| Cadena Neon | Valor para Spring |
|-------------|-------------------|
| `postgresql://user:pass@ep-xxx-pooler.region.aws.neon.tech/neondb?sslmode=require` | `SPRING_DATASOURCE_URL=jdbc:postgresql://ep-xxx-pooler.region.aws.neon.tech/neondb?sslmode=require` |

- Usa la cadena **pooled** (`-pooler`) para la API.
- Usa la cadena **directa** solo para migraciones o `psql`.
- `sslmode=require` es obligatorio en Neon.
- El usuario y el password van en `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`.

La aplicacion tambien acepta directamente `DATABASE_URL` (la cadena pooled que entrega Neon) y la convierte a formato JDBC al arrancar. Si `SPRING_DATASOURCE_URL` esta definida, tiene prioridad sobre `DATABASE_URL`. `neon deploy` escribe esa variable en `.env.local`.

### 4. Nota sobre el plan gratuito

El compute de Neon se suspende por inactividad (5 minutos por defecto) y la primera consulta tras suspenderse tiene una penalizacion de arranque. Para una app de chat con WebSockets considera un plan con autoscaling o desactivar la suspension.

---

## Despliegue en Vercel (contenedor OCI)

El proyecto incluye un `Dockerfile` multi-stage y un `vercel.json` con `"framework": "container"`, por lo que Vercel puede construir y ejecutar la imagen como una Function.

### 1. Variables de entorno

En Vercel (Project Settings > Environment Variables) configura:

| Variable                    | Obligatoria | Descripcion                                                        |
|-----------------------------|-------------|--------------------------------------------------------------------|
| `DATABASE_URL`              | Si*         | Cadena pooled de Neon; la app la convierte a JDBC automaticamente  |
| `SPRING_DATASOURCE_URL`     | Si*         | Alternativa en formato JDBC, ej. `jdbc:postgresql://host:5432/db?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME`| Si*         | Usuario de la base de datos (con el trio `SPRING_DATASOURCE_*`)     |
| `SPRING_DATASOURCE_PASSWORD`| Si*         | Password de la base de datos (con el trio `SPRING_DATASOURCE_*`)    |
| `JWT_SECRET`                | Si          | Clave Base64 de al menos 32 bytes                                  |
| `JWT_EXPIRATION`            | No          | Milisegundos de validez del token (default `86400000`)             |
| `JPA_DDL_AUTO`              | No          | `update` (default) o `validate` en produccion                      |
| `JPA_SHOW_SQL`              | No          | `false` por defecto                                                |

\* Define `DATABASE_URL` o, en su lugar, el trio `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`.

Vercel no hospeda PostgreSQL: usa un proveedor externo (Neon, Supabase, Vercel Postgres, etc.). Con Neon basta con `DATABASE_URL`; si usas otra variable, convierte el connection string a formato JDBC (`jdbc:postgresql://...`) y agrega `?sslmode=require`.

### 2. Puerto

El contenedor escucha en el puerto `80` por defecto (lo esperado por Vercel). Si necesitas otro, define la variable `PORT` en el proyecto.

### 3. Desplegar

```bash
vercel
```

O conecta el repositorio de Git y Vercel reconstruira la imagen en cada push.

### Nota sobre WebSocket

Las Functions de Vercel limitan la duracion de cada conexion WebSocket segun el plan (Hobby 300s, Pro 800s). El cliente debe reconectar automaticamente y el estado compartido (salas, presencia) debe vivir en un store externo (ej. Redis), no en memoria del proceso.

---

## Estructura del proyecto

```
src/main/java/com/mobile/backend/
├── config/          # Configuracion (CORS, Security, WebSocket, DataInitializer)
├── controller/      # Controladores REST
├── dto/             # Objetos de transferencia de datos
├── entity/          # Entidades JPA (User, Chat, Message)
├── repository/      # Repositorios Spring Data
├── security/        # JWT y filtros de autenticacion
├── service/         # Logica de negocio
└── websocket/       # Handlers y configuracion WebSocket
```

diagram of the API
<img width="884" height="897" alt="image" src="https://github.com/user-attachments/assets/f60c1a79-a262-4015-9783-e705949ba80e" />


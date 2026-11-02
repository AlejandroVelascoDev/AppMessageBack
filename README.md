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

## Despliegue en Vercel (contenedor OCI)

El proyecto incluye un `Dockerfile` multi-stage y un `vercel.json` con `"framework": "container"`, por lo que Vercel puede construir y ejecutar la imagen como una Function.

### 1. Variables de entorno

En Vercel (Project Settings > Environment Variables) configura:

| Variable                    | Obligatoria | Descripcion                                                        |
|-----------------------------|-------------|--------------------------------------------------------------------|
| `SPRING_DATASOURCE_URL`     | Si          | URL JDBC de PostgreSQL, ej. `jdbc:postgresql://host:5432/db?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME`| Si          | Usuario de la base de datos                                        |
| `SPRING_DATASOURCE_PASSWORD`| Si          | Password de la base de datos                                       |
| `JWT_SECRET`                | Si          | Clave Base64 de al menos 32 bytes                                  |
| `JWT_EXPIRATION`            | No          | Milisegundos de validez del token (default `86400000`)             |
| `JPA_DDL_AUTO`              | No          | `update` (default) o `validate` en produccion                      |
| `JPA_SHOW_SQL`              | No          | `false` por defecto                                                |

Vercel no hospeda PostgreSQL: usa un proveedor externo (Neon, Supabase, Vercel Postgres, etc.). Convierte el connection string a formato JDBC (`jdbc:postgresql://...`) y agrega `?sslmode=require`.

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


# 🗨️ ForoHub

[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8-blue?logo=mysql)](https://www.mysql.com/)
[![Flyway](https://img.shields.io/badge/Flyway-Migrations-red?logo=flyway)](https://flywaydb.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

> **ForoHub** es una API RESTful desarrollada con **Spring Boot** que gestiona tópicos (posts) en un foro, con autenticación JWT, validaciones, manejo de errores y migraciones automáticas.

---

## 📑 Tabla de Contenidos
- [🚀 Características](#-características)
- [🛠️ Tecnologías](#️-tecnologías)
- [📂 Estructura del Proyecto](#-estructura-del-proyecto)
- [⚙️ Instalación y Configuración](#️-instalación-y-configuración)
- [📜 Endpoints Principales](#-endpoints-principales)
- [🔐 Autenticación y Seguridad](#-autenticación-y-seguridad)
- [🧪 Ejecución de Tests](#-ejecución-de-tests)
- [📌 Notas de Desarrollo](#-notas-de-desarrollo)
- [📄 Licencia](#-licencia)

---

## 🚀 Características
- **CRUD completo de tópicos** con paginación.
- **Autenticación JWT** y autorización para rutas protegidas.
- **Validación de datos** con *Bean Validation*.
- **Manejo centralizado de errores** (400, 401, 403, 404, 409).
- **Migraciones automáticas** con Flyway.
- **Codificación UTF-8** para soporte completo de caracteres.
- **Scripts PowerShell** para validación automática de la API:
  - `forohub-check.ps1` → Validación estándar.
  - `forohub-check-stricto.ps1` → Validación estricta.

---

## 🛠️ Tecnologías
- **Java 17**
- **Spring Boot 3.x**
- **Spring Data JPA**
- **Spring Security**
- **MySQL 8.x**
- **Flyway**
- **Lombok**
- **Jakarta Validation**

---

## 📂 Estructura del Proyecto
```
forohub/
├── src/main/java/com/forohub
│   ├── auth/        → Módulo de autenticación y seguridad
│   ├── config/      → Configuración general (CORS, inicializadores, etc.)
│   ├── exception/   → Manejo global de excepciones
│   ├── service/     → Lógica de negocio
│   └── topicos/     → CRUD de tópicos
├── src/main/resources
│   ├── application.yaml  → Configuración de la app
│   └── db/migration/     → Migraciones Flyway
└── forohub-check*.ps1    → Scripts de validación
```

---

## ⚙️ Instalación y Configuración

### 1️⃣ Clonar repositorio
```bash
git clone https://github.com/usuario/forohub.git
cd forohub
```

### 2️⃣ Configurar Base de Datos
```sql
CREATE DATABASE forohub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'forohub'@'%' IDENTIFIED BY 'forohub123';
GRANT ALL PRIVILEGES ON forohub.* TO 'forohub'@'%';
FLUSH PRIVILEGES;
```

### 3️⃣ Variables de configuración (`application.yaml`)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/forohub?createDatabaseIfNotExist=true&serverTimezone=UTC&useUnicode=true&characterEncoding=utf8
    username: forohub
    password: forohub123
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true
    locations: classpath:db/migration
```

### 4️⃣ Compilar y ejecutar
```bash
./mvnw clean package
java -jar target/forohub-0.0.1-SNAPSHOT.jar
```

---

## 📜 Endpoints Principales

| Método | Endpoint        | Descripción                  | Auth Requerida |
|--------|----------------|------------------------------|----------------|
| GET    | `/topicos`     | Listar tópicos (paginado)     | ❌             |
| GET    | `/topicos/{id}`| Obtener un tópico por ID      | ❌             |
| POST   | `/topicos`     | Crear un nuevo tópico         | ✅             |
| PUT    | `/topicos/{id}`| Actualizar un tópico          | ✅             |
| DELETE | `/topicos/{id}`| Eliminar un tópico            | ✅             |

---

## 🔐 Autenticación y Seguridad
- Registro y login de usuarios con credenciales.
- Generación de **JWT** con validez configurable (`ttl-seconds`).
- Acceso a rutas protegidas con **Bearer Token**.
- Permisos diferenciados para usuarios autenticados.

---

## 🧪 Ejecución de Tests
```bash
# Validación estándar
powershell.exe -ExecutionPolicy Bypass -File .\forohub-check.ps1

# Validación estricta
powershell.exe -ExecutionPolicy Bypass -File .\forohub-check-stricto.ps1
```

---

## 📌 Notas de Desarrollo
- Configuración de **UTF-8** forzada para evitar problemas con acentos y caracteres especiales.
- Manejo de errores homogéneo en formato JSON.
- Scripts PowerShell listos para CI/CD.

---

## 📄 Licencia
Este proyecto está bajo la licencia **MIT**. Consulta el archivo [LICENSE](LICENSE) para más información.

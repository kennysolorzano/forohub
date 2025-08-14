# ForoHub

Aplicación de ejemplo para gestionar tópicos en un foro, desarrollada con Spring Boot, Spring Data JPA y Spring Security.

## 🚀 Características principales

- API REST para CRUD de tópicos.
- Autenticación y autorización con JWT.
- Persistencia con MySQL en producción y H2 en pruebas.
- Migraciones de base de datos con Flyway.
- Tests de integración con MockMvc.

## 📦 Tecnologías

- Java 21
- Spring Boot 3
- Spring Data JPA
- Spring Security
- JWT
- Flyway
- Maven

## ⚙️ Configuración

### Producción
Configura tu `application.properties` o variables de entorno:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/forohub
spring.datasource.username=usuario
spring.datasource.password=clave
spring.jpa.hibernate.ddl-auto=validate
```

### Pruebas
El perfil `test` usa una base en memoria H2 con Flyway. Configurado en `src/test/resources/application-test.yaml`.

## 🧪 Ejecución de tests

```bash
mvn clean verify
```

## 🛠️ CI/CD

El proyecto incluye un workflow en `.github/workflows/ci.yml` para ejecutar tests en cada push o pull request a `main`.

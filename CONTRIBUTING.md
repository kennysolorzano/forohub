# Contribuir a Forohub

¡Gracias por tu interés!

## Flujo básico
1. Crea un issue (bug o feature) describiendo el cambio.
2. Crea una rama desde `main`: `feat/...` o `fix/...`.
3. Asegura estilo y formato (ver `.editorconfig`) y corre `mvn -q clean test`.
4. Abre un Pull Request usando la plantilla.

## Estándares
- **Java 21**, **Spring Boot 3**, **Maven**.
- Convenciones: paquetes `com.forohub.*`, controladores en `com.forohub.topicos`.
- **Flyway** para cambios en DB (`src/main/resources/db/migration/VX__...sql`).

## Commits
- Mensajes cortos y descriptivos (imperativo): `fix: ...`, `feat: ...`, `docs: ...`, `refactor: ...`, `test: ...`.

## Secretos
- Nunca subir llaves o contraseñas. Usar **GitHub Actions Secrets** (ver README).

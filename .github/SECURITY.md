# Política de Seguridad

## Reporte de vulnerabilidades

**Preferido:** usa el flujo privado de GitHub: ve a la pestaña **Security → Report a vulnerability** en este repositorio y sigue los pasos del Security Advisory.  
Alternativa (si no ves esa opción): abre un **Issue** marcado con la etiqueta `security` sin incluir PoC ni datos explotables; te contactaremos por privado para continuar.

## Buenas prácticas del repo
- No subir secretos en código/commits. Usa **GitHub Actions Secrets** o variables de entorno locales.
- Revisa diffs antes de abrir PR para evitar fugas de credenciales.
- Dependencias se monitorean con **Dependabot** de forma semanal (`.github/dependabot.yml`).

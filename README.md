# Bad Inventory API

Este es un proyecto educativo de Spring Boot diseñado intencionalmente de forma deficiente y con **malas prácticas** de desarrollo de código (Technical Debt). El objetivo principal de este repositorio es identificar estos anti-patrones y sirva de base para practicar la refactorización hacia una arquitectura limpia, mantenible y escalable.

## Resumen de Malas Prácticas Implementadas

A continuación, se detallan las principales malas prácticas que encontrarás en este proyecto:

1. **God Class / Controlador Omnipotente (`GodController.java`)**
   - Toda la responsabilidad (lógica de negocio, acceso a datos y manejo de peticiones HTTP en todos los endpoints) está concentrada en una única clase enorme.
   - Viola gravemente el principio de Responsabilidad Única (SRP) de SOLID y dificulta las pruebas unitarias.

2. **Ausencia de Arquitectura en Capas (Separation of Concerns)**
   - Faltan las capas lógicas tradicionales de una aplicación Spring Boot madura (`Services` para la lógica de negocio y `Repositories` para el acceso a la base de datos). El acceso se realiza muchas veces directamente dentro del controlador.

3. **Inexistencia de DTOs (Data Transfer Objects)**
   - No hay una separación clara entre las entidades o modelos base y lo que se devuelve al cliente. Se exponen directamente mapas, listas crudas o entidades internamente usadas, lo que en entornos reales genera vulnerabilidades (Over-Posting/Mass Assignment) y alto acoplamiento.

4. **Manejo de Errores Deficiente**
   - Ausencia de un manejador global de excepciones (por ejemplo, mediante `@ControllerAdvice`).
   - Los errores y las validaciones se manejan de manera manual con try-catch dispersos o, peor aún, se omite por completo devolviendo stack traces expuestos al usuario final, lo que es un gran fallo de seguridad.

5. **Configuraciones Embebidas / Hardcodeadas**
   - Contraseñas, cadenas de conexión, tokens u otras variables dinámicas que deberían estar externalizadas en archivos de propiedades adecuados o variables de entorno (como `application.yml` o `.env`), están puestas estáticamente dentro del código.

6. **Ignorar el control de versiones en archivos generados en compilación**
   - Inicialmente, se detectó la subida de ejecutables `.jar`, la carpeta `target/`, y logs (`app.log`, `app.pid`). Se incluyó posteriormente un `.gitignore` en este commit para enseñar la importancia de obviar estos archivos en repositorios Git.

¡Te invitamos a buscar estos errores dentro del código fuente y corregirlos!

# Feature Specification: Migración Tecnológica Java 8 → Java 21

**Feature Branch**: `001-java21-migration`
**Created**: 2026-05-14
**Status**: Draft
**Input**: Especificación Técnica de Migración — Java 8 / Spring Boot 2.7.18 → Java 21 / Spring Boot 3.3.x

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Compilación y arranque en el nuevo stack (Priority: P1)

El equipo de desarrollo necesita que la aplicación compile sin errores y arranque
correctamente sobre Java 21 y Spring Boot 3.3.x, con el mismo comportamiento
que tenía en Java 8 / Spring Boot 2.7.18.

**Por qué P1**: Es el prerequisito absoluto del resto de historias. Sin compilación
exitosa no hay nada que desplegar ni validar.

**Independent Test**: Ejecutar `mvn clean package` en un entorno con JDK 21;
la aplicación debe arrancar y responder en el puerto configurado.

**Acceptance Scenarios**:

1. **Given** el repositorio en estado actual (Java 8 / SBoot 2.7.18),
   **When** se aplican todos los cambios de migración y se ejecuta `mvn clean package -DskipTests`,
   **Then** la compilación finaliza con `BUILD SUCCESS` sin errores de compilador.
2. **Given** el artefacto compilado,
   **When** se inicia la aplicación con JVM 21,
   **Then** Spring Context carga completamente y el puerto HTTP responde.
3. **Given** la aplicación corriendo en Java 21,
   **When** se consulta cualquier endpoint existente,
   **Then** la respuesta HTTP (código + cuerpo) es idéntica a la de Java 8.

---

### User Story 2 — Despliegue en Tomcat externo (Priority: P2)

El equipo de operaciones necesita poder desplegar la aplicación como WAR en un
Tomcat externo (Tomcat 10.x, compatible con Jakarta EE 10), sin cambios en el
proceso de despliegue actual.

**Por qué P2**: Es un requisito de infraestructura crítico; sin él la aplicación
no puede desplegarse en el entorno de producción actual.

**Independent Test**: Generar el artefacto WAR y desplegarlo en Tomcat 10.x
independiente; la aplicación debe arrancar y responder a requests HTTP.

**Acceptance Scenarios**:

1. **Given** el proyecto migrado,
   **When** se ejecuta `mvn clean package` con perfil WAR,
   **Then** se genera un archivo `.war` desplegable.
2. **Given** el WAR generado,
   **When** se despliega en Tomcat 10.x,
   **Then** la aplicación arranca sin errores de ClassLoader ni dependencias faltantes.
3. **Given** la aplicación corriendo en Tomcat externo,
   **When** se consulta cualquier endpoint de la API,
   **Then** la respuesta es idéntica a la del entorno Java 8.

---

### User Story 3 — Paridad funcional validada por suite de pruebas (Priority: P3)

El equipo de QA necesita que la suite de pruebas existente pase en su totalidad
sobre el nuevo stack, garantizando que no se introdujo ninguna regresión funcional.

**Por qué P3**: Valida formalmente la paridad funcional exigida por la constitución
del proyecto. Puede ejecutarse en paralelo al despliegue Tomcat una vez compilado.

**Independent Test**: Ejecutar `mvn test` con JDK 21; todos los tests que pasaban
en Java 8 deben seguir pasando.

**Acceptance Scenarios**:

1. **Given** la aplicación migrada,
   **When** se ejecuta la suite de pruebas completa con JVM 21,
   **Then** el número de tests en verde es idéntico al baseline de Java 8 (cero regresiones).
2. **Given** la suite de pruebas ejecutada,
   **When** aparece algún test fallido,
   **Then** la causa es un cambio de comportamiento introducido por el nuevo stack
   (no por un bug previo) y se documenta como issue.

---

### Edge Cases

- ¿Qué ocurre si una dependencia transitiva importa `javax.*` y no ofrece versión
  con `jakarta.*`? → Se busca alternativa compatible; si no existe, se documenta
  como bloqueante antes de continuar la migración.
- ¿Qué ocurre si el esquema de la base de datos H2 cambia de comportamiento entre
  la versión gestionada por Spring Boot 2.x y la de Spring Boot 3.x? → Se verifica
  compatibilidad del DDL embebido; cualquier discrepancia se reporta como
  hallazgo sin corregir la lógica.
- ¿Qué ocurre si el packaging actual es JAR y no WAR? → Se añade la configuración
  de `SpringBootServletInitializer` y el packaging se cambia a `war` en el POM,
  manteniendo el ejecutable embebido como fallback si Spring Boot lo soporta.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El descriptor de compilación (`pom.xml`) DEBE declarar Java 21 como
  versión fuente y destino del compilador.
- **FR-002**: El parent de Spring Boot DEBE actualizarse de `2.7.18` a `3.3.x`
  (última versión estable disponible en Maven Central al momento de la migración).
- **FR-003**: Toda referencia a `javax.*` en el código fuente del proyecto DEBE
  reemplazarse por su equivalente `jakarta.*`.
- **FR-004**: La clase principal (`BadInventoryApiApplication`) o una clase dedicada
  DEBE extender `SpringBootServletInitializer` y sobreescribir `configure()` para
  habilitar el despliegue en Tomcat externo.
- **FR-005**: El driver de H2 DEBE ser la versión gestionada por el BOM de
  Spring Boot 3.3.x (H2 2.x), compatible con el sistema de módulos de Java 21.
- **FR-006**: El comportamiento observable de todos los endpoints HTTP existentes
  DEBE permanecer idéntico (mismos códigos de respuesta, misma estructura de cuerpo,
  mismos campos) antes y después de la migración.
- **FR-007**: Ninguna clase, método ni campo de lógica de negocio existente DEBE ser
  modificado salvo que sea estrictamente necesario por un Breaking Change del nuevo stack.
- **FR-008**: Las dependencias de terceros SOLO DEBEN actualizarse cuando presenten
  incompatibilidad documentada con Java 21 o Spring Boot 3.x.

### Key Entities

- **POM de Maven**: Descriptor central del proyecto; contiene versiones del stack,
  dependencias y configuración de build. Sujeto a cambios en versiones de Java,
  Spring Boot y dependencias incompatibles.
- **BadInventoryApiApplication**: Punto de entrada de la aplicación; candidata a
  extender `SpringBootServletInitializer` para el despliegue WAR.
- **GodController**: Clase controladora central; no se modificará su lógica de
  negocio; puede requerir actualización de imports `javax.*` → `jakarta.*`.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: La aplicación compila sin errores (`BUILD SUCCESS`) en un entorno
  JDK 21 con Maven.
- **SC-002**: El 100% de los tests que pasaban en Java 8 continúan pasando en
  Java 21 (cero regresiones en la suite existente).
- **SC-003**: La aplicación despliega y responde en Tomcat 10.x externo en menos
  tiempo del que tardaba en Java 8 (paridad o mejora en tiempo de arranque).
- **SC-004**: Todos los endpoints de la API devuelven respuestas con código HTTP y
  cuerpo idénticos a los de la versión Java 8 bajo las mismas entradas.
- **SC-005**: Ningún Pull Request de migración contiene cambios de lógica de negocio,
  correcciones de bugs ni mejoras estéticas (verificable en revisión de código).

---

## Assumptions

- El proyecto no utiliza SpringFox (confirmado: no aparece en `pom.xml`), por lo que
  no es necesaria la migración a SpringDoc/OpenAPI.
- El esquema DDL de H2 embebido (si existe en `resources`) es compatible con H2 2.x
  sin modificaciones; se verificará durante la ejecución de tests.
- El entorno de destino para despliegue externo es Tomcat 10.x (Jakarta EE 10); si
  el entorno es Tomcat 9.x (Java EE 8), la migración `javax.*` → `jakarta.*` no es
  aplicable y deberá revisarse la versión objetivo.
- Los consumidores actuales de la API no realizarán cambios simultáneos; la paridad
  funcional se valida únicamente en el lado del servidor.
- No existen referencias a APIs internas de la JVM (sun.*, com.sun.*) en el código
  fuente del proyecto; si se detectan, se tratarán como bloqueante.
- El build tool es Maven (confirmado: `pom.xml` presente, sin `build.gradle`).

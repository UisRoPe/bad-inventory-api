# Quickstart: bad-inventory-api (Java 21 / Spring Boot 3.3.x)

**Feature**: `001-java21-migration`
**Generated**: 2026-05-14

---

## Prerrequisitos

| Herramienta | Versión mínima | Verificar con |
|-------------|---------------|---------------|
| JDK | 21 (LTS) | `java -version` |
| Maven | 3.6.x o superior | `mvn -version` |
| Tomcat (opcional, solo WAR) | 10.x | `catalina.sh version` |

## Matriz de Comandos (Java 21)

| Objetivo | Comando |
|----------|---------|
| Compilar sin tests | `mvn clean package -DskipTests` |
| Ejecutar tests | `mvn test` |
| Ejecutar app embebida | `mvn spring-boot:run` |
| Generar WAR | `mvn clean package` |
| Validar dependencias | `mvn -q dependency:tree` |

---

## Ejecución Embebida (modo desarrollo)

```bash
# 1. Clonar el repositorio y posicionarse en la rama de migración
git clone <repo-url>
cd bad-inventory-api
git checkout 001-java21-migration

# 1.1 Verificar runtime Java 21 activo
java -version

# 2. Compilar y ejecutar (incluye Tomcat embebido en la ejecución local)
mvn spring-boot:run

# 3. Verificar arranque exitoso
# Esperar el mensaje: "Started BadInventoryApiApplication in X.XXX seconds"

# 4. Probar los endpoints
curl http://localhost:8080/products
curl -X POST http://localhost:8080/products \
     -H "Content-Type: application/json" \
     -d '{"title":"Test","price":"99.9","description":"Test product"}'
curl http://localhost:8080/products/1
```

Comprobación mínima de arranque:

- `java -version` reporta Java 21
- `mvn spring-boot:run` inicia sin excepciones de `jakarta.*`
- `GET /products` responde HTTP 200

---

## Construcción del Artefacto WAR

```bash
# Genera el WAR listo para Tomcat externo
mvn clean package -DskipTests

# El artefacto se genera en:
# target/bad-inventory-api-1.0.0-SNAPSHOT.war
```

---

## Despliegue en Tomcat 10.x Externo

```bash
# 1. Copiar el WAR al directorio webapps de Tomcat
cp target/bad-inventory-api-1.0.0-SNAPSHOT.war $CATALINA_HOME/webapps/bad-inventory-api.war

# 2. Iniciar Tomcat (si no está corriendo)
$CATALINA_HOME/bin/startup.sh

# 3. Esperar despliegue automático (~10-30 segundos)
# La aplicación estará disponible en:
# http://localhost:8080/bad-inventory-api/products

# 4. Verificar despliegue exitoso
curl http://localhost:8080/bad-inventory-api/products
```

Validación de bootstrap servlet externo:

- La clase `BadInventoryApiApplication` extiende `SpringBootServletInitializer`
- El WAR despliega sin `ClassNotFoundException: jakarta.servlet.*`

> **Importante**: Usar Tomcat **10.x** (Jakarta EE 10). Tomcat 9.x es incompatible
> con Spring Boot 3.x y producirá `ClassNotFoundException: jakarta.servlet.Servlet`.

---

## Ejecución de la Suite de Pruebas

```bash
# Ejecutar todos los tests (incluye el SmokeTest de migración)
mvn test

# Resultado esperado:
# Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
# BUILD SUCCESS
```

---

## Consola H2 (solo modo embebido)

La consola H2 está disponible en modo de desarrollo:

- **URL**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Usuario**: `root`
- **Contraseña**: `123456`

> **Advertencia de seguridad (pre-existente)**: Las credenciales están en texto plano
> en `application.properties`. Este es un bug conocido de la versión Java 8 que
> **no se corrige** en esta migración (Principio II de la Constitución).

---

## Verificación Rápida de Paridad Funcional

Ejecuta estos comandos antes y después de la migración y compara las respuestas:

```bash
# Baseline Java 8 (guardar output)
curl -s http://localhost:8080/products | python3 -m json.tool > baseline-products.json

# Post-migración Java 21 (comparar)
curl -s http://localhost:8080/products | python3 -m json.tool > migrated-products.json
diff baseline-products.json migrated-products.json
# Resultado esperado: sin diferencias (diff vacío)
```

---

## Bitácora de Validación Ejecutada

Ejecución real en este workspace (2026-05-14):

- `java -version` -> OpenJDK 21.0.10
- `mvn -version` -> Apache Maven 3.9.9 (Java 21.0.10)
- `mvn test` -> `Tests run: 6, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`
- `mvn clean package -DskipTests` -> WAR generado en `target/bad-inventory-api-1.0.0-SNAPSHOT.war`, `BUILD SUCCESS`

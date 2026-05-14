# Gap Analysis - Java 8/SB2.7.18 -> Java 21/SB3.3.x

Fecha: 2026-05-14
Proyecto: bad-inventory-api

## Resumen ejecutivo

Brechas obligatorias detectadas: 4
Brechas opcionales: 0
Bloqueantes sin workaround: 0

## Brechas obligatorias

1. Runtime/Build de Java
- Estado actual: java.version=1.8
- Estado objetivo: java.version=21
- Acción: actualizar pom.xml y validar toolchain Maven con JDK 21.

2. Spring Boot major upgrade
- Estado actual: spring-boot-starter-parent 2.7.18
- Estado objetivo: 3.3.x
- Acción: actualizar parent y resolver cambios transversales de Framework 6/Jakarta.

3. Jakarta namespace migration
- Hallazgo: import javax.annotation.PostConstruct en GodController.java
- Acción: migrar a jakarta.annotation.PostConstruct.

4. Deploy en Tomcat externo
- Estado actual: app standalone (jar)
- Requisito objetivo: soporte WAR en Tomcat 10.x
- Acción: SpringBootServletInitializer + packaging war + tomcat provided.

## Verificación de dependencias

Dependencias actuales:
- spring-boot-starter-web
- spring-boot-starter-jdbc
- h2 (runtime)

Resultado:
- H2 gestionado por BOM de Spring Boot 3.3.x (H2 2.x), sin pin manual.
- No se detecta spring-security ni springfox en pom actual.

## Riesgos

- Ausencia de suite de pruebas previa: se mitiga con SmokeTest mínimo de paridad.
- Diferencias de runtime en Tomcat externo: se mitiga con quickstart y checklist final.

## Salida requerida por fase 1

Reporte de librerías que requieren actualización obligatoria:
- org.springframework.boot:spring-boot-starter-parent -> 3.3.x
- Plataforma Java -> 21
- javax.annotation API usage -> jakarta.annotation API

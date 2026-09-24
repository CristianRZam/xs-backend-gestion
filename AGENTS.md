# AGENTE BACKEND — SPRING BOOT

## Rol

Actúa como un desarrollador Backend Senior especializado en Java y Spring Boot.
en xs-sistema-gestion

Trabajarás sobre un sistema empresarial existente. Tu responsabilidad es analizar, diseñar, implementar, corregir y mejorar funcionalidades del backend respetando estrictamente la arquitectura, convenciones y código existente.

No debes reinventar la arquitectura del proyecto ni introducir tecnologías innecesarias.

---

# STACK DEL PROYECTO

El proyecto utiliza:

* Java 17
* Spring Boot 3.5.4
* Maven
* PostgreSQL
* Spring Data JPA
* Spring Security
* JWT
* Lombok
* ModelMapper
* Jakarta Validation
* OpenPDF
* Apache POI
* Azure Blob Storage

El proyecto se empaqueta actualmente como WAR.

No cambies versiones de Java, Spring Boot o dependencias principales salvo que el usuario lo solicite explícitamente.

---

# ARQUITECTURA

El proyecto utiliza una arquitectura separada por responsabilidades.

Estructura principal:

domain/
application/
infrastructure/

La implementación puede contener estructuras como:

domain/
├── model
├── repository
└── usecase

application/
├── dto
├── service
└── report

infrastructure/
├── controller
├── persistence
├── security
├── report
└── util

Antes de crear cualquier clase nueva:

1. Busca implementaciones similares.
2. Identifica el patrón utilizado.
3. Reutiliza las convenciones existentes.
4. Mantén la separación de responsabilidades.

No introduzcas una arquitectura diferente.

---

# REGLA PRINCIPAL

Antes de modificar código:

1. Analiza los archivos relacionados.
2. Busca funcionalidades similares.
3. Revisa entidades.
4. Revisa DTOs.
5. Revisa repositories.
6. Revisa services/use cases.
7. Revisa controllers.
8. Revisa seguridad.
9. Revisa las relaciones con otros módulos.

Después implementa únicamente los cambios necesarios.

---

# FLUJO DEL BACKEND

Las funcionalidades deben seguir el flujo existente:

Controller
↓
UseCase / Service
↓
Repository
↓
Entity
↓
PostgreSQL

Cuando corresponda:

Request DTO
↓
Controller
↓
UseCase / Service
↓
Entity
↓
Repository

Y para respuestas:

Entity
↓
Response DTO
↓
ApiResponse
↓
Controller

---

# CONTROLLERS

Los Controllers deben ser delgados.

Deben encargarse principalmente de:

* recibir requests
* validar información
* obtener parámetros
* llamar al caso de uso/service
* construir la respuesta

No colocar lógica de negocio compleja dentro del Controller.

Mantener los endpoints REST existentes.

---

# API RESPONSE

El proyecto utiliza:

ApiResponse

y:

ApiResponseFactory

Las respuestas deben mantener la estructura existente:

* status
* success
* message
* data
* errors

No crear formatos de respuesta alternativos si ya existe `ApiResponse`.

---

# EXCEPCIONES

Para errores de negocio utilizar:

BusinessException

Los mensajes deben ser claros y útiles.

No utilizar excepciones genéricas para representar reglas de negocio.

No devolver stack traces al frontend.

---

# VALIDACIÓN

Utilizar Jakarta Validation cuando corresponda:

@NotNull
@NotBlank
@Size
@Positive
@PositiveOrZero
@Valid

La validación debe existir tanto donde corresponda en la API como en las reglas de negocio.

Nunca confiar únicamente en la validación del frontend.

---

# JPA Y POSTGRESQL

El proyecto utiliza Spring Data JPA.

No introducir MongoDB, Couchbase u otra tecnología NoSQL en este proyecto salvo solicitud explícita.

Antes de modificar una Entity:

1. Revisar la tabla.
2. Revisar columnas.
3. Revisar relaciones.
4. Revisar repositories.
5. Revisar consultas existentes.
6. Revisar DTOs.

No modificar nombres de columnas o relaciones sin analizar el impacto.

No utilizar `CascadeType.ALL` automáticamente.

No agregar `EAGER` indiscriminadamente.

---

# TRANSACCIONES

Utilizar `@Transactional` cuando una operación modifique múltiples datos y necesite atomicidad.

No utilizar `@Transactional` indiscriminadamente.

Las operaciones críticas de inventario, órdenes, ventas y caja deben analizarse especialmente.

---

# SEGURIDAD

El proyecto utiliza:

* Spring Security
* JWT
* JwtTokenProvider
* JwtAuthenticationFilter
* CustomUserDetailsService
* SecurityUtil

El login utiliza:

POST /api/login

Para obtener el usuario autenticado utilizar el mecanismo existente:

SecurityUtil.getCurrentUserId()

No desactivar Spring Security para solucionar errores.

No eliminar filtros JWT.

No crear un segundo sistema de autenticación.

---

# PERMISOS

El sistema utiliza permisos como:

VIEW_*

y roles como:

SUPER_ADMIN

Antes de agregar un endpoint protegido:

1. Revisa cómo están protegidos los endpoints existentes.
2. Reutiliza el sistema de permisos.
3. Mantén la consistencia con el resto del proyecto.

---

# SOFT DELETE

El proyecto utiliza eliminación lógica en diferentes módulos.

Antes de implementar DELETE verifica si la entidad utiliza:

deleted

u otro mecanismo existente.

Si una entidad utiliza soft delete:

* no eliminar físicamente el registro
* mantener auditoría
* excluir registros eliminados de las consultas normales

---

# INVENTARIO

Los tipos de movimiento existentes incluyen:

ENTRY
SALE
WASTE
ADJUSTMENT
SALE_RETURN

Regla fundamental:

El stock nunca debe quedar negativo.

Antes de registrar movimientos que disminuyen stock:

1. Obtener stock actual.
2. Validar disponibilidad.
3. Registrar movimiento.
4. Actualizar stock.
5. Mantener la operación transaccional cuando corresponda.

No duplicar lógica de inventario en diferentes servicios.

---

# CAJA

El sistema utiliza:

cash_registers
cash_sessions

Las sesiones pueden manejar información como:

* cashRegisterId
* openedBy
* openedAt
* openingAmount
* closedBy
* closedAt
* expectedAmount
* closingAmount
* difference
* status
* openingComment
* closingComment
* createdBy
* createdAt
* modifiedBy
* modifiedAt
* deleted

Respetar el flujo existente de apertura, consulta, cierre e historial.

No permitir estados inconsistentes.

No permitir múltiples sesiones abiertas para una misma caja si la regla actual establece una sola sesión abierta.

Las operaciones monetarias deben mantener precisión.

---

# ÓRDENES

Los endpoints actuales incluyen:

POST   /api/orders
GET    /api/orders
GET    /api/orders/{id}
PUT    /api/orders/{id}
PUT    /api/orders/{id}/status
DELETE /api/orders/{id}

Antes de modificar órdenes analiza su relación con:

* productos
* detalle de orden
* stock
* inventario
* pagos
* ventas
* estados

No asumas que crear una orden equivale automáticamente a realizar una venta.

Respeta el flujo de negocio existente.

---

# REPORTES

El proyecto utiliza:

* OpenPDF
* Apache POI

Reutiliza estas tecnologías para PDF y Excel cuando corresponda.

No introduzcas otra librería de reportes sin una razón explícita.

---

# ARCHIVOS E IMÁGENES

El proyecto utiliza Azure Blob Storage mediante:

azure-storage-blob

Las operaciones de almacenamiento deben permanecer en infraestructura.

No realizar llamadas a Azure directamente desde Controllers.

---

# DTOs

Preferir:

Request DTO
Response DTO
Entity

No exponer directamente entidades JPA desde los Controllers cuando el proyecto ya utiliza DTOs.

Utilizar ModelMapper cuando corresponda al patrón existente.

No crear conversiones duplicadas innecesariamente.

---

# CÓDIGO

Cuando implementes una funcionalidad:

* entrega código funcional
* no utilices pseudocódigo
* no utilices `...`
* no dejes métodos incompletos
* no inventes clases que ya existen
* no dupliques servicios
* no cambies nombres existentes sin necesidad

Si necesitas modificar un archivo, conserva todo el código existente que siga siendo válido.

---

# CAMBIOS MÍNIMOS

Modifica únicamente los archivos necesarios.

No reformatees todo el proyecto.

No cambies dependencias del `pom.xml` sin necesidad.

No actualices Spring Boot.

No cambies Java.

No cambies la arquitectura.

No elimines funcionalidades existentes para implementar una nueva.

---

# ANÁLISIS ANTES DE IMPLEMENTAR

Para cualquier tarea sigue:

ANALIZAR
↓
BUSCAR IMPLEMENTACIÓN SIMILAR
↓
DETERMINAR ARCHIVOS AFECTADOS
↓
IMPLEMENTAR
↓
COMPILAR
↓
VALIDAR

---

# VALIDACIÓN

Cuando sea posible ejecuta:

mvn clean test

o:

mvn clean package

Si modificaste código relacionado con una funcionalidad específica, ejecuta las pruebas correspondientes.

Nunca afirmes que ejecutaste un comando si realmente no lo ejecutaste.

Si existe un error de compilación:

1. identifica la causa
2. corrígela
3. vuelve a validar

---

# RESPUESTA DE CODEX

Cuando termines una tarea informa:

## Cambios realizados

Lista de archivos modificados.

## Implementación

Explica brevemente qué se hizo.

## Validación

Indica qué comandos se ejecutaron y su resultado.

## Observaciones

Indica posibles riesgos o decisiones importantes.

---

# PRINCIPIO FINAL

La prioridad es:

CONSISTENCIA > COMPLEJIDAD

REUTILIZACIÓN > DUPLICACIÓN

SIMPLICIDAD > SOBREINGENIERÍA

CÓDIGO EXISTENTE > SOLUCIÓN GENÉRICA

Nunca reinventes el proyecto.

La base de datos se define en `database/bd_schema.sql`: es el script completo listo para restablecer el sistema desde cero; toda modificación de tablas, índices o datos iniciales debe incorporarse allí, incluidas las tablas nuevas.

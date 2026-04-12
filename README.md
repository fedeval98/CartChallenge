# Cart Challenge API

API REST desarrollada con Spring Boot para simular un sistema de procesamiento de pedidos con autenticación básica, persistencia en H2 y procesamiento asíncrono de órdenes.

## Tecnologías usadas

- Java 21 o superior
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- H2 Database
- Lombok
- JUnit 5
- Mockito

## Requisitos previos

Tener instalado:

- Java 21 o superior
- Gradle

## Cómo levantar el proyecto

### 1. Clonar el repositorio

```bash
git clone https://github.com/fedeval98/CartChallenge.git
cd CartChallenge
```

### 2. Ejecutar la aplicación

En Windows:

```bash
gradlew bootRun
```

En Linux / Mac:

```bash
./gradlew bootRun
```

La aplicación quedará disponible en:

http://localhost:8080

## Autenticación

Todos los endpoints requieren **Basic Auth**.

Usar email y password de un cliente precargado en la base de datos.

## Base de datos H2

Acceso:

http://localhost:8080/h2-console

Configuración típica:

- JDBC URL: jdbc:h2:mem:cartdb
- User: sa
- Password: (vacío)

## Endpoints principales

### Crear carrito
POST /api/cart/createcart

### Agregar producto
POST /api/cart/addproduct

### Eliminar producto
DELETE /api/cart/deleteproductfrom/{cartCode}/products/{productCode}

### Listar productos
GET /api/cart/{cartCode}/products

### Procesar orden
POST /api/cart/processOrder/{cartCode}

### Listar carritos
GET /api/cart/mycarts

## Tests

Ejecutar:

gradlew test

## Notas

- Autenticación con Basic Auth
- Base de datos en memoria
- Procesamiento asincrónico de órdenes

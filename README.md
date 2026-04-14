# Cart Challenge API

API REST desarrollada con Spring Boot para simular un sistema de procesamiento de pedidos con autenticación básica, persistencia en H2 y procesamiento asíncrono de órdenes.

## Tecnologías usadas
### 🧠 Core
![Static Badge](https://img.shields.io/badge/Java-21%2B-blue) ![Static Badge](https://img.shields.io/badge/Spring_Boot-4.0.5-green)

### 🌐 API y Seguridad
![Static Badge](https://img.shields.io/badge/Spring_Web-REST-green) ![Static Badge](https://img.shields.io/badge/Spring_Security-Basic_Auth-red)

### 🗄️ Manejo de datos
![Static Badge](https://img.shields.io/badge/Spring_Data_JPA-Hibernate-orange) ![Static Badge](https://img.shields.io/badge/H2-InMemory_DB-lightgrey)

### 🛠️ Herramientas
![Static Badge](https://img.shields.io/badge/Lombok-Annotations-pink)

### 🧪 Testing
![Static Badge](https://img.shields.io/badge/JUnit-5-red) ![Static Badge](https://img.shields.io/badge/Mockito-Mocking-blueviolet)

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

Usar email y password de un cliente precargado en la base de datos.[(Consultar Wiki)](https://github.com/fedeval98/CartChallenge/wiki/Endpoints#autenticaci%C3%B3n)

## Base de datos H2

Acceso:

http://localhost:8080/h2-console

Configuración típica:

- JDBC URL: jdbc:h2:mem:cartdb
- User: sa
- Password: (vacío)

---

## Endpoints principales - [Consultar wiki](https://github.com/fedeval98/CartChallenge/wiki/Endpoints)

---

## Informe de Observabilidad y pruebas de Estres - [Consultar wiki](https://github.com/fedeval98/CartChallenge/wiki/Observabilidad-y-pruebas-de-estr%C3%A9s)

---

## Tests

Ejecutar:

gradlew test

[Consultar Coverage de Tests](https://github.com/fedeval98/CartChallenge/wiki/Tets-Coverage)


## Notas

- Autenticación con Basic Auth
- Base de datos en memoria
- Procesamiento asincrónico de órdenes

This repository contains a multi-module Java backend and a JavaScript frontend for the **Big Book Database**. The Java backend is organised into Maven modules under a parent POM:
- **commons** — shared domain model, DAO, utilities and configuration.
- **mvc** — Tomcat 10 MVC web application (builds to a WAR).
- **rest** — RESTful service (Spring Boot runnable or WAR deployable depending on packaging).
- **frontend** — Node-based JavaScript client (development server and production build).

The project was developed in **IntelliJ IDEA**. The parent POM can build all modules together.

## Prerequisites
- **JDK 17+**
- **Maven 3.6+**
- **Node 16+** and **npm** (for the frontend)
- **Tomcat 10** (for deploying the MVC WAR)
- **MySQL / MariaDB** instance reachable by the services (credentials configured in `commons` DatabaseConfiguration)

- Optional: **Docker** / **docker-compose** for containerised runs

## Build (Maven)
From the repository root use the parent POM to build all modules:
bash
```
# Clean and package all modules (commons, mvc, rest)
mvn clean package
```

- The **mvc** module produces a WAR file at `mvc/target/*.war`.
- The **commons** module produces a JAR used by both services.
- The **rest** module packaging depends on its POM:
    - Spring Boot packaging → executable JAR
    - Servlet packaging → WAR

## Deploying the MVC (Tomcat 10)
1. Build the project:
bash
```
mvn clean package
```

2. Copy the WAR to Tomcat’s `webapps` directory:
bash
```
cp mvc/target/<your-mvc-artifact>.war $TOMCAT_HOME/webapps/
```

3. Start or restart Tomcat.
4. Access the MVC app at the configured context path, e.g.:
Code
```
http://localhost:8080/<context>/
```

**Notes**
- The WAR target is located under `mvc/target/`.
- Tomcat logs are typically available at `$TOMCAT_HOME/logs/catalina.out`.

## Running the REST backend
### If Spring Boot based:
From the `rest` module directory:
bash
```
mvn spring-boot:run
```

### If packaged as a WAR:
Deploy it to Tomcat the same way as the MVC WAR.

### Configuration
- Database connection details come from `commons` `DatabaseConfiguration`.
- Update JDBC URL, username, password for local testing.
- Ensure REST service port does not conflict with Tomcat.

## Running the frontend
From the `frontend` directory:
bash
```
# Install dependencies (first time)
npm install

# Run development server (hot reload)
npm run dev

# Build production bundle
npm run build
```

- Dev server port is defined in `package.json`.

## Behavior
- The frontend supports **content negotiation** (JSON, XML, YAML).
- Preferences are stored in browser cookies.
- By default, the client fetches `GET /books` on startup.

## Development notes (IntelliJ)
- Open the parent POM in IntelliJ as a Maven project.
- Use run configurations for:
    - `mvn spring-boot:run` (REST)
    - frontend dev server

## Testing and API verification
Use curl or Postman:
Code
```
GET /books
GET /books?id=123
GET /books?page=X&size=Y
POST /books
PUT /books
DELETE /books?id=123
```

- Test content negotiation with `Accept: application/json`, `application/xml`, `application/x-yaml`.
- Use browser DevTools Network tab for reviewing payloads.
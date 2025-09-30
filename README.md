# Ejecucion Local

## Requisitios

1.Java 17

2.Maven

3.docker-compose

## Ejecucion

### 1. Build
```bash
mvn clean package -DskipTests
```

### 2. Ejecutar docker-compose

#### Linux
```bash
SPRING_PROFILE=local docker-compose up --build
```

#### Windows(powershell)
```bash
$env:SPRING_PROFILE="local";docker-compose up --build
```

Endpoint: http://localhost:8080
# 🚀 Microservices Backend (Java/Spring)

This repository contains a small microservices system wired through an API Gateway. Each service runs as a Spring Boot application with H2 in-memory databases for local development.

## 🧩 Services and Ports
- 🧭 Service Registry (Eureka): `service-registry` → `http://localhost:8761`
- 🛡️ API Gateway (Spring Cloud Gateway MVC): `api-gateway` → `http://localhost:8080`
- 📦 Product Service: `product-service` → `http://localhost:8081`
- 🧾 Order Service: `order-service` → `http://localhost:8082`
- 👤 User/Auth Service: `user-service` → `http://localhost:8083`
- ⚙️ Config Server: `config-server` (disabled by default: `spring.cloud.config.enabled: false`)

## 📋 Prerequisites
- ☕ Java 17+
- 🧰 Maven 3.9+
- 🐳 (Optional) Docker for Kafka if you want to run Kafka locally

## ▶️ Running Locally
You can run in two modes. The repository is currently configured to use Direct URIs in the gateway for simplicity.

### A) 🔗 Direct-URI Mode (no Eureka)
1) Start services in any order:
   - `product-service` (8081)
   - `order-service` (8082)
   - `user-service` (8083)
2) Start `api-gateway` (8080)
3) Visit `http://localhost:8080/...`

Gateway routes are configured in `api-gateway/src/main/resources/application.yml`:
```yaml
spring:
  cloud:
    gateway:
      server:
        webmvc:
          routes:
            - id: product-service
              uri: http://localhost:8081
              predicates:
                - Path=/products/**
            - id: order-service
              uri: http://localhost:8082
              predicates:
                - Path=/orders/**
            - id: user-service
              uri: http://localhost:8083
              predicates:
                - Path=/auth/**
```

### B) 🛰️ Eureka Mode (load-balanced)
1) Start `service-registry` (8761)
2) Revert gateway URIs to `lb://product-service`, `lb://order-service`, `lb://user-service`
3) Start `product-service`, `order-service`, `user-service` (they register with Eureka)
4) Start `api-gateway`

## 🔐 Security and Auth
The gateway enforces JWT-based authentication for most routes, with the following exceptions configured in `api-gateway` `SecurityConfig`:
- ✅ Permit all: `/auth/**`
- ✅ Permit all: GET `/products/**`, GET `/orders/**` (for easier development)
- 🔒 Everything else: requires `Authorization: Bearer <token>`

Auth endpoints (proxied via gateway to `user-service`):
- `POST /auth/register` → body: `{ name, email, password, role }`, role is `USER` or `ADMIN`
- `POST /auth/login` → body: `{ email, password }` returns a JWT string

Include the token in calls to protected endpoints:
```
Authorization: Bearer <your_jwt>
```

## 📚 Core Endpoints
Product Service (via Gateway):
- `GET /products` → list products
- `GET /products/{id}` → product details
- `POST /products` → create product (requires JWT)
- `PUT /products/{id}` / `DELETE /products/{id}` → update/delete (requires JWT)

Order Service (via Gateway):
- `GET /orders` → list orders
- `GET /orders/{id}` → order details
- `POST /orders` → create order (requires JWT)
  - Body shape: `{ "orderItems": [{ "productId": number, "quantity": number }] }`

User Service (via Gateway):
- `POST /auth/register`
- `POST /auth/login`

## 📡 Kafka (Stock Updates)
- Orders publish stock update events after creation.
- Product Service consumes and updates stock levels.
- If you see errors like "Product not found with id X" in `product-service`, create products first before placing orders.

## 🗄️ Local Databases
- Each service uses H2 in-memory DB by default (`ddl-auto: update`). Data resets on service restart.

## 🧯 Troubleshooting
- 503 "Unable to find instance for ...":
  - In Direct-URI mode: ensure target service is up (8081/8082/8083).
  - In Eureka mode: ensure `service-registry` is running and services have registered.
- 401/403 from gateway:
  - Obtain a JWT via `POST /auth/login` and include `Authorization: Bearer <token>`.
  - For quick dev, GET `/products/**` and GET `/orders/**` are already public.
- Kafka listener errors in `product-service` about missing product IDs:
  - Create product(s) first; then place orders using those IDs.

## 🛠️ Build
Run each service:
```
cd <service-folder>
./mvnw spring-boot:run
```
Or build jars:
```
./mvnw -DskipTests package
```

---
For the frontend, see `frontend/` (React + Vite) – not covered in this README.

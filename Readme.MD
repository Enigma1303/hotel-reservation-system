# Hotel Reservation System — Microservices Architecture

A production-ready hotel reservation system built with Spring Boot microservices, event-driven communication via Apache Kafka, and supporting infrastructure including Eureka, Config Server, API Gateway, and Zipkin distributed tracing.

---

## Architecture Overview

```
                          ┌─────────────────┐
                          │     Client      │
                          └────────┬────────┘
                                   │
                          ┌────────▼────────┐
                          │   API Gateway   │ :8080
                          │ (Spring Cloud)  │
                          └────────┬────────┘
                                   │ REST (lb://)
            ┌──────────────────────┼──────────────────────┐
            │                      │                      │
   ┌────────▼────────┐  ┌─────────▼───────┐  ┌──────────▼──────┐
   │ Reservation     │  │  Room-Service   │  │Customer-Service │
   │ Service  :8083  │  │    :8082        │  │    :8081        │
   └────────┬────────┘  └─────────────────┘  └─────────────────┘
            │                      │ Circuit Breaker (Resilience4j)
            │ Feign + Circuit       └──────────────────────┘
            │ Breaker
            │
     ┌──────▼──────────────────────────────────┐
     │              Apache Kafka               │
     │   reservation-events | payment-events   │
     └──────┬──────────────────────────────────┘
            │                            |
     ┌──────▼──────┐              ┌──────────────────┐
     │   Payment   │              │  Notification    │
     │   Service   │              │    Service       │
     │   :8084     │              │    :8085         │
     └─────────────┘              └──────────────────┘

Supporting Infrastructure:
  Eureka Server     :8761   (Service Discovery)
  Config Server     :8888   (Centralized Config via GitHub)
  Zipkin            :9411   (Distributed Tracing)
  MySQL x5          :3306-3310
  Kafka (KRaft)     :9092/:9094
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.x |
| Service Discovery | Spring Cloud Netflix Eureka |
| API Gateway | Spring Cloud Gateway |
| Config Management | Spring Cloud Config Server (Git backend) |
| Messaging | Apache Kafka (KRaft mode) |
| Resilience | Resilience4j Circuit Breaker |
| Inter-service calls | OpenFeign |
| Distributed Tracing | Micrometer + Zipkin |
| Database | MySQL 8.0 (one per service) |
| ORM | Spring Data JPA / Hibernate |
| Containerization | Docker + Docker Compose |
| Email | Spring Mail (Gmail SMTP) |

---

## Services

| Service | Port | Responsibility |
|---|---|---|
| Eureka Server | 8761 | Service registry and discovery |
| Config Server | 8888 | Centralized configuration via GitHub |
| API Gateway | 8080 | Single entry point, routing, load balancing |
| Customer-Service | 8081 | Customer management |
| Room-Service | 8082 | Room inventory and availability |
| Reservation-Service | 8083 | Reservation lifecycle management |
| Payment-Service | 8084 | Simulated payment processing |
| Notification-Service | 8085 | Email notifications via Kafka events |

---

## Prerequisites

- Docker and Docker Compose
- Java 21
- Maven 3.9+

---

## Setup and Running

### — Docker

Run the entire system with a single command:

```bash
docker-compose up --build
```

Wait 2-3 minutes for all services to start. Check Eureka dashboard to confirm all services are registered:
```
http://localhost:8761
```


## Seed Data

After services start, insert test data via DBeaver or MySQL client:

**customer_db:**
```sql
INSERT INTO customers (name, email, created_at, updated_at) VALUES
('John Doe', 'john@example.com', NOW(), NOW()),
('Jane Doe', 'jane@example.com', NOW(), NOW());
```

**room_db:**
```sql
INSERT INTO rooms (type, availability, price, created_at, updated_at) VALUES
('SINGLE', true, 1000, NOW(), NOW()),
('DOUBLE', true, 2000, NOW(), NOW()),
('DELUXE', true, 3000, NOW(), NOW()),
('SINGLE', true, 1000, NOW(), NOW()),
('DOUBLE', true, 2000, NOW(), NOW());
```

---

## API Documentation

All requests go through the API Gateway at `http://localhost:8080`.

### Customer APIs

**Create Customer**
```
POST /api/customers
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com"
}

Response 201:
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com"
}
```

**Get Customer**
```
GET /api/customers/{id}

Response 200:
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com"
}
```

---

### Room APIs

**Get All Rooms**
```
GET /api/rooms

Response 200:
[
  {
    "id": 1,
    "type": "DELUXE",
    "availability": true,
    "price": 3000.0
  }
]
```

**Get Room by ID**
```
GET /api/rooms/{id}
```

**Update Room Availability**
```
PATCH /api/rooms/{id}
Content-Type: application/json

{
  "availability": false
}
```

---

### Reservation APIs

**Create Reservation**
```
POST /api/reservations
Content-Type: application/json

{
  "customerId": 1,
  "roomId": 2
}

Response 201:
{
  "id": 1,
  "customerId": 1,
  "roomId": 2,
  "status": "PENDING",
  "createdAt": "2026-04-06T10:00:00"
}
```

Status transitions automatically via Kafka:
- Payment success → `CONFIRMED`
- Payment failure → `FAILED`

**Get Reservation**
```
GET /api/reservations/{id}
```

**Get All Reservations**
```
GET /api/reservations
```

**Get Reservation Details** *(aggregated from 3 services)*
```
GET /api/reservations/{id}/details

Response 200:
{
  "reservationId": 1,
  "status": "CONFIRMED",
  "customerName": "John Doe",
  "roomId": 2,
  "roomType": "DELUXE",
  "paymentStatus": "SUCCESS",
  "createdAt": "2026-04-06T10:00:00"
}
```
---

### Payment APIs

**Get Payment by ID**
```
GET /api/payments/{id}

Response 200:
{
  "id": 1,
  "reservationId": 1,
  "status": "SUCCESS"
}
```

**Get Payment by Reservation**
```
GET /api/payments/reservation/{reservationId}
```

**Simulate Payment Failure** *(testing only)*
```
POST /api/payments/simulate-failure

Response 200:
{
  "message": "Payment-Service will fail for next 10 requests"
}
```

Use this to demonstrate the circuit breaker CLOSED → OPEN → HALF-OPEN → CLOSED cycle.

---

### Notification APIs

**Get All Notifications**
```
GET /api/notifications

Response 200:
[
  {
    "id": 1,
    "message": "...",
    "type": "EMAIL",
    "status": "SENT",
    "retryCount": 0
  }
]
```

**Retry Failed Notification** *(admin only)*
```
POST /api/notifications/{id}/retry

Response 200:
{
  "id": 1,
  "status": "SENT",
  "retryCount": 1
}
```

---

## Event Flow

```
1. POST /api/reservations
        │
        ▼
2. Reservation-Service validates customer + room via REST (Feign + Circuit Breaker)
        │
        ▼
3. Reservation saved with status PENDING
        │
        ▼
4. ReservationCreatedEvent published to Kafka (reservation-events topic)
        │
        ├──────────────────────────────────────┐
        ▼                                      ▼
5. Payment-Service consumes event        Notification-Service consumes event
   Simulates payment processing          Sends "Reservation Received" email
        │
        ▼
6. PaymentCompletedEvent published to Kafka (payment-events topic)
        │
        ├──────────────────────────────────────┐
        ▼                                      ▼
7. Reservation-Service consumes event    Notification-Service consumes event
   Updates status → CONFIRMED/FAILED     Sends payment success/failure email
        │
        ▼
8. On FAILED: Room availability restored via REST call to Room-Service
```

---

## Resilience Features

### Circuit Breaker

Circuit breakers protect all synchronous REST calls from Reservation-Service:
- Reservation-Service → Customer-Service
- Reservation-Service → Room-Service
- Reservation-Service → Payment-Service

**Configuration:**
- Sliding window: 5 calls
- Failure threshold: 50%
- Wait in OPEN state: 10 seconds
- Half-open test calls: 2

**Check circuit breaker state:**
```
GET http://localhost:8083/actuator/health
```

**Demonstrate full cycle:**
1. Call `POST /api/payments/simulate-failure`
2. Make 3+ reservation requests → circuit OPENS
3. Wait 10 seconds → circuit goes HALF-OPEN
4. Make successful requests → circuit CLOSES

### Notification Retry

Failed notifications are automatically retried every 60 seconds up to 3 attempts. After max attempts, admin is alerted and manual retry is available via the retry API.

### Kafka Fault Tolerance

If a consuming service is down, events remain in Kafka and are processed when the service recovers. Each service uses a `processed_events` table to prevent duplicate processing on Kafka redelivery.

---

## Distributed Tracing

Open Zipkin to trace requests across services:
```
http://localhost:9411
```

Click **Run Query** to see traces. Click any trace to see the full request journey across services including Kafka event flow.

---

## Load Balancing Demo

Room-Service supports multiple instances. To demonstrate:

**Start a second instance locally:**
```bash
cd room-service
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8086
```

Both instances register with Eureka under `room-service`. The Gateway load balances between them automatically.

**Demonstrate instance failure:**
```bash
# Stop the second instance
lsof -ti:8086 | xargs kill
```

Wait 30 seconds — Eureka marks the instance as DOWN. Requests continue routing to the healthy instance with no downtime.

---

## Monitoring

| Endpoint | Description |
|---|---|
| `http://localhost:8761` | Eureka dashboard — all registered services |
| `http://localhost:9411` | Zipkin — distributed traces |
| `http://localhost:8888/reservation-service/default` | Config Server — service config |
| `http://localhost:8083/actuator/health` | Reservation-Service health + circuit breaker states |

---

## Design Decisions

- **Database per service** — each service owns its data, no shared databases
- **Kafka over REST for async flows** — payment and notifications are eventual, decoupled from reservation creation
- **Feign + Circuit Breaker** — synchronous calls protected against cascading failures
- **Idempotent consumers** — `processed_events` table in each Kafka consumer prevents duplicate processing
- **Simulated payments** — no external payment gateway, 70% success rate by default
- **Email notifications** — sent via Gmail SMTP, logged to console as formatted templates
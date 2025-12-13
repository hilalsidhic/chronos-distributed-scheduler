# ⏳ Chronos — Distributed Job Scheduler

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-green)
![React](https://img.shields.io/badge/React-18-blue)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)
![License](https://img.shields.io/badge/License-MIT-purple)

**Chronos** is a **high-throughput, fault-tolerant distributed job scheduler** built to reliably execute millions of jobs across a cluster of workers.

It is designed as a **production-grade system**, focusing on **correctness, scalability, and observability**, **PostgreSQL row-level locking** for exactly-once execution, and a **React-based dashboard** for real-time monitoring.

> Think of Chronos as a modern alternative to Quartz — built for cloud-native, horizontally scalable systems.

---

## ✨ Highlights

- **Distributed by Design** — Separate Scheduler and Worker services for clean scaling
- **Exactly-Once Execution** — Postgres `FOR UPDATE SKIP LOCKED` prevents duplicate processing
- **Automatic Retries** — Exponential backoff with configurable retry limits
- **Observable by Default** — Prometheus metrics + Grafana dashboards included
- **Production-Ready UI** — React dashboard for job creation & execution tracking


   Read More at [Design Docs](https://github.com/hilalsidhic/chronos-distributed-scheduler/blob/master/docs/Chronos_%20Job%20Scheduler%20System.pdf)

---

## 🏗️ Architecture Overview

Chronos is composed of **loosely coupled microservices**, orchestrated via Docker Compose.

### Core Services

1. **Chronos Gateway**
   - Spring Cloud Gateway
   - Single entry point for APIs
   - Handles routing & authentication

2. **Chronos Scheduler**
   - Polls the database for due jobs
   - Applies locking & dispatch logic
   - Pushes jobs into the execution queue

3. **Chronos Worker**
   - Executes HTTP-based job payloads
   - Horizontally scalable
   - Implements retry & timeout handling

4. **Chronos Frontend**
   - React + TypeScript + Tailwind
   - Job management & execution history

### Observability Stack

- **Prometheus** — Metrics scraping
- **Grafana** — Pre-provisioned dashboards
- **JVM Metrics** — memory, GC, CPU

---

## 🧱 Tech Stack

### Backend
- Java 21
- Spring Boot 3
- Gradle
- PostgreSQL 15

### Frontend
- React 18
- TypeScript
- TailwindCSS
- shadcn/ui

### Infrastructure
- Docker & Docker Compose
- Nginx (Reverse Proxy / Load Balancer)
- Prometheus & Grafana

---

## 📂 Project Structure

```
chronos/
├── Chronos_Gateway/            # API Gateway & Auth
├── Chronos_Scheduler/          # Job polling & dispatch logic
├── Chronos_Worker/             # Job execution engine
├── chronos-job-dashboard-main/ # React frontend
├── grafana/                    # Dashboards & datasources
├── nginx/                      # Reverse proxy config
├── docker-compose.yml          # Base configuration
├── docker-compose.override.yml # Local development overrides
└── docker-compose.prod.yml     # Production configuration
```

---

## 🛠️ Getting Started (Local Development)

### Prerequisites

- Docker & Docker Compose
- Java 21 *(optional — only if running services outside Docker)*

### Run the Entire Stack

Chronos is designed to start with **one command**.

```bash
git clone https://github.com/yourusername/chronos.git
cd chronos

docker-compose up --build
```

`docker-compose.override.yml` is automatically applied for local development and exposes all required ports.

---

## 🌐 Accessing Services

| Service      | URL                         | Credentials |
|-------------|-----------------------------|-------------|
| Dashboard   | http://localhost:8080       | N/A |
| Grafana     | http://localhost:3000       | admin / admin |
| Prometheus  | http://localhost:9090       | N/A |
| PostgreSQL  | localhost:5432              | chronos / chronos123 |

---

## ☁️ Production Deployment

For production environments (AWS, GCP, DigitalOcean, etc.), use the production override file.

```bash
docker-compose   -f docker-compose.yml   -f docker-compose.prod.yml   up -d --build
```

### Notes
- Internal service ports are hidden
- Frontend is served via **Nginx (Port 80)**
- Update `VITE_API_URL` in `docker-compose.prod.yml` before building

---

## 🔌 API Usage

Jobs can be scheduled programmatically via REST APIs.

### Create a Recurring Job

```bash
curl -X POST http://localhost:8080/scheduler/jobs   -H "Content-Type: application/json"   -H "Authorization: Bearer <your-token>"   -d '{
    "name": "Health Check",
    "isRecurring": true,
    "intervalSeconds": 60,
    "maxRetries": 3,
    "payload": {
      "url": "https://httpbin.org/post",
      "method": "POST",
      "timeoutMs": 5000,
      "headers": {
        "User-Agent": "Chronos"
      },
      "body": {
        "status": "alive"
      }
    }
  }'
```

### Create a One-Time Job

```json
{
  "name": "Email Reminder",
  "isRecurring": false,
  "nextExecutionTime": "2025-12-25T09:00:00Z",
  "payload": {
    "url": "https://api.emailservice.com/send",
    "method": "POST",
    "body": {
      "to": "user@example.com"
    }
  }
}
```

---

## 📊 Monitoring & Metrics

Chronos ships with **auto-provisioned Grafana dashboards**.

### How to View
1. Open Grafana → http://localhost:3000
2. Navigate to **Dashboards**
3. Select **Chronos – Production Overview**

### Metrics Tracked
- Job Lag (Scheduled vs Actual execution time)
- Queue Depth
- Throughput (Jobs/sec, Failures/sec)
- Virtual Thread saturation & park time
- JVM memory, GC, CPU

---

## 🤝 Contributing

Contributions are welcome!

1. Fork the repository
2. Create your feature branch  
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. Commit your changes  
   ```bash
   git commit -m "Add amazing feature"
   ```
4. Push to the branch  
   ```bash
   git push origin feature/amazing-feature
   ```
5. Open a Pull Request

---

## 📄 License

This project is licensed under the **MIT License**.  
See the `LICENSE` file for details.

---

⭐ If this project helped you understand distributed systems better, consider giving it a star!
   

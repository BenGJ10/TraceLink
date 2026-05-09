<div align="center">

# 🔗 TraceLink

### Advanced URL Shortening, Analytics & Developer Platform

A full-stack, production-ready **URL shortening and analytics platform** designed with a focus on enterprise-grade security, comprehensive developer tooling, and modern DevOps deployment practices.

[![Java](https://img.shields.io/badge/Java-21_LTS-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2+-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.3-61DAFB?style=flat-square&logo=react&logoColor=black)](https://reactjs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1?style=flat-square&logo=mysql&logoColor=white)](https://mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=flat-square&logo=docker&logoColor=white)](https://docker.com/)
[![AWS](https://img.shields.io/badge/AWS-Deployed-FF9900?style=flat-square&logo=amazonaws&logoColor=white)](https://aws.amazon.com/)

[Features](#features) · [Architecture](#architecture) · [API Reference](#api-reference) · [Getting Started](#getting-started) · [Deployment](#deployment)

</div>

---

## Overview

**TraceLink** is a scalable, cloud-native URL management platform built with a **Spring Boot** backend and a **React** frontend. It demonstrates end-to-end enterprise development and DevOps practices, including dual authentication (JWT + BCrypt-hashed API keys), real-time traffic analytics, and a fully automated AWS infrastructure deployment via Docker, EKS, RDS, S3, and CodePipeline.

---

## Features

### Link Management & Analytics
- **Custom URL Shortening:** Generate concise, shareable links with custom aliases.
- **Dynamic QR Codes:** Automatically generate downloadable QR codes (SVG/PNG) for every link.
- **Real-Time Analytics:** Track total clicks, referrers, and daily traffic trends.
- **Device & Geography Tracking:** Advanced User-Agent parsing to track visitor OS, Browser, and Device types.

### Developer API Platform
- **Programmatic Access:** Comprehensive REST API for creating and managing links.
- **Secure API Keys:** Generate, mask, and manage API keys via a dedicated developer dashboard.
- **Enterprise Security:** API keys are secured using an `O(1)` deterministic prefix lookup paired with rigorous **BCrypt hashing**.
- **Bearer Authentication:** Seamless integration for developers using standard `Authorization: Bearer <API_KEY>` headers.

### Authentication & User Security
- **Stateless JWT Authentication** alongside Developer API Keys.
- **Role-Based Access Control (RBAC)** to isolate user data.
- **User Dashboard:** Update profiles, manage URLs, and securely revoke active sessions or API keys.

---

## Architecture

### System Overview

```mermaid
graph TB
    subgraph Clients["Client Layer"]
        A1[React Frontend UI]
        A2[3rd Party Apps / CLI]
    end

    subgraph Security["Security Layer"]
        B1[CORS & Rate Limiting]
        B2[JWT Auth Filter]
        B3[API Key Hash Validator]
    end

    subgraph Controllers["Controller Layer"]
        C1[AuthController]
        C2[UrlMappingController]
        C3[ApiKeyController]
        C4[AnalyticsController]
    end

    subgraph Services["Business Logic Layer"]
        D1[UserService]
        D2[UrlService]
        D3[ApiKeyService]
        D4[AnalyticsService]
    end

    subgraph Persistence["Data Access Layer"]
        E1[(Amazon RDS MySQL)]
    end

    A1 -->|JWT| B1
    A2 -->|API Key| B1
    B1 --> B2 & B3
    B2 & B3 --> C1 & C2 & C3 & C4
    C1 --> D1
    C2 --> D2
    C3 --> D3
    C4 --> D4
    D1 & D2 & D3 & D4 --> E1
```

---

### AWS Infrastructure Pipeline

```mermaid
graph TB
    subgraph CI/CD ["AWS CodePipeline"]
        GH[GitHub Source] --> CB[AWS CodeBuild]
        CB -->|Docker Push| ECR[Amazon ECR]
    end

    subgraph AWS ["AWS Cloud Infrastructure"]
        subgraph Edge ["Edge Delivery"]
            CF[CloudFront CDN]
            S3[S3 Bucket Static React]
        end

        subgraph Compute ["Compute — EKS Cluster"]
            EKS[Amazon EKS LoadBalancer]
            Pods[Spring Boot Pods]
        end

        subgraph Database ["Persistence — RDS"]
            RDS[(Amazon RDS MySQL 8)]
        end
    end

    Users -->|tracelink.domain.in| CF
    CF --> S3
    
    Users/Devs -->|api.tracelink.domain.in| EKS
    ECR -.->|Pulls Image| Pods
    EKS --> Pods
    Pods -->|JDBC| RDS
```

---

## Technology Stack

### Backend
- **Core:** Java 21 LTS, Spring Boot 3
- **Security:** Spring Security, JWT (JJWT), BCrypt
- **Persistence:** Spring Data JPA, Hibernate, MySQL 8
- **Tools:** Maven, Lombok, Yauaa (User-Agent parsing)

### Frontend
- **Core:** React 18, Vite
- **Routing & State:** React Router DOM, React Context
- **Styling:** Vanilla CSS, Framer Motion (Animations)
- **Data Visualization:** Recharts
- **Forms & Validation:** React Hook Form, React Hot Toast

### DevOps & Infrastructure
- **Containerization:** Docker (Multi-stage builds)
- **AWS Compute:** Amazon EKS, ECR
- **AWS Data & Edge:** Amazon RDS, S3, CloudFront
- **CI/CD:** AWS CodePipeline, CodeBuild

---

## API Reference

**Base URL:** `https://api.tracelink.yourdomain.com/api`

### Links & Redirects
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/urls/shorten` | Create a new short URL | JWT / API Key |
| `GET`  | `/urls/myurls` | List all URLs for the user | JWT / API Key |
| `GET`  | `/{shortUrl}` | Redirect to original URL | None |
| `GET`  | `/url/qr/{shortUrl}` | Generate QR code (SVG/PNG) | None |

### Analytics
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `GET`  | `/analytics/{shortUrl}` | Get detailed analytics for a URL | JWT / API Key |
| `GET`  | `/analytics/total` | Get aggregated account analytics | JWT / API Key |

### Developer Platform
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/keys` | Generate a new developer API key | JWT |
| `GET`  | `/keys` | List active API keys | JWT |
| `DELETE`| `/keys/{id}` | Revoke an API key | JWT |

---

## Getting Started

### Local Development Setup

**1. Clone the repository**
```bash
git clone https://github.com/BenGJ10/TraceLink.git
cd TraceLink
```

**2. Database Configuration**
Create a MySQL database named `url_shortner_db`.
Copy the local environment profile and configure your database credentials:
```bash
cp src/main/resources/application-dev.properties.example src/main/resources/application-dev.properties
```

**3. Run the Backend**
```bash
export SPRING_PROFILES_ACTIVE=dev
./mvnw spring-boot:run
```
*Backend runs on `http://localhost:8080`*

**4. Run the Frontend**
```bash
cd frontend
cp .env.local.example .env.local # Ensure VITE_API_BASE_URL=http://localhost:8080
npm install
npm run dev
```
*Frontend runs on `http://localhost:5173`*

---

## Docker & Cloud Deployment

### Dockerized Local Execution
TraceLink uses a heavily optimized, multi-stage `Dockerfile`. You can run the entire stack locally via Docker:

```bash
docker build -t tracelink-backend .

docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:mysql://host.docker.internal:3306/url_shortner_db \
  -e DB_USER=your_user \
  -e DB_PASS=your_password \
  -e JWT_SECRET=your_256_bit_secret \
  tracelink-backend
```

### AWS Production Deployment
TraceLink is designed to be deployed to an enterprise-grade AWS environment:

1. **Frontend:** Built via CodeBuild and synced to an **S3 Bucket**, served globally via **CloudFront**.

2. **Backend:** Packaged into a Docker container, pushed to **Amazon ECR**, and orchestrated via **Amazon EKS** behind an AWS LoadBalancer.

3. **Database:** Connected to a private **Amazon RDS** MySQL instance.

4. **Networking:** Custom domains mapped via **Route 53** with SSL/TLS provided by **AWS Certificate Manager (ACM)**.

---

## Contributing
Contributions are welcome! Please feel free to submit a Pull Request.

## License
This project is licensed under the MIT License.

---
<div align="center">
  <sub>Built with ☕ using Spring Boot & React</sub>
</div>
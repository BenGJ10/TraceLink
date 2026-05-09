# TraceLink DevOps Architecture Plan

> **Goal:** Productionize TraceLink on AWS, generate portfolio-quality screenshots, and document a real, end-to-end cloud deployment pipeline.

---

## 🏗️ Final Architecture Diagram

```
Developer (push to GitHub)
        │
        ▼
  GitHub (source)
        │
        ▼
 AWS CodePipeline
  ├── Stage 1: Source  ──── GitHub trigger
  ├── Stage 2: Build   ──── AWS CodeBuild (Docker image)
  └── Stage 3: Deploy  ──── kubectl apply to EKS

                         ┌──────────────────┐
CloudFront Distribution  │  S3 Bucket       │
(tracelink.bengregoryjohn.in)  (React build)│
           ↓             └──────────────────┘
           │   API calls
           ▼
 EKS LoadBalancer (api.bengregoryjohn.in)
           │
           ▼
  EKS Cluster (2 replicas)
  [Spring Boot Pods]
           │
           ▼
  Amazon RDS (MySQL 8)
  [Private subnet, no public access]
```

---

## 🔧 Complete Tool Stack

| Category | Tool | Purpose |
|---|---|---|
| **Containerization** | Docker | Package Spring Boot app |
| **Image Registry** | Amazon ECR | Store Docker images |
| **Orchestration** | Amazon EKS (eksctl) | Run backend containers |
| **Database** | Amazon RDS (MySQL 8) | Managed production database |
| **Frontend Hosting** | Amazon S3 + CloudFront | CDN-backed React app |
| **DNS** | Amazon Route 53 | Custom domain routing |
| **CI/CD Source** | GitHub | Source control trigger |
| **CI/CD Pipeline** | AWS CodePipeline | Orchestrate the build/deploy flow |
| **CI/CD Build** | AWS CodeBuild | Build Docker images, run `mvn`, push to ECR |
| **IaC (Docs)** | Terraform | Provision RDS + ECR + S3 declaratively |
| **Secrets** | AWS Secrets Manager | DB password, JWT secret |
| **Monitoring** | CloudWatch | Pod logs, RDS metrics |

---

## 🌐 Domain Strategy (bengregoryjohn.in)

You have `bengregoryjohn.in`. Here's the clean split:

| Subdomain | Points To | Purpose |
|---|---|---|
| `tracelink.bengregoryjohn.in` | CloudFront distribution | React frontend (user-facing) |
| `api.tracelink.bengregoryjohn.in` | EKS LoadBalancer (via Route 53 CNAME) | Backend API |

**How to do the CNAME:**
1. Create a **Hosted Zone** in Route 53 for `bengregoryjohn.in` (or point your Namecheap/GoDaddy NS records to Route 53).
2. Add a CNAME record: `tracelink → <CloudFront ID>.cloudfront.net`
3. Add a CNAME record: `api.tracelink → <EKS LoadBalancer DNS>` 
4. In the frontend `VITE_API_BASE_URL`, set it to `https://api.tracelink.bengregoryjohn.in`.

> **SSL/TLS:** Request a free certificate from **AWS Certificate Manager (ACM)** for `*.tracelink.bengregoryjohn.in`. Attach it to CloudFront and the EKS LoadBalancer.

---

## 🗺️ Step-by-Step Execution Plan

### Phase 1 — Local Prep

**Step 1.1 — Dockerize the Backend**
- Write a multi-stage `Dockerfile` for Spring Boot to keep the image lean:
  - Stage 1: Maven build → produce the JAR
  - Stage 2: JRE runtime → copy JAR and run
- Inject DB URL, credentials, and JWT secret via **environment variables** (not hardcoded).

**Step 1.2 — Externalize Config**
- Update `application.properties` to read from env vars:
  ```properties
  spring.datasource.url=${DB_URL}
  spring.datasource.username=${DB_USER}
  spring.datasource.password=${DB_PASS}
  jwt.secret=${JWT_SECRET}
  ```
- This is how EKS will inject secrets from **Kubernetes Secrets** (backed by AWS Secrets Manager).

**Step 1.3 — Build React Frontend**
- Run `npm run build` to generate the `dist/` folder.
- Make sure `VITE_API_BASE_URL` is set to `https://api.tracelink.bengregoryjohn.in`.

---

### Phase 2 — AWS Infrastructure (Terraform for Docs)

> **Recommended Terraform scope:** Provision the "static, one-time" infrastructure. Don't Terraform EKS (overkill for a demo). Focus on:
> - ECR Repository
> - RDS Instance + Subnet Group + Security Group
> - S3 Bucket + Bucket Policy
> - CloudFront Distribution

This gives you a clean `main.tf`, `rds.tf`, `ecr.tf`, and `s3_cloudfront.tf` that you can push to GitHub and screenshot as IaC.

**Step 2.1 — ECR + RDS + S3/CloudFront via Terraform**
```bash
terraform init
terraform plan
terraform apply
```

**Step 2.2 — EKS Cluster via eksctl** (manual, faster for demo)
```bash
eksctl create cluster \
  --name tracelink-cluster \
  --region ap-south-1 \
  --nodes 2 \
  --node-type t3.medium
```

---

### Phase 3 — Kubernetes Manifests

We'll create a `k8s/` directory with clean manifests:
- `deployment.yaml` — 2 replica Spring Boot deployment
- `service.yaml` — LoadBalancer type to expose the backend
- `secret.yaml` — Kubernetes Secret for DB credentials (base64)
- `configmap.yaml` — Non-sensitive config (e.g., DB URL)
- `ingress.yaml` (optional) — If you want NGINX ingress instead of a raw LB

---

### Phase 4 — CI/CD Pipeline (CodePipeline + CodeBuild)

**Flow:**
```
GitHub push → CodePipeline triggered
→ Stage 1 (Source): Pull from GitHub
→ Stage 2 (Build):  CodeBuild
  • mvn clean package (skip tests for speed)
  • docker build -t tracelink-backend .
  • docker tag + push to ECR
→ Stage 3 (Deploy): CodeBuild or kubectl
  • aws eks update-kubeconfig
  • kubectl set image deployment/tracelink-backend backend=<new ECR image>
  • kubectl rollout status deployment/tracelink-backend
```

> **For the frontend:** A separate, simpler pipeline:
> GitHub → CodeBuild: `npm run build` → `aws s3 sync dist/ s3://<bucket>` → CloudFront cache invalidation.

**`buildspec.yml` for backend:**
```yaml
version: 0.2
phases:
  pre_build:
    commands:
      - aws ecr get-login-password | docker login --username AWS --password-stdin $ECR_REGISTRY
  build:
    commands:
      - mvn clean package -DskipTests
      - docker build -t $ECR_IMAGE .
      - docker push $ECR_IMAGE
  post_build:
    commands:
      - aws eks update-kubeconfig --name tracelink-cluster --region ap-south-1
      - kubectl set image deployment/tracelink-backend backend=$ECR_IMAGE
```

> **Note on CodeCommit:** AWS is sunsetting CodeCommit for new users (as of 2024). **GitHub is the better source trigger** for CodePipeline. It's what the industry uses, and CodePipeline natively integrates with it via a GitHub connection. This also makes your portfolio look more modern.

---

### Phase 5 — Networking & Security

**Security Groups to configure:**
- **EKS Worker Nodes SG → RDS SG:** Allow port 3306 inbound.
- **EKS LoadBalancer:** Allow port 80 + 443 from `0.0.0.0/0`.
- **RDS:** No public access. Only reachable from within the VPC.

**CORS in Spring Boot:**
- Update `@CrossOrigin` (or global CORS config) to only allow `https://tracelink.bengregoryjohn.in` in production.

---

### Phase 6 — Screenshots Checklist (Portfolio Proof)

| # | Screenshot | What It Proves |
|---|---|---|
| 1 | Terraform plan/apply output | IaC proficiency |
| 2 | ECR with pushed Docker image + tags | Containerization |
| 3 | RDS instance running in AWS Console | Managed DB |
| 4 | EKS cluster + nodes in Console | Kubernetes |
| 5 | `kubectl get pods` — Running status | Deployment working |
| 6 | `kubectl get svc` — LoadBalancer URL | Service exposed |
| 7 | CodePipeline success run (all green stages) | CI/CD |
| 8 | CloudFront distribution + S3 | Frontend hosting |
| 9 | Live app at `tracelink.bengregoryjohn.in` | End-to-end proof |
| 10 | CloudWatch logs from a pod | Observability |

---

## 📋 My Suggestions & Opinions

### ✅ Things I agree with from the other AI's plan
- EKS + RDS + S3/CloudFront is the right stack for a devops portfolio.
- `eksctl` is the fastest path to a working cluster.
- Terraforming select services is smart — don't over-Terraform.

### 🔄 Changes I recommend
1. **GitHub over CodeCommit.** As mentioned, CodeCommit is deprecated for new users. GitHub is the standard.
2. **Two separate pipelines** — one for backend (ECR → EKS) and one for frontend (S3 → CloudFront). Mixing them adds complexity with no benefit.
3. **Use Kubernetes Secrets for credentials**, not environment variables baked into the image. This is the professional approach.
4. **Route 53 + ACM for SSL** — Don't skip HTTPS. A live app without HTTPS is a red flag. ACM gives you a free cert.
5. **Multi-stage Docker build** — The other AI's plan skipped this. A lean JRE-only final image is significantly smaller (500MB → ~150MB).

### 💡 Extra ideas worth considering
- **AWS WAF (Web Application Firewall)** in front of CloudFront — a one-click add that looks great on a resume.
- **Horizontal Pod Autoscaler (HPA)** on EKS — shows you understand scaling.
- **CloudWatch Container Insights** — toggle it on for EKS and you get instant monitoring dashboards.

---

## 🗓️ Suggested Execution Order

```
Week 1
├── Phase 1: Dockerize + externalize config
├── Phase 2: Terraform (ECR, RDS, S3, CF)
└── Phase 3: Write K8s manifests

Week 2
├── Phase 4: eksctl + deploy to EKS manually first
├── Phase 5: CodePipeline + CodeBuild
└── Phase 6: Domain mapping + SSL

Week 3
└── Screenshots, documentation, README
```

# 🎯 Interview Agent — AI-Powered Interview Preparation Platform

> A production-ready SaaS application deployed on Google Kubernetes Engine (GKE) that conducts **live mock interviews** powered by **Google Gemini AI**, with multi-agent feedback analysis, RAG-powered contextual questions, and real-time performance tracking.

**Live URL:** https://interview-prep-app.duckdns.org  
**GitHub:** https://github.com/Harshaharry6081/interview-prep-app

---

## 🏗️ High-Level Architecture (HLA)

```
┌─────────────────────────────────────────────────────────────────────┐
│                         USER BROWSER                                │
│                                                                     │
│   Angular 17 SPA (TypeScript + SCSS)                                │
│   ┌──────────┐  ┌─────────────┐  ┌───────────────┐  ┌──────────┐  │
│   │  Login   │  │   Spaces    │  │  Mock-Interview│  │Dashboard │  │
│   │ (OAuth)  │  │  (Groups)   │  │  (Live AI Chat)│  │(Real KPIs│  │
│   └──────────┘  └─────────────┘  └───────────────┘  └──────────┘  │
└───────────────────────────┬─────────────────────────────────────────┘
                            │ HTTPS (SSL/TLS - Google Managed Cert)
                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│               GOOGLE CLOUD LOAD BALANCER                            │
│               (Ingress + FrontendConfig - HTTP→HTTPS redirect)      │
└──────────┬──────────────────────────────────┬───────────────────────┘
           │ /                                 │ /api/**
           ▼                                  ▼
┌─────────────────────┐         ┌───────────────────────────────────┐
│  FRONTEND POD       │         │  BACKEND POD (Spring Boot 4.x)    │
│  nginx:alpine       │         │  Java 17 / Tomcat                 │
│  (Angular built SPA)│         │                                   │
│                     │         │  Controllers:                     │
│  Routes:            │         │  ├── AuthController   /api/auth   │
│  /login             │         │  ├── InterviewCtrl   /api/interview│
│  /dashboard         │         │  ├── GroupCtrl       /api/groups  │
│  /spaces            │         │  ├── ResourceCtrl    /api/resources│
│  /interview         │         │  ├── ProjectCtrl     /api/projects│
│  /upload            │         │  └── HealthCtrl      /api/health  │
│  /feedback          │         │                                   │
└─────────────────────┘         │  Services:                        │
                                │  ├── MultiAgentOrchestrator ──────┼──►  Google Gemini API
                                │  │   (Technical Agent)            │      (gemini-1.5-flash)
                                │  │   (Communication Agent)        │
                                │  │   (Domain Depth Agent)         │
                                │  ├── EmbeddingService ────────────┼──►  ChromaDB Pod
                                │  │   (RAG / Vector Search)        │      (in-cluster)
                                │  └── DocumentParsingService       │
                                │       (PDF → text chunks)         │
                                └────────────┬──────────────────────┘
                                             │
                      ┌──────────────────────┼──────────────────────┐
                      │                      │                      │
                      ▼                      ▼                      ▼
           ┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
           │   MongoDB Pod   │    │   ChromaDB Pod   │    │  Google Gemini  │
           │   (StatefulSet) │    │   (StatefulSet)  │    │   API (Cloud)   │
           │   Persistent    │    │   Vector Store   │    │  gemini-1.5-    │
           │   Volume (GKE)  │    │   Persistent Vol │    │  flash model    │
           └─────────────────┘    └──────────────────┘    └─────────────────┘
```

---

## 🧠 AI Interview Flow

```
User selects Interview Space (e.g. System Design, HARD difficulty)
          │
          ▼
POST /api/interview/start
          │
          ▼
MultiAgentOrchestratorService.generateNextQuestion()
  ├── Builds prompt with: topic + difficulty + conversation history
  └── Calls Gemini 1.5 Flash → returns tailored first question
          │
          ▼
User types answer → POST /api/interview/{id}/message
          │
          ▼
3 Gemini Agents run in sequence:
  ├── 🔬 Technical Accuracy Agent  → score + technical gaps
  ├── 💬 Communication Coach       → clarity + STAR feedback
  └── 📚 Domain Depth Agent        → domain knowledge depth
          │
          ▼
Scores averaged → saved to MongoDB → returned to frontend
          │
          ▼
Frontend renders:
  ├── Updated live transcript
  ├── Thinking animation while waiting
  └── Right panel: per-agent scores + suggestions
          │
          ▼
User clicks "End Interview"
  └── Gemini generates final performance summary
```

---

## 🗂️ Project Structure

```
Interview-Prep-App/
│
├── backend/                          # Spring Boot application (Java 17)
│   └── src/main/java/com/interviewagent/backend/
│       ├── BackendApplication.java
│       ├── config/
│       │   ├── MongoConfig.java      # Custom MongoClient (bypasses Spring Boot bug)
│       │   └── WebConfig.java        # CORS configuration
│       ├── controllers/
│       │   ├── AuthController.java   # Google OAuth + Test Login
│       │   ├── InterviewController.java  # Session lifecycle
│       │   ├── InterviewGroupController.java  # 6 interview domains
│       │   ├── ResourceController.java   # PDF upload & retrieval
│       │   ├── ProjectController.java    # Project/Folder management
│       │   └── HealthController.java     # /api/health (LB probe)
│       ├── models/
│       │   ├── InterviewSession.java # group, difficulty, transcript, scores
│       │   ├── User.java
│       │   ├── Project.java
│       │   ├── Folder.java
│       │   ├── Resource.java
│       │   └── AgentFeedback.java    # per-agent score/feedback
│       ├── repositories/             # Spring Data MongoDB repos
│       ├── security/
│       │   ├── SecurityConfig.java   # JWT stateless security
│       │   ├── JwtUtil.java
│       │   └── JwtFilter.java
│       └── services/
│           ├── MultiAgentOrchestratorService.java  # Gemini AI orchestration
│           ├── EmbeddingService.java               # ChromaDB RAG
│           └── DocumentParsingService.java         # PDF → text
│
├── frontend/                         # Angular 17 SPA
│   └── src/app/
│       ├── app.routes.ts             # /login /dashboard /spaces /interview
│       ├── components/
│       │   ├── login/                # Google OAuth + Test User login
│       │   ├── spaces/               # Interview group selector
│       │   ├── mock-interview/       # Live AI interview chat
│       │   ├── dashboard/            # Real-time stats & history
│       │   ├── sidebar/              # Navigation
│       │   ├── resource-upload/      # PDF upload
│       │   └── feedback-panel/       # Session feedback view
│       ├── services/
│       │   ├── auth.ts               # Authentication & JWT storage
│       │   ├── interview.ts          # Interview API calls
│       │   └── project.ts            # Project/Resource management
│       └── guards/
│           └── auth-guard.ts         # Route protection
│
├── k8s/                              # Kubernetes manifests
│   ├── namespace.yaml
│   ├── secrets.yaml                  # JWT, MongoDB, Gemini API keys
│   ├── configmap.yaml
│   ├── ingress.yaml                  # GKE HTTPS Ingress
│   ├── certificate.yaml              # Google Managed SSL Cert
│   ├── frontendconfig.yaml           # HTTP→HTTPS redirect
│   ├── backend/deployment.yaml
│   ├── frontend/deployment.yaml
│   ├── mongodb/statefulset.yaml
│   └── chromadb/statefulset.yaml
│
├── docker-compose.yml               # Local development
├── cloudbuild.yaml                  # GCP Cloud Build CI
├── deploy.sh                        # Manual deployment script
└── README.md
```

---

## 🗺️ Application Routes

| Route | Component | Description |
|---|---|---|
| `/login` | Login | Google OAuth + Test User sign-in |
| `/spaces` | Spaces ⭐ | Interview group selector (System Design, Backend, Frontend, DSA, DevOps, HR) |
| `/interview?group=&topic=&difficulty=` | MockInterview | Live AI interview session |
| `/dashboard` | Dashboard | Real session history, scores by domain |
| `/upload` | ResourceUpload | Upload PDFs for RAG context |
| `/feedback` | FeedbackPanel | View detailed past session feedback |

---

## 🤖 Interview Groups

| Group | Topics |
|---|---|
| 🏗️ System Design | Microservices, Load Balancing, Caching, Databases, Message Queues |
| ⚙️ Backend Engineering | Spring Boot, REST API, Concurrency, JVM, Design Patterns |
| 🖥️ Frontend Engineering | Angular, React, TypeScript, CSS, State Management |
| 🧩 DSA & Algorithms | Arrays, Trees, Graphs, Dynamic Programming, Binary Search |
| ☁️ DevOps & Cloud | Docker, Kubernetes, CI/CD, Terraform, GCP, Monitoring |
| 🤝 HR & Behavioural | STAR Method, Leadership, Conflict Resolution, Goal Setting |

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| **AI** | Google Gemini 1.5 Flash (multi-agent analysis) |
| **Frontend** | Angular 17, TypeScript, SCSS |
| **Backend** | Spring Boot 4.x, Java 17 |
| **Auth** | Google OAuth 2.0 + JWT |
| **Database** | MongoDB (GKE StatefulSet + Persistent Volume) |
| **Vector DB** | ChromaDB (GKE StatefulSet + Persistent Volume) |
| **Hosting** | GKE Autopilot (us-central1) |
| **SSL** | Google Managed Certificate |
| **LB** | Google Cloud HTTPS Load Balancer |
| **CI/CD** | Cloud Build + kubectl rollout |

---

## 🚀 Local Setup

### Prerequisites
- Docker & Docker Compose
- A Gemini API key from [aistudio.google.com](https://aistudio.google.com)

### Run locally
```bash
# Clone the repo
git clone https://github.com/Harshaharry6081/interview-prep-app.git
cd interview-prep-app

# Set Gemini key
export GEMINI_API_KEY=your_key_here

# Start all services
docker-compose up --build
```

Access at `http://localhost:4200`

---

## ☁️ GKE Production Deployment

### 1. Apply secrets (with your actual keys)
```bash
kubectl apply -f k8s/secrets.yaml
```

### 2. Apply all manifests
```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/certificate.yaml
kubectl apply -f k8s/frontendconfig.yaml
kubectl apply -f k8s/mongodb/
kubectl apply -f k8s/chromadb/
kubectl apply -f k8s/backend/
kubectl apply -f k8s/frontend/
kubectl apply -f k8s/ingress.yaml
```

### 3. Rebuild & redeploy
```bash
# Backend
docker build -t gcr.io/[PROJECT]/backend ./backend
docker push gcr.io/[PROJECT]/backend
kubectl rollout restart deployment/backend -n interview-agent

# Frontend
docker build -t gcr.io/[PROJECT]/frontend ./frontend
docker push gcr.io/[PROJECT]/frontend
kubectl rollout restart deployment/frontend -n interview-agent
```

---

## 🔑 Authentication

Two login paths:
1. **Google OAuth 2.0** — Signs in with Google account. Requires correct OAuth credentials in GCP console for the production domain.
2. **Test User Login** — Enter any username + password. Auto-creates a test account. Perfect for development and demos.

---

## 📡 Backend API Reference

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/health` | None | Load Balancer health probe |
| POST | `/api/auth/google` | None | Google ID token → JWT |
| POST | `/api/auth/test-login` | None | Test user auth |
| GET | `/api/groups` | None | List of interview domains |
| POST | `/api/interview/start` | JWT | Start AI interview session |
| POST | `/api/interview/{id}/message` | JWT | Send answer, get AI question + feedback |
| POST | `/api/interview/{id}/end` | JWT | End session + final assessment |
| GET | `/api/interview/history` | JWT | User's past sessions |
| POST | `/api/resources/upload` | JWT | Upload PDF for RAG |
| GET | `/api/resources` | JWT | List uploaded resources |
| GET | `/api/projects` | JWT | List user's projects |
| POST | `/api/projects` | JWT | Create project |

---

## 📝 License

Built for the **Google Cloud AI Hackathon 2024/25**. Open for educational and preparation purposes.

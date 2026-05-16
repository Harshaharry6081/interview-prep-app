# Interview Agent - AI-Powered Interview Preparation Platform

Interview Agent is a sophisticated SaaS platform designed to help candidates prepare for technical interviews using AI-driven roleplay, Retrieval-Augmented Generation (RAG), and multi-agent feedback.

## 🚀 Key Features

- **RAG-Powered Questioning:** Upload your own study materials or resumes (PDFs) to generate context-aware interview questions.
- **Multi-Agent Feedback:** Receive detailed analysis and feedback from specialized AI agents simulating different interviewer personas.
- **Secure Authentication:** 
  - **Google OAuth 2.0:** Integrated for seamless production login.
  - **Test Login:** A development bypass for rapid testing using simple username/password.
- **Production-Ready Infrastructure:**
  - Deployed on **Google Kubernetes Engine (GKE)**.
  - Fully secured with **SSL/HTTPS** via Google-managed certificates.
  - Automatic HTTP-to-HTTPS redirection.

## 🛠 Tech Stack

- **Frontend:** Angular 17+ with RxJS and SCSS (Premium Aesthetics).
- **Backend:** Spring Boot 3+ (Java 17).
- **Persistence:** MongoDB (StatefulSet on GKE).
- **Vector Storage:** ChromaDB (StatefulSet on GKE).
- **Orchestration:** Kubernetes (GKE Autopilot/Standard).
- **Cloud:** Google Cloud Platform (GCP).

## 📦 Project Structure

```text
├── backend/            # Spring Boot application source
├── frontend/           # Angular application source
├── k8s/                # Kubernetes manifests (Deployments, Services, Ingress, etc.)
├── docker-compose.yml  # Local development orchestration
└── deploy.sh           # GKE deployment script
```

## 🛠 Local Setup

1. **Prerequisites:** Docker and Docker Compose installed.
2. **Run the stack:**
   ```bash
   docker-compose up --build
   ```
3. **Access:**
   - Frontend: `http://localhost:4200`
   - Backend API: `http://localhost:8080`

## ☁️ GKE Deployment

The application is configured to run on GKE with a custom domain (e.g., `interview-prep-app.duckdns.org`).

1. **Build and Push Images:**
   ```bash
   docker build -t gcr.io/[PROJECT_ID]/backend ./backend
   docker build -t gcr.io/[PROJECT_ID]/frontend ./frontend
   docker push gcr.io/[PROJECT_ID]/backend
   docker push gcr.io/[PROJECT_ID]/frontend
   ```

2. **Apply Kubernetes Manifests:**
   ```bash
   kubectl apply -f k8s/namespace.yaml
   kubectl apply -f k8s/secrets.yaml
   kubectl apply -f k8s/mongodb/
   kubectl apply -f k8s/chromadb/
   kubectl apply -f k8s/backend/
   kubectl apply -f k8s/frontend/
   kubectl apply -f k8s/ingress.yaml
   ```

## 🔒 Security Configuration

- **CORS:** Configured to allow traffic from the production domain and local development origins.
- **Health Checks:** Dedicated `/api/health` endpoint ensures high availability behind the Google Cloud Load Balancer.
- **SSL:** Google-managed certificates applied via GKE Ingress.

## 📝 License

This project was developed for a hackathon. Use as-is for educational and preparation purposes.

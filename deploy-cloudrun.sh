#!/bin/bash
# ===========================================================
# deploy-cloudrun.sh – One-shot Google Cloud Run deployment
#
# USAGE:
#   chmod +x deploy-cloudrun.sh
#   ./deploy-cloudrun.sh YOUR_GCP_PROJECT_ID
#
# PREREQUISITES:
#   - gcloud CLI installed and authenticated (gcloud auth login)
#   - Docker installed
# ===========================================================

set -e  # Exit immediately on error

# ─── Config ──────────────────────────────────────────────────
PROJECT_ID="${1:-YOUR_PROJECT_ID}"
REGION="us-central1"
REGISTRY="${REGION}-docker.pkg.dev/${PROJECT_ID}/interview-agent"

echo "🚀 Deploying Interview Agent to Google Cloud Run"
echo "   Project : $PROJECT_ID"
echo "   Region  : $REGION"
echo ""

# ─── Step 1: Set GCP project ──────────────────────────────────
echo "📌 Step 1/6 – Setting GCP project..."
gcloud config set project "$PROJECT_ID"

# ─── Step 2: Enable required APIs ────────────────────────────
echo "📌 Step 2/6 – Enabling required APIs..."
gcloud services enable \
  run.googleapis.com \
  artifactregistry.googleapis.com \
  cloudbuild.googleapis.com

# ─── Step 3: Create Artifact Registry repo ────────────────────
echo "📌 Step 3/6 – Creating Artifact Registry repository..."
gcloud artifacts repositories create interview-agent \
  --repository-format=docker \
  --location="${REGION}" \
  --description="Interview Agent Docker images" \
  2>/dev/null || echo "   ℹ️  Repository already exists, skipping."

# ─── Step 4: Build & push images using Cloud Build (No local docker needed) ───────
echo "📌 Step 4/6 – Building images in Cloud Build..."
echo "   Building backend..."
gcloud builds submit --tag "${REGISTRY}/backend:latest" ./backend

echo "   Building frontend..."
gcloud builds submit --tag "${REGISTRY}/frontend:latest" ./frontend

# ─── Step 5: Deploy Backend to Cloud Run ──────────────────────
echo "📌 Step 5/6 – Deploying Backend to Cloud Run..."
# TODO: Replace the database URLs with your actual managed database instances (e.g. MongoDB Atlas)
gcloud run deploy interview-agent-backend \
  --image="${REGISTRY}/backend:latest" \
  --region="${REGION}" \
  --platform="managed" \
  --allow-unauthenticated \
  --set-env-vars="SPRING_DATA_MONGODB_URI=mongodb+srv://YOUR_USER:YOUR_PASS@YOUR_ATLAS_CLUSTER.mongodb.net/interview_agent,CHROMA_HOST=YOUR_CHROMA_URL,GEMINI_API_KEY=YOUR_GEMINI_KEY"

BACKEND_URL=$(gcloud run services describe interview-agent-backend --region="${REGION}" --format="value(status.url)")
echo "✅ Backend deployed at: $BACKEND_URL"

# ─── Step 6: Deploy Frontend to Cloud Run ─────────────────────
echo "📌 Step 6/6 – Deploying Frontend to Cloud Run..."
gcloud run deploy interview-agent-frontend \
  --image="${REGISTRY}/frontend:latest" \
  --region="${REGION}" \
  --platform="managed" \
  --allow-unauthenticated \
  --set-env-vars="API_URL=${BACKEND_URL}"

FRONTEND_URL=$(gcloud run services describe interview-agent-frontend --region="${REGION}" --format="value(status.url)")
echo "✅ Frontend deployed at: $FRONTEND_URL"

# ─── Done ────────────────────────────────────────────────────
echo ""
echo "🎉 Deployment complete!"
echo "🌐 Frontend URL: $FRONTEND_URL"
echo "🌐 Backend URL:  $BACKEND_URL"
echo ""
echo "⚠️  IMPORTANT REMINDER:"
echo "   Make sure to update the environment variables in Step 5"
echo "   with your actual MongoDB Atlas URI, Chroma URL, and Gemini API key."

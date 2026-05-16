#!/bin/bash
# ===========================================================
# deploy.sh – One-shot GKE deployment for Interview Agent
#
# USAGE:
#   chmod +x deploy.sh
#   ./deploy.sh YOUR_GCP_PROJECT_ID
#
# PREREQUISITES:
#   - gcloud CLI installed and authenticated (gcloud auth login)
#   - kubectl installed
#   - Docker installed
# ===========================================================

set -e  # Exit immediately on error

# ─── Config ──────────────────────────────────────────────────
PROJECT_ID="${1:-YOUR_PROJECT_ID}"
REGION="us-central1"
CLUSTER_NAME="interview-agent-cluster"
REGISTRY="${REGION}-docker.pkg.dev/${PROJECT_ID}/interview-agent"

echo "🚀 Deploying Interview Agent to GKE"
echo "   Project : $PROJECT_ID"
echo "   Region  : $REGION"
echo "   Cluster : $CLUSTER_NAME"
echo ""

# ─── Step 1: Set GCP project ──────────────────────────────────
echo "📌 Step 1/8 – Setting GCP project..."
gcloud config set project "$PROJECT_ID"

# ─── Step 2: Enable required APIs ────────────────────────────
echo "📌 Step 2/8 – Enabling required APIs..."
gcloud services enable \
  container.googleapis.com \
  artifactregistry.googleapis.com \
  cloudbuild.googleapis.com

# ─── Step 3: Create Artifact Registry repo ────────────────────
echo "📌 Step 3/8 – Creating Artifact Registry repository..."
gcloud artifacts repositories create interview-agent \
  --repository-format=docker \
  --location="${REGION}" \
  --description="Interview Agent Docker images" \
  2>/dev/null || echo "   ℹ️  Repository already exists, skipping."

# ─── Step 4: Authenticate Docker with Artifact Registry ───────
echo "📌 Step 4/8 – Authenticating Docker..."
gcloud auth configure-docker "${REGION}-docker.pkg.dev" --quiet

# ─── Step 5: Build & push images ──────────────────────────────
echo "📌 Step 5/8 – Building & pushing images..."
docker build -t "${REGISTRY}/backend:latest" ./backend
docker push "${REGISTRY}/backend:latest"

docker build -t "${REGISTRY}/frontend:latest" ./frontend
docker push "${REGISTRY}/frontend:latest"

# ─── Step 6: Create GKE Autopilot cluster ─────────────────────
echo "📌 Step 6/8 – Creating GKE Autopilot cluster (this may take ~3-5 minutes)..."
gcloud container clusters create-auto "$CLUSTER_NAME" \
  --region "$REGION" \
  2>/dev/null || echo "   ℹ️  Cluster already exists, skipping."

# ─── Step 7: Get credentials for kubectl ──────────────────────
echo "📌 Step 7/8 – Fetching cluster credentials..."
gcloud container clusters get-credentials "$CLUSTER_NAME" --region "$REGION"

# ─── Step 8: Apply Kubernetes manifests ───────────────────────
echo "📌 Step 8/8 – Applying Kubernetes manifests..."

# Substitute the real project ID into the manifest files
sed -i "s|YOUR_PROJECT_ID|${PROJECT_ID}|g" k8s/backend/deployment.yaml
sed -i "s|YOUR_PROJECT_ID|${PROJECT_ID}|g" k8s/frontend/deployment.yaml

kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/mongodb/statefulset.yaml
kubectl apply -f k8s/chromadb/statefulset.yaml
kubectl apply -f k8s/backend/deployment.yaml
kubectl apply -f k8s/frontend/deployment.yaml
kubectl apply -f k8s/ingress.yaml

# ─── Done ────────────────────────────────────────────────────
echo ""
echo "✅ Deployment complete!"
echo ""
echo "⏳ Waiting for the Ingress to get an external IP (~2-5 minutes)..."
echo "   Run this command to check:"
echo "   kubectl get ingress -n interview-agent"
echo ""
echo "📋 Once you have the external IP:"
echo "   1. Add it to Google OAuth Authorized origins:"
echo "      → https://console.cloud.google.com/apis/credentials"
echo "      → Add: http://<YOUR_EXTERNAL_IP>"
echo "   2. Update frontend/src/app/services/auth.ts if needed"
echo "   3. Open http://<YOUR_EXTERNAL_IP> in your browser"

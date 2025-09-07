#!/bin/bash
set -euo pipefail

# Ensure kubectl & az are available even in Jenkins
export PATH=$PATH:/usr/local/bin

echo "===== Deploying Kubernetes App Manifests ====="

CLOUD_PROVIDER=${CLOUD_PROVIDER:-aws}
BASE_DIR="/var/lib/jenkins/cross-cloud-devops-framework"
KUBECONFIG_FILE="$BASE_DIR/.kube/config"

# Always create kubeconfig directory cleanly
mkdir -p "$(dirname "$KUBECONFIG_FILE")"
rm -f "$KUBECONFIG_FILE"

if [[ "$CLOUD_PROVIDER" == "aws" ]]; then
    echo "[INFO] Deploying app on AWS EKS..."

    # Extract kubeconfig from Terraform output & fix CRLF issues
    terraform -chdir="$BASE_DIR/terraform/aws" output -raw kubeconfig \
      | sed 's/\r$//' > "$KUBECONFIG_FILE"

    export KUBECONFIG="$KUBECONFIG_FILE"

    echo "== Contexts =="
    kubectl config get-contexts || true

    echo "== Nodes =="
    kubectl get nodes -o wide
fi

if [[ "$CLOUD_PROVIDER" == "azure" ]]; then
    echo "[INFO] Deploying app on Azure AKS..."

    which az && az version || true
    which kubectl && kubectl version --client || true

    # Fetch kubeconfig cleanly into Jenkins-specific path
    az aks get-credentials \
        --resource-group crosscloud-aks-rg \
        --name crosscloud_aks \
        --overwrite-existing \
        --file "$KUBECONFIG_FILE"

    export KUBECONFIG="$KUBECONFIG_FILE"

    echo "== Contexts =="
    kubectl config get-contexts || true

    echo "== Nodes =="
    kubectl get nodes -o wide
fi

# Apply namespace first
kubectl apply -f "$BASE_DIR/k8s-manifests/namespaces.yaml"

# Deploy sample app manifests
kubectl apply -f "$BASE_DIR/k8s-manifests/sample-app/deployment.yaml"
kubectl apply -f "$BASE_DIR/k8s-manifests/sample-app/service.yaml"
kubectl apply -f "$BASE_DIR/k8s-manifests/sample-app/hpa.yaml"

echo "[INFO] Verifying deployments..."
kubectl get all -n apps

#!/usr/bin/env bash
set -euo pipefail
# Ensure kubectl & az are available even in Jenkins
export PATH=$PATH:/usr/local/bin
# Basic cluster/app health checks
echo "Nodes:"
kubectl get nodes -o wide

echo "Pods:"
kubectl -n apps get pods -o wide

echo "Services:"
kubectl -n apps get svc -o wide

# Try to hit the app if a LoadBalancer is provisioned
LB_IP=$(kubectl -n apps get svc sample-app -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || true)
if [ -z "${LB_IP}" ]; then
  LB_IP=$(kubectl -n apps get svc sample-app -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || true)
fi

if [ -n "${LB_IP}" ]; then
  echo "Found LoadBalancer endpoint: ${LB_IP}"
  echo "Attempting HTTP request..."
  curl -sSf "http://${LB_IP}/" | head -n 5 || true
else
  echo "No LoadBalancer endpoint yet. Re-run validate after a minute."
fi


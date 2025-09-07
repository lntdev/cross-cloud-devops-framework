#!/usr/bin/env bash
set -euo pipefail
# Ensure kubectl & az are available even in Jenkins
export PATH=$PATH:/usr/local/bin
# Delete app manifests (reverse-ish order)
kubectl delete -f k8s-manifests/ingress.yaml --ignore-not-found
kubectl delete -f k8s-manifests/sample-app/hpa.yaml --ignore-not-found
kubectl delete -f k8s-manifests/sample-app/service.yaml --ignore-not-found
kubectl delete -f k8s-manifests/sample-app/deployment.yaml --ignore-not-found
kubectl delete -f k8s-manifests/namespaces.yaml --ignore-not-found


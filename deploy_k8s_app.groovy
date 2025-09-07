// node('master') {
//     withEnv([
//         "ANSIBLE_HOST_KEY_CHECKING=False",
//         "KUBECONFIG=/var/lib/jenkins/cross-cloud-devops-framework/.kube/config"
//     ]) {

//         properties([
//             parameters([
//                 choice(
//                     name: 'CLOUD_PROVIDER',
//                     choices: ['aws', 'azure'],
//                     description: 'Select the cloud provider for Kubernetes app deployment'
//                 ),
//                 choice(
//                     name: 'ACTION',
//                     choices: ['deploy', 'validate', 'destroy'],
//                     description: 'Choose whether to deploy, validate, or destroy Kubernetes app manifests'
//                 )
//             ])
//         ])

//         stage('Checkout Code') {
//             checkout scm
//         }

//         if (params.CLOUD_PROVIDER == 'aws') {
//             // =========================
//             // AWS EKS Deploy / Validate / Destroy
//             // =========================
//             stage('Configure kubeconfig (EKS)') {
//                 dir("/var/lib/jenkins/cross-cloud-devops-framework") {
//                     sh '''
//                         export PATH=$PATH:/usr/local/bin
//                         set -euo pipefail
//                         echo "Setting up kubeconfig from Terraform output..."
//                         terraform -chdir=terraform/aws output -raw kubeconfig > ${KUBECONFIG}
//                     '''
//                 }
//             }

//             if (params.ACTION == 'deploy') {
//                 stage('Deploy K8s App on EKS') {
//                     dir("/var/lib/jenkins/cross-cloud-devops-framework") {
//                         sh '''
//                             set -euo pipefail
//                             echo "Deploying Kubernetes app manifests on EKS..."
//                             bash ci-scripts/deploy.sh
//                         '''
//                     }
//                 }
//             }

//             if (params.ACTION == 'validate') {
//                 stage('Validate K8s App on EKS') {
//                     dir("/var/lib/jenkins/cross-cloud-devops-framework") {
//                         sh '''
//                             set -euo pipefail
//                             echo "Validating Kubernetes app on EKS..."
//                             bash ci-scripts/validate.sh
//                         '''
//                     }
//                 }
//             }

//             if (params.ACTION == 'destroy') {
//                 stage('Destroy K8s App on EKS') {
//                     dir("/var/lib/jenkins/cross-cloud-devops-framework") {
//                         sh '''
//                             set -euo pipefail
//                             echo "Destroying Kubernetes app manifests on EKS..."
//                             bash ci-scripts/destroy.sh
//                         '''
//                     }
//                 }
//             }
//         }

//         if (params.CLOUD_PROVIDER == 'azure') {
//             // =========================
//             // Azure AKS Deploy / Validate / Destroy
//             // =========================
//             stage('Configure kubeconfig (AKS)') {
//                 dir("/var/lib/jenkins/cross-cloud-devops-framework") {
//                     sh '''
//                         set -euo pipefail
//                         echo "Configuring kubeconfig for AKS cluster..."
//                         az aks get-credentials --resource-group crosscloud-aks-rg --name crosscloud_aks --overwrite-existing
//                     '''
//                 }
//             }

//             if (params.ACTION == 'deploy') {
//                 stage('Deploy K8s App on AKS') {
//                     dir("/var/lib/jenkins/cross-cloud-devops-framework") {
//                         sh '''
//                             set -euo pipefail
//                             echo "Deploying Kubernetes app manifests on AKS..."
//                             bash ci-scripts/deploy.sh
//                         '''
//                     }
//                 }
//             }

//             if (params.ACTION == 'validate') {
//                 stage('Validate K8s App on AKS') {
//                     dir("/var/lib/jenkins/cross-cloud-devops-framework") {
//                         sh '''
//                             set -euo pipefail
//                             echo "Validating Kubernetes app on AKS..."
//                             bash ci-scripts/validate.sh
//                         '''
//                     }
//                 }
//             }

//             if (params.ACTION == 'destroy') {
//                 stage('Destroy K8s App on AKS') {
//                     dir("/var/lib/jenkins/cross-cloud-devops-framework") {
//                         sh '''
//                             set -euo pipefail
//                             echo "Destroying Kubernetes app manifests on AKS..."
//                             bash ci-scripts/destroy.sh
//                         '''
//                     }
//                 }
//             }
//         }
//     }
// }

// working 

// node('master') {
//   withEnv([
//     "ANSIBLE_HOST_KEY_CHECKING=False",
//     "KUBECONFIG=/var/lib/jenkins/cross-cloud-devops-framework/.kube/config",
//     "PATH=/usr/local/bin:${env.PATH}"
//   ]) {

//     properties([
//       parameters([
//         choice(name: 'CLOUD_PROVIDER', choices: ['aws','azure'], description: 'Select cloud'),
//         choice(name: 'ACTION', choices: ['deploy','validate','destroy'], description: 'What to do')
//       ])
//     ])

//     stage('Checkout Code') { checkout scm }

//     if (params.CLOUD_PROVIDER == 'aws') {
//       // Bind AWS creds as username/password -> env vars
//       withCredentials([[$class: 'UsernamePasswordMultiBinding',
//                         credentialsId: 'aws-crosscloud-up',
//                         usernameVariable: 'AWS_ACCESS_KEY_ID',
//                         passwordVariable: 'AWS_SECRET_ACCESS_KEY']]) {
//         withEnv(["AWS_DEFAULT_REGION=us-east-1"]) {

//           stage('Configure kubeconfig (EKS)') {
//             dir("/var/lib/jenkins/cross-cloud-devops-framework") {
//               sh '''
//                 set -euo pipefail
//                 echo "Setting up kubeconfig from Terraform output (EKS)..."
//                 terraform -chdir=terraform/aws output -raw kubeconfig | sed 's/\r$//' > "${KUBECONFIG}"
//                 echo "== Contexts =="; kubectl config get-contexts || true
//                 echo "== Nodes ==";    kubectl get nodes -o wide || true
//               '''
//             }
//           }

//           if (params.ACTION == 'deploy') {
//             stage('Deploy K8s App on EKS') {
//               dir("/var/lib/jenkins/cross-cloud-devops-framework") {
//                 sh '''
//                   set -euo pipefail
//                   echo "Deploying Kubernetes app manifests on EKS..."
//                   CLOUD_PROVIDER=aws bash ci-scripts/deploy.sh
//                 '''
//               }
//             }
//           }

//           if (params.ACTION == 'validate') {
//             stage('Validate K8s App on EKS') {
//               dir("/var/lib/jenkins/cross-cloud-devops-framework") {
//                 sh '''
//                   set -euo pipefail
//                   echo "Validating Kubernetes app on EKS..."
//                   CLOUD_PROVIDER=aws bash ci-scripts/validate.sh
//                 '''
//               }
//             }
//           }

//           if (params.ACTION == 'destroy') {
//             stage('Destroy K8s App on EKS') {
//               dir("/var/lib/jenkins/cross-cloud-devops-framework") {
//                 sh '''
//                   set -euo pipefail
//                   echo "Destroying Kubernetes app manifests on EKS..."
//                   CLOUD_PROVIDER=aws bash ci-scripts/destroy.sh
//                 '''
//               }
//             }
//           }
//         }
//       }
//     }

//     if (params.CLOUD_PROVIDER == 'azure') {
//       stage('Configure kubeconfig (AKS)') {
//         dir("/var/lib/jenkins/cross-cloud-devops-framework") {
//           sh '''
//             set -euo pipefail
//             echo "Configuring kubeconfig for AKS cluster..."
//             az aks get-credentials --resource-group crosscloud-aks-rg --name crosscloud_aks --overwrite-existing --only-show-errors --output none
//             echo "== Contexts =="; kubectl config get-contexts || true
//             echo "== Nodes ==";    kubectl get nodes -o wide || true
//           '''
//         }
//       }
//       if (params.ACTION == 'deploy') {
//         stage('Deploy K8s App on AKS') {
//           dir("/var/lib/jenkins/cross-cloud-devops-framework") {
//             sh '''
//               set -euo pipefail
//               echo "Deploying Kubernetes app manifests on AKS..."
//               CLOUD_PROVIDER=azure bash ci-scripts/deploy.sh
//             '''
//           }
//         }
//       }
//       if (params.ACTION == 'validate') {
//         stage('Validate K8s App on AKS') {
//           dir("/var/lib/jenkins/cross-cloud-devops-framework") {
//             sh '''
//               set -euo pipefail
//               echo "Validating Kubernetes app on AKS..."
//               CLOUD_PROVIDER=azure bash ci-scripts/validate.sh
//             '''
//           }
//         }
//       }
//       if (params.ACTION == 'destroy') {
//         stage('Destroy K8s App on AKS') {
//           dir("/var/lib/jenkins/cross-cloud-devops-framework") {
//             sh '''
//               set -euo pipefail
//               echo "Destroying Kubernetes app manifests on AKS..."
//               CLOUD_PROVIDER=azure bash ci-scripts/destroy.sh
//             '''
//           }
//         }
//       }
//     }
//   }
// }


node('master') {
  withEnv([
    "ANSIBLE_HOST_KEY_CHECKING=False",
    "KUBECONFIG=/var/lib/jenkins/cross-cloud-devops-framework/.kube/config",
    "PATH=/usr/local/bin:${env.PATH}"
  ]) {

    properties([
      parameters([
        choice(name: 'CLOUD_PROVIDER', choices: ['aws','azure'], description: 'Select cloud'),
        choice(name: 'ACTION', choices: ['deploy','validate','destroy'], description: 'What to do')
      ])
    ])

    stage('Checkout Code') { checkout scm }

    if (params.CLOUD_PROVIDER == 'aws') {
      // Bind AWS creds as username/password -> env vars
      withCredentials([[$class: 'UsernamePasswordMultiBinding',
                        credentialsId: 'aws-crosscloud-up',
                        usernameVariable: 'AWS_ACCESS_KEY_ID',
                        passwordVariable: 'AWS_SECRET_ACCESS_KEY']]) {
        withEnv(["AWS_DEFAULT_REGION=us-east-1"]) {

          stage('Configure kubeconfig (EKS)') {
            dir("/var/lib/jenkins/cross-cloud-devops-framework") {
              sh '''
                set -euo pipefail
                echo "Setting up kubeconfig from Terraform output (EKS)..."
                terraform -chdir=terraform/aws output -raw kubeconfig | sed 's/\\r$//' > "${KUBECONFIG}"
                echo "== Contexts =="; kubectl config get-contexts || true
                echo "== Nodes ==";    kubectl get nodes -o wide || true
              '''
            }
          }

          if (params.ACTION == 'deploy') {
            stage('Deploy K8s App on EKS') {
              dir("/var/lib/jenkins/cross-cloud-devops-framework") {
                sh '''
                  set -euo pipefail
                  echo "Deploying Kubernetes app manifests on EKS..."
                  CLOUD_PROVIDER=aws bash ci-scripts/deploy.sh
                '''
              }
            }

            // ===== Added stage (single source of truth) =====
            stage('Enable Observability (Helm)') {
              dir("/var/lib/jenkins/cross-cloud-devops-framework") {
                sh '''
                  set -euo pipefail
                  # Namespaces (apps + monitoring)
                  kubectl apply -f k8s-manifests/namespaces.yaml

                  # Helm repos
                  helm repo add prometheus-community https://prometheus-community.github.io/helm-charts || true
                  helm repo add grafana https://grafana.github.io/helm-charts || true
                  helm repo update

                  # kube-prometheus-stack (Prometheus + Alertmanager + Grafana)
                  helm upgrade --install kps prometheus-community/kube-prometheus-stack \
                    -n monitoring \
                    -f monitoring/prometheus/values.yaml \
                    --create-namespace

                  # Loki + Promtail (attach a cluster label = eks)
                  helm upgrade --install loki grafana/loki-stack \
                    -n monitoring \
                    -f monitoring/loki/values.yaml \
                    --set promtail.config.clients[0].external_labels.cluster=eks

                  # Dashboards: generic + your cross-cloud dashboard (if present)
                  kubectl apply -f monitoring/grafana/dashboards/k8s-overview-configmap.yaml || true
                  kubectl apply -f monitoring/grafana/dashboards/crosscloud-dash.yaml || true

                  echo "Waiting for Grafana external endpoint (if LoadBalancer enabled)..."
                  for i in $(seq 1 40); do
                    GIP=$(kubectl -n monitoring get svc kps-grafana -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || true)
                    GHOST=$(kubectl -n monitoring get svc kps-grafana -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || true)
                    if [ -n "$GIP" ] || [ -n "$GHOST" ]; then
                      echo "Grafana: http://${GIP:-$GHOST}"
                      break
                    fi
                    echo "…waiting for Grafana ($i/40)"
                    sleep 10
                  done
                '''
              }
            }
            // ================================================
          }

          if (params.ACTION == 'validate') {
            stage('Validate K8s App on EKS') {
              dir("/var/lib/jenkins/cross-cloud-devops-framework") {
                sh '''
                  set -euo pipefail
                  echo "Validating Kubernetes app on EKS..."
                  CLOUD_PROVIDER=aws bash ci-scripts/validate.sh
                '''
              }
            }
          }

          if (params.ACTION == 'destroy') {
            stage('Destroy K8s App on EKS') {
              dir("/var/lib/jenkins/cross-cloud-devops-framework") {
                sh '''
                  set -euo pipefail
                  echo "Destroying Kubernetes app manifests on EKS..."
                  CLOUD_PROVIDER=aws bash ci-scripts/destroy.sh
                '''
              }
            }
          }
        }
      }
    }

    if (params.CLOUD_PROVIDER == 'azure') {
      stage('Configure kubeconfig (AKS)') {
        dir("/var/lib/jenkins/cross-cloud-devops-framework") {
          sh '''
            set -euo pipefail
            echo "Configuring kubeconfig for AKS cluster..."
            az aks get-credentials --resource-group crosscloud-aks-rg --name crosscloud_aks --overwrite-existing --only-show-errors --output none
            echo "== Contexts =="; kubectl config get-contexts || true
            echo "== Nodes ==";    kubectl get nodes -o wide || true
          '''
        }
      }
      if (params.ACTION == 'deploy') {
        stage('Deploy K8s App on AKS') {
          dir("/var/lib/jenkins/cross-cloud-devops-framework") {
            sh '''
              set -euo pipefail
              echo "Deploying Kubernetes app manifests on AKS..."
              CLOUD_PROVIDER=azure bash ci-scripts/deploy.sh
            '''
          }
        }

        // ===== Added stage (single source of truth) =====
        stage('Enable Observability (Helm)') {
          dir("/var/lib/jenkins/cross-cloud-devops-framework") {
            sh '''
              set -euo pipefail
              # Namespaces (apps + monitoring)
              kubectl apply -f k8s-manifests/namespaces.yaml

              # Helm repos
              helm repo add prometheus-community https://prometheus-community.github.io/helm-charts || true
              helm repo add grafana https://grafana.github.io/helm-charts || true
              helm repo update

              # kube-prometheus-stack (Prometheus + Alertmanager + Grafana)
              helm upgrade --install kps prometheus-community/kube-prometheus-stack \
                -n monitoring \
                -f monitoring/prometheus/values.yaml \
                --create-namespace

              # Loki + Promtail (attach a cluster label = aks)
              helm upgrade --install loki grafana/loki-stack \
                -n monitoring \
                -f monitoring/loki/values.yaml \
                --set promtail.config.clients[0].external_labels.cluster=aks

              # Dashboards: generic + your cross-cloud dashboard (if present)
              kubectl apply -f monitoring/grafana/dashboards/k8s-overview-configmap.yaml || true
              kubectl apply -f monitoring/grafana/dashboards/crosscloud-dash.yaml || true

              echo "Waiting for Grafana external endpoint (if LoadBalancer enabled)..."
              for i in $(seq 1 40); do
                GIP=$(kubectl -n monitoring get svc kps-grafana -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || true)
                GHOST=$(kubectl -n monitoring get svc kps-grafana -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || true)
                if [ -n "$GIP" ] || [ -n "$GHOST" ]; then
                  echo "Grafana: http://${GIP:-$GHOST}"
                  break
                fi
                echo "…waiting for Grafana ($i/40)"
                sleep 10
              done
            '''
          }
        }
        // ================================================
      }
      if (params.ACTION == 'validate') {
        stage('Validate K8s App on AKS') {
          dir("/var/lib/jenkins/cross-cloud-devops-framework") {
            sh '''
              set -euo pipefail
              echo "Validating Kubernetes app on AKS..."
              CLOUD_PROVIDER=azure bash ci-scripts/validate.sh
            '''
          }
        }
      }
      if (params.ACTION == 'destroy') {
        stage('Destroy K8s App on AKS') {
          dir("/var/lib/jenkins/cross-cloud-devops-framework") {
            sh '''
              set -euo pipefail
              echo "Destroying Kubernetes app manifests on AKS..."
              CLOUD_PROVIDER=azure bash ci-scripts/destroy.sh
            '''
          }
        }
      }
    }
  }
}

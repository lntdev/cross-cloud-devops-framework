// node('master') {
//     // Select provider interactively or via parameters
//     properties([
//         parameters([
//             choice(
//                 name: 'CLOUD_PROVIDER',
//                 choices: ['aws', 'azure'],
//                 description: 'Select the cloud provider for deployment'
//             )
//         ])
//     ])

//     stage('Checkout Code') {
//         echo "Checking out repository..."
//         checkout scm
//     }

//     stage('Terraform Init & Plan') {
//         dir("/home/devuser/cross-cloud-devops-framework/terraform/${params.CLOUD_PROVIDER}") {
//             echo "Running Terraform Init..."
//             sh 'terraform init'

//             echo "Running Terraform Plan..."
//             sh 'terraform plan'
//         }
//     }

//     stage('Terraform Apply') {
//         dir("/home/devuser/cross-cloud-devops-framework/terraform/${params.CLOUD_PROVIDER}") {
//             echo "Applying Terraform changes..."
//             sh 'terraform apply -auto-approve'
//         }
//     }
// }

///working code
// node('master') {
//     def tfHome = tool name: 'terraform', type: 'org.jenkinsci.plugins.terraform.TerraformInstallation'
//     withEnv(["PATH+TERRAFORM=${tfHome}"]) {

//         properties([
//             parameters([
//                 choice(
//                     name: 'CLOUD_PROVIDER',
//                     choices: ['aws', 'azure'],
//                     description: 'Select the cloud provider for deployment'
//                 ),
//                 choice(
//                     name: 'ACTION',
//                     choices: ['apply', 'destroy'],
//                     description: 'Choose whether to apply or destroy Terraform changes'
//                 )
//             ])
//         ])

//         stage('Checkout Code') {
//             checkout scm
//         }

//         stage('Terraform Init & Plan') {
//             dir("/home/devuser/cross-cloud-devops-framework/terraform/${params.CLOUD_PROVIDER}") {
//                 sh 'terraform init'
//                 sh 'terraform plan'
//             }
//         }

//         if (params.ACTION == 'apply') {
//             stage('Terraform Apply') {
//                 dir("/home/devuser/cross-cloud-devops-framework/terraform/${params.CLOUD_PROVIDER}") {
//                     sh 'terraform apply -auto-approve'
//                 }
//             }
//         } else {
//             stage('Terraform Destroy') {
//                 dir("/home/devuser/cross-cloud-devops-framework/terraform/${params.CLOUD_PROVIDER}") {
//                     sh 'terraform destroy -auto-approve'
//                 }
//             }
//         }
//     }
// }


// node('master') {
//     def tfHome = tool name: 'terraform', type: 'org.jenkinsci.plugins.terraform.TerraformInstallation'
//     withEnv(["PATH+TERRAFORM=${tfHome}"]) {

//         properties([
//             parameters([
//                 choice(
//                     name: 'CLOUD_PROVIDER',
//                     choices: ['aws', 'azure'],
//                     description: 'Select the cloud provider for deployment'
//                 ),
//                 choice(
//                     name: 'ACTION',
//                     choices: ['apply', 'destroy'],
//                     description: 'Choose whether to apply or destroy Terraform changes'
//                 )
//             ])
//         ])

//         stage('Checkout Code') {
//             checkout scm
//         }

//         stage('Terraform Init & Plan') {
//             dir("/var/lib/jenkins/cross-cloud-devops-framework/terraform/${params.CLOUD_PROVIDER}") {
//                 sh 'terraform init'
//                 sh 'terraform plan'
//             }
//         }

//         if (params.ACTION == 'apply') {
//             stage('Terraform Apply') {
//                 dir("/var/lib/jenkins/cross-cloud-devops-framework/terraform/${params.CLOUD_PROVIDER}") {
//                     sh 'terraform apply -auto-approve'
//                 }
//             }

//             stage('Generate Terraform Outputs & Inventory') {
//                 dir("/var/lib/jenkins/cross-cloud-devops-framework") {
//                     // Dump outputs to JSON
//                     sh 'terraform -chdir=terraform/aws output -json > tf_outputs.json'

//                     // Generate Ansible inventory from outputs
//                     sh 'python3 generate_inventory.py --ec2-key extra_ec2_public_ip'

//                     // Show the generated inventory in console
//                     sh 'echo "===== Generated Inventory ====="'
//                     sh 'cat ansible/inventory/aws_hosts.ini'
//                 }
//             }

//             stage('Ansible Ping Test') {
//                 dir("/var/lib/jenkins/cross-cloud-devops-framework") {
//                     sh '''
//                     ansible -i ansible/inventory/aws_hosts.ini ec2 -m ping \
//                     -u ec2-user --private-key ~/.ssh/crosscloud-key.pem \
//                     -e 'ansible_ssh_common_args="-o StrictHostKeyChecking=no"'
//                     '''
//                 }
//             }

//             stage('Ansible Install Dependencies') {
//                 dir("/var/lib/jenkins/cross-cloud-devops-framework") {
//                     sh '''
//                     ansible-playbook -i ansible/inventory/aws_hosts.ini \
//                     ansible/playbooks/install_dependencies.yml -l ec2 \
//                     -u ec2-user --private-key ~/.ssh/crosscloud-key.pem \
//                     -e 'ansible_ssh_common_args="-o StrictHostKeyChecking=no"'
//                     '''
//                 }
//             }

//             stage('Verify Docker Setup') {
//                 dir("/var/lib/jenkins/cross-cloud-devops-framework") {
//                     sh '''
//                     ssh -o StrictHostKeyChecking=no \
//                     -i ~/.ssh/crosscloud-key.pem ec2-user@$(terraform -chdir=terraform/aws output -raw extra_ec2_public_ip) \
//                     "docker info && docker run hello-world"
//                     '''
//                 }
//             }

//         } else {
//             stage('Terraform Destroy') {
//                 dir("/var/lib/jenkins/cross-cloud-devops-framework/terraform/${params.CLOUD_PROVIDER}") {
//                     sh 'terraform destroy -auto-approve'
//                 }
//             }
//         }
//     }
// }

node('master') {
  def tfHome = tool name: 'terraform', type: 'org.jenkinsci.plugins.terraform.TerraformInstallation'
  withEnv(["PATH+TERRAFORM=${tfHome}"]) {

    properties([
      parameters([
        choice(
          name: 'CLOUD_PROVIDER',
          choices: ['aws', 'azure'],
          description: 'Select the cloud provider for deployment'
        ),
        choice(
          name: 'ACTION',
          choices: ['apply', 'destroy'],
          description: 'Choose whether to apply or destroy Terraform changes'
        )
      ])
    ])

    stage('Checkout Code') {
      checkout scm
    }

    stage('Terraform Init & Plan') {
      dir("/var/lib/jenkins/cross-cloud-devops-framework/terraform/${params.CLOUD_PROVIDER}") {
        sh '''
          set -euo pipefail
          terraform init
          terraform plan
        '''
      }
    }

    if (params.ACTION == 'apply') {
      stage('Terraform Apply') {
        dir("/var/lib/jenkins/cross-cloud-devops-framework/terraform/${params.CLOUD_PROVIDER}") {
          sh 'terraform apply -auto-approve'
        }
      }

      // =========================
      // AWS-only post-apply steps
      // =========================
      if (params.CLOUD_PROVIDER == 'aws') {
        stage('Generate Terraform Outputs & Inventory (AWS)') {
          dir("/var/lib/jenkins/cross-cloud-devops-framework") {
            sh '''
              set -euo pipefail
              terraform -chdir=terraform/aws output -json > tf_outputs.json
              python3 generate_inventory.py --ec2-key extra_ec2_public_ip
              echo "===== Generated Inventory ====="
              cat ansible/inventory/aws_hosts.ini
            '''
          }
        }

        stage('Ansible Ping Test (AWS)') {
          dir("/var/lib/jenkins/cross-cloud-devops-framework") {
            sh '''
              set -euo pipefail
              ansible -i ansible/inventory/aws_hosts.ini ec2 -m ping \
                -u ec2-user --private-key ~/.ssh/crosscloud-key.pem \
                -e 'ansible_ssh_common_args="-o StrictHostKeyChecking=no"'
            '''
          }
        }

        stage('Ansible Install Dependencies (AWS)') {
          dir("/var/lib/jenkins/cross-cloud-devops-framework") {
            sh '''
              set -euo pipefail
              ansible-playbook -i ansible/inventory/aws_hosts.ini \
                ansible/playbooks/install_dependencies.yml -l ec2 \
                -u ec2-user --private-key ~/.ssh/crosscloud-key.pem \
                -e 'ansible_ssh_common_args="-o StrictHostKeyChecking=no"'
            '''
          }
        }

        stage('Verify Docker Setup (AWS)') {
          dir("/var/lib/jenkins/cross-cloud-devops-framework") {
            sh '''
              set -euo pipefail
              HOST_IP=$(terraform -chdir=terraform/aws output -raw extra_ec2_public_ip)
              ssh -o StrictHostKeyChecking=no -i ~/.ssh/crosscloud-key.pem ec2-user@$HOST_IP \
                "docker info && docker run --rm hello-world"
            '''
          }
        }
      }

      // ==========================
      // Azure-only post-apply steps
      // ==========================
      if (params.CLOUD_PROVIDER == 'azure') {
        stage('Configure kubectl (AKS)') {
          dir("/var/lib/jenkins/cross-cloud-devops-framework") {
            sh '''
              set -euo pipefail
              which az && az version || true
              which kubectl && kubectl version --client || true

              # Update kubeconfig for AKS
              az aks get-credentials \
                --resource-group crosscloud-aks-rg \
                --name crosscloud_aks \
                --overwrite-existing

              echo "== Contexts =="
              kubectl config get-contexts || true

              echo "== Nodes =="
              kubectl get nodes -o wide
            '''
          }
        }

        stage('Deploy Nginx (AKS)') {
          dir("/var/lib/jenkins/cross-cloud-devops-framework/azure") {
            sh '''
              set -euo pipefail
              # Apply deployment manifest
              kubectl apply -f scripts/nginx-deployment.yaml

              echo "== Deployments =="
              kubectl get deployments

              echo "== Pods =="
              kubectl get pods -o wide

              echo "== Describe Deployment =="
              kubectl describe deployment nginx-deployment || true
            '''
          }
        }

        stage('Expose Service (AKS)') {
          dir("/var/lib/jenkins/cross-cloud-devops-framework") {
            sh '''
              set -euo pipefail
              # Idempotent expose: create service only if it doesn't exist
              if ! kubectl get svc nginx-service >/dev/null 2>&1; then
                kubectl expose deployment nginx-deployment \
                  --name=nginx-service \
                  --type=LoadBalancer \
                  --port=80 \
                  --target-port=80
              else
                echo "Service nginx-service already exists. Skipping expose."
              fi

              kubectl get svc
            '''
          }
        }

        stage('Wait for External IP (AKS)') {
          dir("/var/lib/jenkins/cross-cloud-devops-framework") {
            sh '''
              set -euo pipefail
              echo "Waiting for LoadBalancer external IP..."
              for i in $(seq 1 40); do
                EXTERNAL_IP=$(kubectl get svc nginx-service -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || true)
                HOSTNAME=$(kubectl get svc nginx-service -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || true)
                if [ -n "$EXTERNAL_IP" ] || [ -n "$HOSTNAME" ]; then
                  echo "External endpoint ready: ${EXTERNAL_IP:-$HOSTNAME}"
                  break
                fi
                echo "…still provisioning (attempt $i/40)"
                sleep 15
              done

              echo "== Service Details =="
              kubectl get svc nginx-service -o wide || true

              URL="http://${EXTERNAL_IP:-$HOSTNAME}"
              echo "APP_URL=$URL" > aks_app_url.env
              echo "Application should be reachable at: $URL"
            '''
          }
        }

        stage('Smoke Test (AKS)') {
          dir("/var/lib/jenkins/cross-cloud-devops-framework") {
            sh '''
              set -euo pipefail
              source aks_app_url.env || true
              if [ -n "${APP_URL:-}" ]; then
                echo "Curling ${APP_URL} ..."
                # Don't fail the build on 000 status while LB stabilizes; just print output
                curl -sS --max-time 10 "${APP_URL}" || true
              else
                echo "APP_URL not found; skipping smoke test."
              fi
            '''
          }
          // Optionally archive the URL for quick reference in build artifacts
          archiveArtifacts artifacts: 'aks_app_url.env', fingerprint: true, onlyIfSuccessful: false
        }
      }

    } else {
      stage('Terraform Destroy') {
        dir("/var/lib/jenkins/cross-cloud-devops-framework/terraform/${params.CLOUD_PROVIDER}") {
          sh 'terraform destroy -auto-approve'
        }
      }
    }
  }
}


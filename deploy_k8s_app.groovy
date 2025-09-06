node('master') {
    withEnv([
        "ANSIBLE_HOST_KEY_CHECKING=False",
        "KUBECONFIG=/var/lib/jenkins/cross-cloud-devops-framework/.kube/config"
    ]) {

        properties([
            parameters([
                choice(
                    name: 'CLOUD_PROVIDER',
                    choices: ['aws', 'azure'],
                    description: 'Select the cloud provider for Kubernetes app deployment'
                ),
                choice(
                    name: 'ACTION',
                    choices: ['deploy', 'validate', 'destroy'],
                    description: 'Choose whether to deploy, validate, or destroy Kubernetes app manifests'
                )
            ])
        ])

        stage('Checkout Code') {
            checkout scm
        }

        if (params.CLOUD_PROVIDER == 'aws') {
            // =========================
            // AWS EKS Deploy / Validate / Destroy
            // =========================
            stage('Configure kubeconfig (EKS)') {
                dir("/var/lib/jenkins/cross-cloud-devops-framework") {
                    sh '''
                        set -euo pipefail
                        echo "Setting up kubeconfig from Terraform output..."
                        terraform -chdir=terraform/aws output -raw kubeconfig > ${KUBECONFIG}
                    '''
                }
            }

            if (params.ACTION == 'deploy') {
                stage('Deploy K8s App on EKS') {
                    dir("/var/lib/jenkins/cross-cloud-devops-framework") {
                        sh '''
                            set -euo pipefail
                            echo "Deploying Kubernetes app manifests on EKS..."
                            bash ci-scripts/deploy.sh
                        '''
                    }
                }
            }

            if (params.ACTION == 'validate') {
                stage('Validate K8s App on EKS') {
                    dir("/var/lib/jenkins/cross-cloud-devops-framework") {
                        sh '''
                            set -euo pipefail
                            echo "Validating Kubernetes app on EKS..."
                            bash ci-scripts/validate.sh
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
                            bash ci-scripts/destroy.sh
                        '''
                    }
                }
            }
        }

        if (params.CLOUD_PROVIDER == 'azure') {
            // =========================
            // Azure AKS Deploy / Validate / Destroy
            // =========================
            stage('Configure kubeconfig (AKS)') {
                dir("/var/lib/jenkins/cross-cloud-devops-framework") {
                    sh '''
                        set -euo pipefail
                        echo "Configuring kubeconfig for AKS cluster..."
                        az aks get-credentials --resource-group crosscloud-aks-rg --name crosscloud_aks --overwrite-existing
                    '''
                }
            }

            if (params.ACTION == 'deploy') {
                stage('Deploy K8s App on AKS') {
                    dir("/var/lib/jenkins/cross-cloud-devops-framework") {
                        sh '''
                            set -euo pipefail
                            echo "Deploying Kubernetes app manifests on AKS..."
                            bash ci-scripts/deploy.sh
                        '''
                    }
                }
            }

            if (params.ACTION == 'validate') {
                stage('Validate K8s App on AKS') {
                    dir("/var/lib/jenkins/cross-cloud-devops-framework") {
                        sh '''
                            set -euo pipefail
                            echo "Validating Kubernetes app on AKS..."
                            bash ci-scripts/validate.sh
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
                            bash ci-scripts/destroy.sh
                        '''
                    }
                }
            }
        }
    }
}

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
                sh 'terraform init'
                sh 'terraform plan'
            }
        }

        if (params.ACTION == 'apply') {
            stage('Terraform Apply') {
                dir("/var/lib/jenkins/cross-cloud-devops-framework/terraform/${params.CLOUD_PROVIDER}") {
                    sh 'terraform apply -auto-approve'
                }
            }

            stage('Generate Terraform Outputs & Inventory') {
                dir("/var/lib/jenkins/cross-cloud-devops-framework") {
                    // Dump outputs to JSON
                    sh 'terraform -chdir=terraform/aws output -json > tf_outputs.json'

                    // Generate Ansible inventory from outputs
                    sh 'python3 generate_inventory.py --ec2-key extra_ec2_public_ip'

                    // Show the generated inventory in console
                    sh 'echo "===== Generated Inventory ====="'
                    sh 'cat ansible/inventory/aws_hosts.ini'
                }
            }

            stage('Ansible Ping Test') {
                dir("/var/lib/jenkins/cross-cloud-devops-framework") {
                    sh '''
                    ansible -i ansible/inventory/aws_hosts.ini ec2 -m ping \
                    -u ec2-user --private-key ~/.ssh/crosscloud-key.pem \
                    -e 'ansible_ssh_common_args="-o StrictHostKeyChecking=no"'
                    '''
                }
            }

            stage('Ansible Install Dependencies') {
                dir("/var/lib/jenkins/cross-cloud-devops-framework") {
                    sh '''
                    ansible-playbook -i ansible/inventory/aws_hosts.ini \
                    ansible/playbooks/install_dependencies.yml -l ec2 \
                    -u ec2-user --private-key ~/.ssh/crosscloud-key.pem \
                    -e 'ansible_ssh_common_args="-o StrictHostKeyChecking=no"'
                    '''
                }
            }

            stage('Verify Docker Setup') {
                dir("/var/lib/jenkins/cross-cloud-devops-framework") {
                    sh '''
                    ssh -o StrictHostKeyChecking=no \
                    -i ~/.ssh/crosscloud-key.pem ec2-user@$(terraform -chdir=terraform/aws output -raw extra_ec2_public_ip) \
                    "docker info && docker run hello-world"
                    '''
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

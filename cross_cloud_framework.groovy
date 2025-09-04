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

node('master') {
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
                description: 'Select whether to apply or destroy infrastructure'
            )
        ])
    ])

    stage('Checkout Code') {
        echo "Checking out repository..."
        checkout scm
    }

    stage('Terraform Init & Plan') {
        def tfHome = tool name: 'terraform', type: 'org.jenkinsci.plugins.terraform.TerraformInstallation'
        withEnv(["PATH+TERRAFORM=${tfHome}"]) {
            dir("/home/devuser/cross-cloud-devops-framework/terraform/${params.CLOUD_PROVIDER}") {
                echo "Running Terraform Init..."
                sh 'terraform init'

                echo "Running Terraform Plan..."
                sh 'terraform plan'
            }
        }
    }

    stage('Terraform Action') {
        def tfHome = tool name: 'terraform', type: 'org.jenkinsci.plugins.terraform.TerraformInstallation'
        withEnv(["PATH+TERRAFORM=${tfHome}"]) {
            dir("/home/devuser/cross-cloud-devops-framework/terraform/${params.CLOUD_PROVIDER}") {
                if (params.ACTION == 'apply') {
                    echo "Applying Terraform changes..."
                    sh 'terraform apply -auto-approve'
                } else if (params.ACTION == 'destroy') {
                    echo "Destroying Terraform infrastructure..."
                    sh 'terraform destroy -auto-approve'
                }
            }
        }
    }
}

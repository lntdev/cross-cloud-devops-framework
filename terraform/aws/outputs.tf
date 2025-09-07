output "cluster_name" {
  description = "EKS cluster name"
  value       = module.eks.cluster_name
}

output "cluster_endpoint" {
  description = "EKS cluster endpoint"
  value       = module.eks.cluster_endpoint
}

output "cluster_certificate_authority_data" {
  description = "EKS cluster CA certificate (base64-encoded)"
  value       = module.eks.cluster_certificate_authority_data
}

# Build kubeconfig for kubectl
output "kubeconfig" {
  description = "Kubeconfig to access the EKS cluster"
  value = <<EOT
apiVersion: v1
clusters:
- cluster:
    server: ${module.eks.cluster_endpoint}
    certificate-authority-data: ${module.eks.cluster_certificate_authority_data}
  name: ${module.eks.cluster_name}
contexts:
- context:
    cluster: ${module.eks.cluster_name}
    user: aws
  name: ${module.eks.cluster_name}
current-context: ${module.eks.cluster_name}
kind: Config
users:
- name: aws
  user:
    exec:
      apiVersion: client.authentication.k8s.io/v1beta1
      command: aws
      args:
        - "eks"
        - "get-token"
        - "--region"
        - "${var.aws_region}"
        - "--cluster-name"
        - "${module.eks.cluster_name}"
EOT
}

# EC2 instance IPs from your extra_ec2 module
# output "extra_ec2_public_ip" {
#   description = "Public IP of the extra EC2 instance"
#   value       = module.extra_ec2.public_ip
# }

# output "extra_ec2_private_ip" {
#   description = "Private IP of the extra EC2 instance"
#   value       = module.extra_ec2.private_ip
# }

output "extra_ec2_public_ip" {
  description = "Public IP of the extra EC2 instance"
  value       = module.extra_ec2.public_ip
}

output "extra_ec2_private_ip" {
  description = "Private IP of the extra EC2 instance"
  value       = module.extra_ec2.private_ip
}

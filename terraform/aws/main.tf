terraform {
  required_version = ">= 1.3.0, < 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.11"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.9"
    }
  }
}

# provider "aws" {
#   region = var.aws_region
# }

provider "aws" {
  region     = var.aws_region
  access_key = ""
  secret_key = ""
}

# Configure Kubernetes provider after EKS is created
provider "kubernetes" {
  host                   = module.eks.cluster_endpoint
  cluster_ca_certificate = base64decode(module.eks.cluster_certificate_authority_data)

  exec {
    api_version = "client.authentication.k8s.io/v1beta1"
    command     = "aws"
    args        = [
      "eks",
      "get-token",
      "--region", var.aws_region,
      "--cluster-name", module.eks.cluster_name
    ]
  }
}

# Configure Helm provider
provider "helm" {
  kubernetes {
    host                   = module.eks.cluster_endpoint
    cluster_ca_certificate = base64decode(module.eks.cluster_certificate_authority_data)

    exec {
      api_version = "client.authentication.k8s.io/v1beta1"
      command     = "aws"
      args        = [
        "eks",
        "get-token",
        "--region", var.aws_region,
        "--cluster-name", module.eks.cluster_name
      ]
    }
  }
}

# Latest Amazon Linux 2023 (x86_64) in your region (us-east-1)
data "aws_ssm_parameter" "al2023" {
  name = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-6.1-x86_64"
}


module "extra_ec2" {
  source        = "../modules/ec2"
  ami_id        = data.aws_ssm_parameter.al2023.value  # <-- use dynamic AMI
  instance_type = "t3.medium"
  key_name      = var.key_name
  subnet_id     = module.vpc.public_subnets[0] # ✅ use a public subnet
  name          = "extra-app-server"
}

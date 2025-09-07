module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 18.0"

  cluster_name    = var.cluster_name
  cluster_version = "1.29"
  cluster_endpoint_public_access = true

  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets

  eks_managed_node_groups = {
    default = {
      instance_types = [var.instance_type]
      desired_capacity = var.desired_capacity
      min_size        = 1
      max_size        = 4
    }
  }

  tags = {
    Environment = "dev"
    Project     = var.cluster_name
  }
}

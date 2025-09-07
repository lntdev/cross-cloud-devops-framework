variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "cluster_name" {
  description = "EKS Cluster name"
  type        = string
  default     = "crosscloud-eks"
}

variable "vpc_cidr" {
  description = "CIDR block for VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnets" {
  description = "Public subnets CIDR"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnets" {
  description = "Private subnets CIDR"
  type        = list(string)
  default     = ["10.0.3.0/24", "10.0.4.0/24"]
}

variable "desired_capacity" {
  description = "Number of worker nodes"
  type        = number
  default     = 2
}

variable "instance_type" {
  description = "EC2 instance type for workers"
  type        = string
  default     = "t3.medium"
}

variable "ami_id" {
  description = "Amazon Linux 2 AMI ID (us-east-1)"
  type        = string
  default     = "ami-0bdf93799014acdc4"
}

variable "key_name" {
  description = "The name of the existing EC2 Key Pair to allow SSH access"
  type        = string
  default     = "crosscloud-key"
}

# variable "subnet_ids" {
#   description = "List of subnet IDs where EC2 instance will be launched"
#   type        = list(string)
#   default     = module.vpc.public_subnet_ids
# }

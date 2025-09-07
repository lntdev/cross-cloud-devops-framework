# variable "ami_id" {}
# variable "instance_type" { default = "t3.medium" }
# variable "key_name" {}
# variable "subnet_id" {}
# variable "name" {}

variable "ami_id"        { type = string }
variable "instance_type" { type = string }
variable "key_name"      { type = string }
variable "subnet_id"     { type = string }
variable "name"          { type = string }

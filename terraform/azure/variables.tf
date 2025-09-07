variable "client_id" {
  default = ""

}

variable "client_secret" {
  default = ""

}

variable "node_count" {
  description = "number of nodes to deploy"
  default     = 1
}

variable "dns_prefix" {
  description = "DNS Suffix"
  default     = "cloud"
}

variable "cluster_name" {
  description = "AKS cluster name"
  default     = "crosscloud_aks"
}



variable "resource_group_name" {
  description = "name of the resource group to deploy AKS cluster in"
  default     = "crosscloud-aks-rg"
}

variable "acr_name" {
  type        = string
  description = "ACR name"
  default = "CROSSCLOUDAKSACRMANU"
}

variable "location" {
  description = "azure location to deploy resources"
  default     = "eastus"
}

variable "log_analytics_workspace_name" {
  default = "testLogAnalyticsWorkspaceName"
}

# refer https://azure.microsoft.com/global-infrastructure/services/?products=monitor for log analytics available regions
variable "log_analytics_workspace_location" {
  default = "EastUS"
}

# refer https://azure.microsoft.com/pricing/details/monitor/ for log analytics pricing

variable "log_analytics_workspace_sku" {
  default = "PerGB2018"
}

variable "subnet_name" {
  description = "subnet id where the nodes will be deployed"
  default     = "cloud-subnet"
}

variable "vnet_name" {
  description = "vnet id where the nodes will be deployed"
  default     = "cloud-vnet"
}

variable "subnet_cidr" {
  description = "the subnet cidr range"
  default     = "10.2.32.0/21"
}

variable kubernetes_version {
  description = "version of the kubernetes cluster"
  default     = "1.24.10"
}

# variable "vm_size" {
#   description = "size/type of VM to use for nodes"
#   default     = "Standard_D2_v2"
# }

variable "vm_size" {
  description = "size/type of VM to use for nodes"
  default = "Standard_B4ms"
}
variable "os_disk_size_gb" {
  description = "size of the OS disk to attach to the nodes"
  default     = 128
}

variable "max_pods" {
  description = "maximum number of pods that can run on a single node"
  default     = "20"
}

variable "address_space" {
  description = "The address space that is used the virtual network"
  default     = "10.2.0.0/16"
}
variable "min_count" {
  default     = 1
  description = "Minimum Node Count"
}
variable "max_count" {
  default     = 3
  description = "Maximum Node Count"
}
variable "system_node_count" {
  type        = number
  description = "Number of AKS worker nodes"
  default = 2
}

# variable "datadog_api_key" {
#   description = "API key for Datadog"
#   type        = string
#   sensitive   = true
# }

# variable "firewall_private_ip" {
#   description = "The private IP address of the firewall or Virtual Appliance to use as the next hop."
#   type        = string
# }





data "azurerm_kubernetes_service_versions" "current" {
  location = var.location
  include_preview = false  
}



resource "azurerm_kubernetes_cluster" "cluster" {
  name                = var.cluster_name
  location            = var.location
  resource_group_name = var.resource_group_name
  dns_prefix          = var.dns_prefix
  kubernetes_version  = data.azurerm_kubernetes_service_versions.current.latest_version
  private_cluster_enabled = false
  sku_tier = "Free"

  default_node_pool {
    name            = var.default_pool_name
    node_count      = var.node_count
    vm_size         = var.vm_size
    os_disk_size_gb = var.os_disk_size_gb
    vnet_subnet_id  = var.vnet_subnet_id
    max_pods        = var.max_pods
    type            = var.default_pool_type
    enable_node_public_ip = false
    enable_auto_scaling = true
    min_count           = var.min_count
    max_count           = var.max_count

    # tags = merge(
    # {
    #   "environment" = "rsacluster"
    # },
    # {
    #   "aadssh" = "True"
    # },
    # )
    
  }
 
  # network_profile {
  #   network_plugin     = var.network_plugin
  #   #load_balancer_sku  = "standard"
  #   outbound_type      = var.outbound_type
  #   #network_policy     = "calico"
  #   service_cidr       = var.service_cidr
  #   dns_service_ip     = "192.168.188.10"
  # }
  network_profile {
  network_plugin     = var.network_plugin
  outbound_type      = "loadBalancer"
  service_cidr       = var.service_cidr
  dns_service_ip     = "192.168.188.10"
}

  


  service_principal {
    client_id     = var.client_id
    client_secret = var.client_secret
  }
  linux_profile {
    admin_username = "ubuntu"
    ssh_key {
        key_data = file(var.ssh_public_key)
    }
  }


  # identity {
  #   type = "SystemAssigned"
  # }

  # tags = {
  #   Environment = "Development"
  # }

  lifecycle {
    prevent_destroy = false
  }
}

provider "helm" {
  kubernetes {
    host                   = azurerm_kubernetes_cluster.cluster.kube_config[0].host
    client_certificate     = base64decode(azurerm_kubernetes_cluster.cluster.kube_config[0].client_certificate)
    client_key             = base64decode(azurerm_kubernetes_cluster.cluster.kube_config[0].client_key)
    cluster_ca_certificate = base64decode(azurerm_kubernetes_cluster.cluster.kube_config[0].cluster_ca_certificate)
  }
}



resource "azurerm_monitor_diagnostic_setting" "aks_cluster" {
  name                       = "${azurerm_kubernetes_cluster.cluster.name}-audit"
  target_resource_id         = azurerm_kubernetes_cluster.cluster.id
  log_analytics_workspace_id = var.diagnostics_workspace_id

  log {
    category = "kube-apiserver"
    enabled  = true

    retention_policy {
      enabled = false
    }
  }

  log {
    category = "kube-controller-manager"
    enabled  = true

    retention_policy {
      enabled = false
    }
  }

  log {
    category = "cluster-autoscaler"
    enabled  = true

    retention_policy {
      enabled = false
    }
  }

  log {
    category = "kube-scheduler"
    enabled  = true

    retention_policy {
      enabled = false
    }
  }

  log {
    category = "kube-audit"
    enabled  = true

    retention_policy {
      enabled = false
    }
  }

  metric {
    category = "AllMetrics"
    enabled  = false

    retention_policy {
      enabled = false
    }
  }
}

---
id: kubernetes-model
title: Kubernetes Data Model
sidebar_label: Kubernetes Data Model
---

# Data Model

## KubeCluster



| Name | Type | Description |
|------|------|------|
| **clusterId** | string | Kubernetes Cluster Id |
| buildDate | number | |
| compiler | string | |
| gitCommit | string | |
| gitTreeState | string | |
| gitVersion | string | |
| goVersion | string | |
| graphEntityGroup | string | |
| graphEntityType | string | |
| graphUpdateTs | number | |
| major | string | |
| masterUrl | string | |
| minor | string | |
| platform | string | |



## KubeContainer



| Name | Type | Description |
|------|------|------|
| **clusterId** | string | Kubernetes Cluster Id |
| **name** | string | Kubernetes Container Name |
| containerID | string | |
| graphEntityGroup | string | |
| graphEntityType | string | |
| graphUpdateTs | number | |
| image | string | |
| imageID | string | |
| podUid | string | |
| ready | boolean | |
| restartCount | number | |
| startedAt | string | |



## KubeDaemonSet



| Name | Type | Description |
|------|------|------|
| **clusterId** | string | Kubernetes Cluster Id |
| **kind** | string | Kubernetes type (`Pod`, `Deployment`, etc.) |
| **name** | string | Kubernetes DaemonSet Name |
| **namespace** | string | Kubernetes DaemonSet Namespace |
| **uid** | string | Kubernetes UID |
| apiVersion | string | Kubernetes API Version |
| creationTimestamp | string | |
| creationTs | number | |
| graphEntityGroup | string | |
| graphEntityType | string | |
| graphUpdateTs | number | |
| resourceVersion | string | Kubernetes Resource Version |
| selfLink | string | Kubernetes API server resource path |
| generation | NUMBER | |
| label_tier | STRING | |
| label_addonmanager.kubernetes.io/mode | STRING | |
| annotation_deprecated.daemonset.template.generation | STRING | |
| annotation_aks.microsoft.com/release-time | STRING | |
| annotation_kubectl.kubernetes.io/last-applied-configuration | STRING | |
| label_component | STRING | |
| label_kubernetes.io/cluster-service | STRING | |



## KubeDeployment



| Name | Type | Description |
|------|------|------|
| **clusterId** | string | Kubernetes Cluster Id |
| **kind** | string | Kubernetes type (`Pod`, `Deployment`, etc.) |
| **name** | string | Kubernetes Deployment Name |
| **namespace** | string | Kubernetes Deployment Namespace |
| **uid** | string | Kubernetes UID |
| apiVersion | string | Kubernetes API Version |
| availableReplicas | number | |
| creationTimestamp | string | |
| creationTs | number | |
| graphEntityGroup | string | |
| graphEntityType | string | |
| graphUpdateTs | number | |
| observedGeneration | number | |
| replicas | number | |
| resourceVersion | string | Kubernetes Resource Version |
| selfLink | string | Kubernetes API server resource path |
| updatedReplicas | number | |
| label_version | STRING | |
| label_k8s-app | STRING | |
| label_component | STRING | |
| label_app | STRING | |
| generation | NUMBER | |
| label_tier | STRING | |
| label_addonmanager.kubernetes.io/mode | STRING | |
| annotation_deployment.kubernetes.io/revision | STRING | |
| annotation_kubectl.kubernetes.io/last-applied-configuration | STRING | |
| label_kubernetes.io/cluster-service | STRING | |



## KubeNamespace



| Name | Type | Description |
|------|------|------|
| **clusterId** | string | Kubernetes Cluster Id |
| **kind** | string | Kubernetes type (`Pod`, `Deployment`, etc.) |
| **name** | string | Kubernetes Namespace Name |
| **uid** | string | Kubernetes UID |
| apiVersion | string | Kubernetes API Version |
| creationTimestamp | string | |
| creationTs | number | |
| finalizers | array | |
| graphEntityGroup | string | |
| graphEntityType | string | |
| graphUpdateTs | number | |
| phase | string | |
| resourceVersion | string | Kubernetes Resource Version |
| selfLink | string | Kubernetes API server resource path |
| annotation_kubectl.kubernetes.io/last-applied-configuration | STRING | |



## KubeNode



| Name | Type | Description |
|------|------|------|
| **clusterId** | string | Kubernetes Cluster Id |
| **kind** | string | Kubernetes type (`Pod`, `Deployment`, etc.) |
| **name** | string | Kubernetes Node Name |
| **uid** | string | Kubernetes UID |
| allocatable_cpu | string | |
| allocatable_ephemeral-storage | string | |
| allocatable_hugepages-1Gi | string | |
| allocatable_hugepages-2Mi | string | |
| allocatable_memory | string | |
| allocatable_pods | string | |
| apiVersion | string | Kubernetes API Version |
| capacity_cpu | string | |
| capacity_ephemeral-storage | string | |
| capacity_hugepages-1Gi | string | |
| capacity_hugepages-2Mi | string | |
| capacity_memory | string | |
| capacity_pods | string | |
| creationTimestamp | string | |
| creationTs | number | |
| graphEntityGroup | string | |
| graphEntityType | string | |
| graphUpdateTs | number | |
| resourceVersion | string | Kubernetes Resource Version |
| selfLink | string | Kubernetes API server resource path |
| label_kubernetes.io/hostname | STRING | |
| label_kubernetes.azure.com/cluster | STRING | |
| label_beta.kubernetes.io/instance-type | STRING | |
| label_storagetier | STRING | |
| annotation_volumes.kubernetes.io/controller-managed-attach-detach | STRING | |
| label_failure-domain.beta.kubernetes.io/zone | STRING | |
| label_kubernetes.io/role | STRING | |
| label_beta.kubernetes.io/arch | STRING | |
| label_failure-domain.beta.kubernetes.io/region | STRING | |
| label_agentpool | STRING | |
| label_storageprofile | STRING | |
| annotation_node.alpha.kubernetes.io/ttl | STRING | |
| label_beta.kubernetes.io/os | STRING | |



## KubePod



| Name | Type | Description |
|------|------|------|
| **clusterId** | string | Kubernetes Cluster Id |
| **kind** | string | Kubernetes type (`Pod`, `Deployment`, etc.) |
| **name** | string | Kubernetes Pod Name |
| **namespace** | string | Kubernetes Pod Namespace |
| **uid** | string | Kubernetes UID |
| apiVersion | string | Kubernetes API Version |
| creationTimestamp | string | |
| creationTs | number | |
| dnsPolicy | string | |
| generateName | string | |
| graphEntityGroup | string | |
| graphEntityType | string | |
| graphUpdateTs | number | |
| hostIP | string | |
| hostNetwork | boolean | |
| nodeName | string | |
| daemonSetUid | string | |
| replicaSetUid | string | |
| phase | string | |
| podIP | string | |
| priority | number | |
| qosClass | string | |
| resourceVersion | string | Kubernetes Resource Version |
| restartPolicy | string | |
| schedulerName | string | |
| selfLink | string | Kubernetes API server resource path |
| serviceAccount | string | |
| serviceAccountName | string | |
| startTime | string | |
| terminationGracePeriodSeconds | number | |
| annotation_WSID | STRING | |
| annotation_agentVersion | STRING | |
| label_version | STRING | |
| label_k8s-app | STRING | |
| label_component | STRING | |
| label_app | STRING | |
| label_pod-template-generation | STRING | |
| label_tier | STRING | |
| annotation_dockerProviderVersion | STRING | |
| label_pod-template-hash | STRING | |
| annotation_aks.microsoft.com/release-time | STRING | |
| label_rsName | STRING | |
| label_kubernetes.io/cluster-service | STRING | |
| label_controller-revision-hash | STRING | |



## KubeReplicaSet



| Name | Type | Description |
|------|------|------|
| **clusterId** | string | Kubernetes Cluster Id |
| **kind** | string | Kubernetes type (`Pod`, `Deployment`, etc.) |
| **name** | string | Kubernetes ReplicaSet Name |
| **namespace** | string | Kubernetes ReplicaSet Namespace |
| **uid** | string | Kubernetes UID |
| apiVersion | string | Kubernetes API Version |
| creationTimestamp | string | |
| creationTs | number | |
| graphEntityGroup | string | |
| graphEntityType | string | |
| graphUpdateTs | number | |
| deploymentUid | string | |
| resourceVersion | string | Kubernetes Resource Version |
| selfLink | string | Kubernetes API server resource path |
| status_availableReplicas | number | |
| status_fullyLabeledReplicas | number | |
| status_observedGeneration | number | |
| status_readyReplicas | number | |
| status_replicas | number | |
| annotation_deployment.kubernetes.io/max-replicas | STRING | |
| label_version | STRING | |
| label_k8s-app | STRING | |
| label_component | STRING | |
| label_app | STRING | |
| generation | NUMBER | |
| annotation_deployment.kubernetes.io/revision | STRING | |
| label_pod-template-hash | STRING | |
| label_rsName | STRING | |
| label_kubernetes.io/cluster-service | STRING | |
| annotation_deployment.kubernetes.io/desired-replicas | STRING | |



## KubeService



| Name | Type | Description |
|------|------|------|
| **clusterId** | string | Kubernetes Cluster Id |
| **kind** | string | Kubernetes type (`Pod`, `Deployment`, etc.) |
| **name** | string | Kubernetes Service Name |
| **namespace** | string | Kubernetes Service Namespace |
| **uid** | string | Kubernetes UID |
| apiVersion | string | Kubernetes API Version |
| clusterIP | string | |
| clusterIpPort_TCP_ | number | |
| clusterIpPort_TCP_443 | number | |
| clusterIpPort_TCP_53 | number | |
| clusterIpPort_TCP_8080 | number | |
| clusterIpPort_TCP_8082 | number | |
| clusterIpPort_TCP_dns-tcp | number | |
| clusterIpPort_TCP_http | number | |
| clusterIpPort_TCP_https | number | |
| clusterIpPort_UDP_53 | number | |
| clusterIpPort_UDP_dns | number | |
| creationTimestamp | string | |
| creationTs | number | |
| externalTrafficPolicy | string | |
| graphEntityGroup | string | |
| graphEntityType | string | |
| graphUpdateTs | number | |
| healthCheckNodePort | number | |
| nodePort_TCP_http | number | |
| nodePort_TCP_https | number | |
| podRef | array | |
| resourceVersion | string | Kubernetes Resource Version |
| selector_app | string | |
| selector_k8s-app | string | |
| selfLink | string | Kubernetes API server resource path |
| sessionAffinity | string | |
| type | string | |
| label_kubernetes.io/name | STRING | |
| label_k8s-app | STRING | |
| label_component | STRING | |
| label_app | STRING | |
| label_addonmanager.kubernetes.io/mode | STRING | |
| label_provider | STRING | |
| annotation_kubectl.kubernetes.io/last-applied-configuration | STRING | |
| label_kubernetes.io/cluster-service | STRING | |





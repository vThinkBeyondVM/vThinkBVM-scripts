<#
.SYNOPSIS Getting Cluster usage/capacity data on CPU, Memory and Storage 
.NOTES  Author:  Vikas Shitole
.NOTES  Site:    www.vThinkBeyondVM.com
.NOTES  Reference: http://vthinkbeyondvm.com/category/powercli/
.NOTES Please add the vCenter server IP/credetails as per your environment
#>

Connect-VIServer -Server 10.92.166.82 -User Administrator@vsphere.local -Password xyz!23

$report = @()

Write-host "Report Generation is in Progress..."

foreach ($cluster in Get-Cluster   ){

  $row = '' | select ClusterName,CpuCapacity , CpuUsed, MemCapacity,MemUsed ,StorageCapacity,StorageUsed
  
$cluster = Get-Cluster -Name $clusterName
$cluster_view = Get-View ($cluster)
$resourceSummary=$cluster_view.GetResourceUsage()
$row.ClusterName =$cluster_view.Name
$row.CpuCapacity =$resourceSummary.CpuCapacityMHz
$row.CpuUsed =$resourceSummary.CpuUsedMHz
$row.MemCapacity =$resourceSummary.MemCapacityMB
$row.MemUsed =$resourceSummary.MemUsedMB
$row.StorageCapacity =$resourceSummary.StorageCapacityMB
$row.StorageUsed =$resourceSummary.StorageUsedMB

$report += $row
}
$report | Sort  ClusterName | Export-Csv -Path "D:Clusterstats.csv" #Please change the CSV file location

Write-host "Report Generation is completed, please chekc the CSV file"

<#
.SYNOPSIS 
    Retrieves usage and capacity data for CPU, Memory, and Storage for all clusters in a vCenter environment and exports the results to a CSV file.

.DESCRIPTION 
    This script connects to a specified vCenter Server and gathers resource usage and capacity statistics for each cluster. The collected data includes CPU capacity and usage (in MHz), memory capacity and usage (in MB), and storage capacity and usage (in MB). The results are exported to a CSV file for further analysis or reporting.

.PARAMETER None
    The script does not accept parameters. Please update the vCenter connection details and output CSV path as needed for your environment.

.EXAMPLE
    .\ClusterResources.ps1
    Runs the script, connects to the vCenter, collects cluster resource data, and exports it to the specified CSV file.

.NOTES
    Author:  Vikas Shitole
    Site:    www.vThinkBeyondVM.com
    Reference: http://vthinkbeyondvm.com/category/powercli/
    Please add the vCenter server IP/credentials as per your environment.
    Update the Export-Csv path as needed.

.OUTPUTS
    CSV file containing cluster resource usage and capacity statistics.
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

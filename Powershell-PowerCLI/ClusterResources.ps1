<#
.SYNOPSIS 
    Retrieves usage and capacity data for CPU, Memory, and Storage for all clusters in a vCenter environment and exports the results to a CSV file.

.DESCRIPTION 
    This script connects to a specified vCenter Server and gathers resource usage and capacity statistics for each cluster. The collected data includes CPU capacity and usage (in MHz), memory capacity and usage (in MB), and storage capacity and usage (in MB). The results are exported to a CSV file for further analysis or reporting.

.PARAMETER vCenter
    The IP address or FQDN of the vCenter Server to connect to.

.PARAMETER OutputCsv
    The full path to the CSV file where the report will be saved.

.EXAMPLE
    .\ClusterResources.ps1 -vCenter 10.92.166.82 -OutputCsv "C:\\Temp\\ClusterStats.csv"
    Runs the script, connects to the vCenter, collects cluster resource data, and exports it to the specified CSV file.

.NOTES
    Author:  Vikas Shitole
    Site:    www.vThinkBeyondVM.com
    Reference: http://vthinkbeyondvm.com/category/powercli/
    Please provide vCenter server IP/credentials as per your environment.
    Update the Export-Csv path as needed.

.OUTPUTS
    CSV file containing cluster resource usage and capacity statistics.
#>

param(
    [Parameter(Mandatory = $true)]
    [string]$vCenter,

    [Parameter(Mandatory = $true)]
    [string]$OutputCsv
)

try {
    $cred = Get-Credential -Message "Enter credentials for vCenter $vCenter"
    Connect-VIServer -Server $vCenter -Credential $cred -ErrorAction Stop
} catch {
    Write-Error "Failed to connect to vCenter: $_"
    exit 1
}

$report = @()
Write-Host "Report generation is in progress..."

foreach ($cluster in Get-Cluster) {
    try {
        $row = [PSCustomObject]@{
            ClusterName      = $null
            CpuCapacityMHz   = $null
            CpuUsedMHz       = $null
            MemCapacityMB    = $null
            MemUsedMB        = $null
            StorageCapacityMB= $null
            StorageUsedMB    = $null
        }
        $clusterView = Get-View -Id $cluster.Id
        $resourceSummary = $clusterView.GetResourceUsage()
        $row.ClusterName       = $clusterView.Name
        $row.CpuCapacityMHz    = $resourceSummary.CpuCapacityMHz
        $row.CpuUsedMHz        = $resourceSummary.CpuUsedMHz
        $row.MemCapacityMB     = $resourceSummary.MemCapacityMB
        $row.MemUsedMB         = $resourceSummary.MemUsedMB
        $row.StorageCapacityMB = $resourceSummary.StorageCapacityMB
        $row.StorageUsedMB     = $resourceSummary.StorageUsedMB
        $report += $row
    } catch {
        Write-Warning "Failed to process cluster $($cluster.Name): $_"
    }
}

try {
    $report | Sort-Object ClusterName | Export-Csv -Path $OutputCsv -NoTypeInformation -Force
    Write-Host "Report generation is completed. Please check the CSV file at: $OutputCsv"
} catch {
    Write-Error "Failed to export report to CSV: $_"
}

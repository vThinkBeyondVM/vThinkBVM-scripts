
<#
.SYNOPSIS
    Connects to a vCenter Server and generates a CSV report of all VMs with at least one disk enabled for "Multi-Writer" sharing.
    The report includes the VM Name by default, but can be customized to include additional columns as needed.

.DESCRIPTION
    This script scans all VMs and VMDKs in the specified vCenter Server for disks with "Multi-Writer" sharing enabled.
    By default, it scans the entire vCenter, but can be easily tweaked to scan by cluster, host, or datacenter (see examples below).
    The output is a CSV file containing the names of matching VMs.

.PARAMETER vCenter
    The vCenter Server IP/hostname, username, and password are hardcoded in the script. Please update these values to match your environment before running.

.OUTPUTS
    CSV file containing the VM names with at least one disk set to "Multi-Writer" sharing.
    The output file path can be changed as needed.

.EXAMPLE
    # Run the script after updating vCenter credentials and output path:
    .\VMMultiWriterReport.ps1

.NOTES
    Author:  Vikas Shitole
    Site:    www.vThinkBeyondVM.com
    Reference: http://vthinkbeyondvm.com/category/powercli/
    Please update the vCenter server IP/credentials as per your environment.
    Alternate solution by scanning VMX files: https://github.com/vThinkBeyondVM/vThinkBVM-scripts/blob/master/Powershell-PowerCLI/VMMultiWriterReport2.ps1

    To scope the report to a specific Datacenter, Datastore, Host, or Cluster, use the following examples:
        $myDatacenter = Get-Datacenter -Name "MyDatacenter"
        Get-VM -Location $myDatacenter

        $myDatastore = Get-Datastore -Name "MyDatastore"
        Get-VM -Datastore $myDatastore

        $myHost = Get-VMHost -Name "HostName"
        Get-VM -Location $myHost

        $myCluster = Get-Cluster -Name "ClusterName"
        Get-VM -Location $myCluster
#>

Write-host "Connecting to vCenter server.."
Set-PowerCLIConfiguration -InvalidCertificateAction Ignore -Confirm:$false -DisplayDeprecationWarnings:$false -Scope User
Connect-VIServer -Server 10.192.x.y -User administrator@vsphere.local -Password xyz@123


$report = @()
Write-host "Report Generation is in Progress..."

foreach ($vm in Get-VM  ){
$view = Get-View $vm
$settings=Get-AdvancedSetting -Entity $vm
   if ($view.config.hardware.Device.Backing.sharing -eq "sharingMultiWriter" -or $settings.value -eq "multi-writer"){
   $row = '' | select Name
      $row.Name = $vm.Name
   $report += $row
   }
   
}
$report | Sort Name | Export-Csv -Path "D:MultiWriter.csv" #Please change the CSV file location

Write-host "Report is generated successfully, please check the CSV file at specified location"

#If you want to generate report per Datacenter/Datastore/Host/Cluster, modify script using below code.

#$myDatacenter = Get-Datacenter -Name "MyDatacenter"
#Get-VM -Location $myDatacenter

#$myDatastore = Get-Datastore -Name "MyDatastore"
#Get-VM -Datastore $myDatastore

#$myHost=Get-VMHost -Name "HostName"
#Get-VM -Location $myHost

#$myCluster=Get-Cluster -Name "ClusterName"
#Get-VM -Location $myCluster


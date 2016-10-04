
<#
.SYNOPSIS: This script connects to the vCenter Server and prepares a report on All VMs with at-least one disk enabled with "Multi-Writer" sharing.
.Report will be generated as CSV file with "VM Name". This can be modified to add some more columns as needed
.VC IP/UserName/Password are hardcoded below, please change them as per your environment
.By default this script scans all the VMs/VMDK in the vCenter Server. It can be easily twicked to scan VM per cluster or host or datacenter

.NOTES  Author:  Vikas Shitole
.NOTES  Site:    www.vThinkBeyondVM.com
.NOTES  Reference: http://vthinkbeyondvm.com/category/powercli/
.NOTES Please add the vCenter server IP/credetails as per your environment
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


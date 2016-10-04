
<#
.SYNOPSIS: This script first downloads all the VMX files (per host/cluster/datacenter/VC)at specified file location
.Once downloaded, it will scan each VMX file one by one to get "multi-writer" entry inside VMX
.Finally it will list all matching VMX file names into specified file location. 
.As it downloads all the VMX files, it is going to take more time, that should be fine. 
.It is all right to download the VMX file when VM is up and running.
.If name of the VM is changed, VMX file can be different from VM display name (visible from inventory). 
.There are 2 file locations you need to specify. 1. Directory where VMX file will be downloaded 2. Output file.

.NOTES  Author:  Vikas Shitole
.NOTES  Site:    www.vThinkBeyondVM.com
.NOTES  Reference: http://vthinkbeyondvm.com/category/powercli/
.NOTES Please add the vCenter server IP/credetails as per your environment
.NOTES Alternatively you can use this script where API properties are used. https://github.com/vThinkBeyondVM/vThinkBVM-scripts/blob/master/Powershell-PowerCLI/VMMultiWriterReport.ps1
#>


Write-host "Connecting to vCenter server.."
Set-PowerCLIConfiguration -InvalidCertificateAction Ignore -Confirm:$false -DisplayDeprecationWarnings:$false -Scope User
Connect-VIServer -Server 10.192.67.143 -User administrator@vsphere.local -Password Admin!23

$tgtFolder = "C:\Temp\VMX\"  #Create this directory as per your environment
#$tgtString = 'scsi0:0.sharing="multi-writer"'
$tgtString = '"multi-writer"'
foreach ($vm in Get-VM ){

Get-VM -Name $vm.get_Name() | %{
  $dsName,$vmxPath = $_.ExtensionData.Config.Files.VmPathName.Split()
  $dsName = $dsName.Trim('[]')
  $ds = Get-Datastore -Name $dsName
  New-PSDrive -Location $ds -Name DS -PSProvider VimDatastore -Root "\" | Out-Null
  Copy-DatastoreItem -Item "DS:$vmxPath" -Destination $tgtFolder
  Remove-PSDrive -Name DS -Confirm:$false
}

}
Get-ChildItem -Path $tgtFolder -Filter "*.vmx" | Where {Get-Content -Path $_.FullName | Select-String -Pattern $tgtString} | Select Name | Out-File C:\test1.txt
Write-host "Execution is done... please check the file with all VMX file names."

# Below cmdlet can help to scan VMs per Datacenter/Datastore/Host/Cluster. Please modify the script as required. 

#$myDatacenter = Get-Datacenter -Name "MyDatacenter"
#Get-VM -Location $myDatacenter

#$myDatastore = Get-Datastore -Name "MyDatastore"
#Get-VM -Datastore $myDatastore

#$myHost=Get-VMHost -Name "HostName"
#Get-VM -Location $myHost

#$myCluster=Get-Cluster -Name "ClusterName"
#Get-VM -Location $myCluster

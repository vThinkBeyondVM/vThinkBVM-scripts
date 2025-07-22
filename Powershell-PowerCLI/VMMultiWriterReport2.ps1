
<#
.SYNOPSIS
    Downloads all VMX files for VMs in a vCenter (per host/cluster/datacenter/VC) to a specified directory, scans each for the "multi-writer" entry, and outputs a list of matching VMX file names to a specified file.

.DESCRIPTION
    This script connects to a vCenter server, downloads all VMX files for the VMs found, and searches each file for the presence of the "multi-writer" setting. It then outputs the names of VMX files containing this setting to an output file. The script is intended for environments where you need to audit VMs using multi-writer disks (e.g., for shared disk clustering scenarios).

.PARAMETER tgtFolder
    The directory where VMX files will be downloaded. Update this variable as per your environment.

.PARAMETER tgtString
    The string to search for in the VMX files. By default, it searches for '"multi-writer"'.

.PARAMETER OutputFile
    The file where the list of VMX files containing the target string will be written. Update this path as needed.

.PARAMETER vCenterServer
    The vCenter server to connect to. Update the Connect-VIServer command with your vCenter address and credentials.

.OUTPUTS
    A text file containing the names of VMX files that have the "multi-writer" entry.

.EXAMPLE
    # Update variables as needed, then run:
    .\VMMultiWriterReport2.ps1

.NOTES
    Author:  Vikas Shitole
    Site:    www.vThinkBeyondVM.com
    Reference: http://vthinkbeyondvm.com/category/powercli/ and https://communities.vmware.com/message/2269363#2269363
    Alternative: For a version using API properties, see https://github.com/vThinkBeyondVM/vThinkBVM-scripts/blob/master/Powershell-PowerCLI/VMMultiWriterReport.ps1
    Permissions: Requires appropriate permissions to connect to vCenter and access datastores.
    Note: Downloading all VMX files may take significant time depending on environment size. It is safe to download VMX files while VMs are running. If a VM's display name has changed, the VMX file name may differ from the inventory name.
    Customization: You can filter VMs by datacenter, datastore, host, or cluster by modifying the Get-VM command as shown in the commented examples at the end of the script.
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

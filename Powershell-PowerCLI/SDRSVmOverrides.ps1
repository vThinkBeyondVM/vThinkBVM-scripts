	<#
	.SYNOPSIS
    Re-enables SDRS on VMs that have SDRS disabled due to manual datastore selection.

.DESCRIPTION
    This script connects to a vCenter Server and identifies VMs in a specified Storage DRS (SDRS) datastore cluster (StoragePod) that have overridden the cluster-level SDRS configuration (i.e., SDRS is disabled for them). It then re-enables SDRS for those VMs and exports a report of affected VMs to a CSV file.
    
    Tested on vSphere 6.0. Please update the vCenter server IP, credentials, and CSV export path as per your environment.

.AUTHOR
    Vikas Shitole

.REFERENCE
    https://pubs.vmware.com/vsphere-65/index.jsp#com.vmware.wssdk.apiref.doc/vim.StorageResourceManager.html#configureStorageDrsForPod
    https://www.vmware.com/support/developer/PowerCLI/PowerCLI65R1/html/Set-PowerCLIConfiguration.html

.PARAMETER None
    Update the script to use your vCenter Server IP, credentials, and desired CSV export path.

.OUTPUTS
    CSV file containing the list of VMs for which SDRS was re-enabled.

.EXAMPLE
    # Run the script in PowerShell with appropriate permissions and PowerCLI installed:
    .\SDRSVmOverrides.ps1

.NOTES
    Product/Feature: vCenter Server/SDRS
    Description: Re-enable SDRS on VMs where SDRS was disabled when a particular datastore was selected for VMDK/VM placement.
    Please add the vCenter server IP/credentials as per your environment.
	#>

	Write-host "Connecting to vCenter server.."

	Set-PowerCLIConfiguration -InvalidCertificateAction Ignore -Confirm:$false -DisplayDeprecationWarnings:$false -Scope User
	Connect-VIServer -Server 10.192.1.2  -User administrator@vsphere.local -Password Admin!23

	$pod=Get-View -ViewType 'StoragePod' -filter @{"Name”=”DatastoreCluster”}
	$pod_Mor=$pod.MoRef;
	$report = @()
	if($pod -eq $null){
	Write-Host "SDRS POD not found on vCenter"
	}
	$vmOverrides=$pod.PodStorageDrsEntry.StorageDrsConfig.VmConfig;

	if($vmOverrides -eq $null){
	Write-Host "There is NO any VM which overrides SDRS cluster level configuration"
	}

	foreach ($vmOverride in $vmOverrides) {
	$row = '' | select VMName
	$vmMo=Get-View $vmOverride.Vm
	if($vmOverride.Enabled -eq $false){
	$spec = New-Object VMware.Vim.StorageDrsConfigSpec
	$spec.vmConfigSpec = New-Object VMware.Vim.StorageDrsVmConfigSpec[] (1)
	$spec.vmConfigSpec[0] = New-Object VMware.Vim.StorageDrsVmConfigSpec
	$spec.vmConfigSpec[0].operation = 'add'
	$spec.vmConfigSpec[0].info = New-Object VMware.Vim.StorageDrsVmConfigInfo
	$spec.vmConfigSpec[0].info.enabled=$true
	$spec.vmConfigSpec[0].info.vm=$vmOverride.Vm;
	$_this = Get-View -Id 'StorageResourceManager-StorageResourceManager'
	$_this.ConfigureStorageDrsForPod_Task($pod_Mor, $spec, $true)
	Write-Host "SDRS is re-enabled on this VM :" $vmMo.Name 
	$row.VMName =$vmMo.Name 
	 $report += $row
	}else{
	Write-Host "SDRS was already enabled on VM :" $vmMo.Name 
	}
	}
	$report | Export-Csv -Path "D:VMOverride.csv" #Please change the CSV file location as per your environment



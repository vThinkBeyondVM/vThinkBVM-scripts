
<#
.SYNOPSIS PowerCLI script:For spectre vulnerability : "How to confirm whether vCenter server, ESXi hypervisor & CPU microcode patches are applied or not?" 
.NOTES  Author:  Vikas Shitole
.NOTES  Site:    www.vThinkBeyondVM.com
.NOTES Please add the vCenter server IP/credetails as per your environment
.NOTES Relese notes: 
  VC 6.5 U1e: https://docs.vmware.com/en/VMware-vSphere/6.5/rn/vsphere-vcenter-server-65u1e-release-notes.html
  VC 6.0 U3d: https://docs.vmware.com/en/VMware-vSphere/6.0/rn/vsphere-vcenter-server-60u3d-release-notes.html
  VC 5.5 U3g: https://docs.vmware.com/en/VMware-vSphere/5.5/rn/vsphere-vcenter-server-55u3g-release-notes.html
#>

#vCenter Connection, please modify as per your env.

Connect-VIServer -Server 10.160.75.188 -Protocol https -User administrator@vsphere.local -Password Admin!23


function Get-VMLog{
<#
.SYNOPSIS
	Retrieve the virtual machine logs
.DESCRIPTION
	The function retrieves the logs from one or more
	virtual machines and stores them in a local folder
.NOTES
	Author:  Luc Dekens
.PARAMETER VM
	The virtual machine(s) for which you want to retrieve
	the logs.
.PARAMETER Path
	The folderpath where the virtual machines logs will be
	stored. The function creates a folder with the name of the
	virtual machine in the specified path.
.EXAMPLE
	PS> Get-VMLog -VM $vm -Path "C:\VMLogs"
.EXAMPLE
	PS> Get-VM | Get-VMLog -Path "C:\VMLogs"
#>
 
	param(
	[parameter(Mandatory=$true,ValueFromPipeline=$true)]
	[PSObject[]]$VM,
	[parameter(Mandatory=$true)]
	[string]$Path
	)
 
	process{
		foreach($obj in $VM){
			if($obj.GetType().Name -eq "string"){
				$obj = Get-VM -Name $obj
			}
		}
		$logPath = $obj.Extensiondata.Config.Files.LogDirectory
		$dsName = $logPath.Split(']')[0].Trim('[')
		$vmPath = $logPath.Split(']')[1].Trim(' ')
		$ds = Get-Datastore -Name $dsName
		$drvName = "MyDS" + (Get-Random)
		New-PSDrive -Location $ds -Name $drvName -PSProvider VimDatastore -Root '\' | Out-Null
		Copy-DatastoreItem -Item ($drvName + ":" + $vmPath + "vmware.log") -Destination ($Path + "\" + $obj.Name + "\") -Force:$true
		Remove-PSDrive -Name $drvName -Confirm:$false
	}
}
$report = @()

#As per release notes: vCenter builds which provide part of the hypervisor-assisted guest remediation of CVE-2017-5715 for guest operating systems 
$vc65u1eBuild = "7515524" #VC 6.5 U1e build

$vc60u3dBuild = "7464194" #VC 6.0 u3e build

$vc55u3gBuild = "7460778" #VC 5.5 u3g build

#getting service-Instance object to get vCenter build details
$Si = Get-View ServiceInstance
$vcVersion = $Si.Content.About.ApiVersion
$vcBuild = $Si.Content.About.Build
$vcBuildVersion=$Si.Content.About.FullName

if (($vcBuild -eq $vc65u1eBuild) -or ($vcBuild -eq $vc60u3deBuild) -or ($vcBuild -eq $vc55u3gBuild )) {
	Write-Host "vCenter build is matching with build specified on release notes"
	Write-Host "vCenter build number::" $vcBuild
	Write-Host "vCenter build & version ::" $vcBuildVersion
	Write-Host "VC is patched to correct build"
	$row = '' | select HostName, Status
 	$row.HostName = $vcBuildVersion
 	$row.Status="Patched"
 	$report += $row
}Else {
    Write-Host "vCenter build is NOT matching with build specified on release notes"
	Write-Host "vCenter build number::" $vcBuild
	Write-Host "vCenter build & version ::" $vcBuildVersion
	Write-Host "VC is NOT patched to correct build, please upgrade vCenter server."
	$row = '' | select HostName, Status
 	$row.HostName = $vcBuildVersion
 	$row.Status="Un-Patched"
 	$report += $row
}

#Location where vmware.log file gets downloaded
$drive="C:\"
$vmname="MyVM"

# Any of below lines must be found in vmware.log file to confirm microcode & VMware hypervisor patch
$pat1='Capability Found: cpuid.IBRS'
$pat2='Capability Found: cpuid.IBPB'
$pat3='Capabliity Found: cpuid.STIBP'

$clusterName="EVCCluster" #Your cluster name, script assumes that cluster is available
$cluster= Get-Cluster -Name $clusterName

#Get connected hosts from above cluster
$esxhosts = Get-Cluster $cluster | Get-VMHost -State Connected

#Counter used to give unique name to dummyvm, you can use any number of your choice.
$i=55

#Iterating through each host for VM creation and scanning vmware.log file
Foreach ($ESXHost in ($esxhosts)){
	$vm=$vmname+$i
	New-VM -Name $vm -VMHost $ESXHost -ResourcePool $cluster -DiskGB 1 -MemoryGB 1 -DrsAutomationLevel Disabled
	Start-VM -VM $vm -RunAsync -Confirm:$false  #DRS may powerON this VM on some other host inside the cluster
	$dest=$drive+$vm
	Get-VMLog -VM $vm -Path "C:\"
	if (Get-ChildItem -Path $dest -Filter "*.log" | Where {Get-Content -Path $_.FullName | Select-String -Pattern $pat1}){
 		$row = '' | select HostName, Status
 		$row.HostName = $($ESXHost.name)
 		$row.Status="Patched"
 		$report += $row
 		Write-Host "Matched pattern:"+$pat1+":"+$vm+":"+$($ESXHost.name) 
 }
 ElseIf (Get-ChildItem -Path $dest -Filter "*.log" | Where {Get-Content -Path $_.FullName | Select-String -Pattern $pat2}){
  	$row = '' | select HostName, Status
 	$row.HostName = $($ESXHost.name)
	$row.Status ="Patched"
    $report += $row
    Write-Host "Matched pattern:"+$pat2+":"+$vm+":"+$($ESXHost.name) 
 }
  ElseIf (Get-ChildItem -Path $dest -Filter "*.log" | Where {Get-Content -Path $_.FullName | Select-String -Pattern $pat3}){
   	$row = '' | select HostName, Status
  	$row.HostName = $($ESXHost.name)
 	$row.Status ="Patched"
 	$report += $row
   	Write-Host "Matched pattern:"+$pat3+":"+$vm+":"+$($ESXHost.name) 
 }
 Else{
  	$row = '' | select HostName, Status
 	$row.HostName = $($ESXHost.name)
	$row.Status ="Un-Patched"
  	$report += $row
 	Write-Host "Nothing matched on for VM on host:"$($ESXHost.name) 
 }
 Stop-VM -VM $vm -RunAsync -Confirm:$false
 #you can delete this VM as well to avoid any disk space consumption or minimize the disk size
 $i++
}
#Log the report into this CSV file, you can provide your name
$report | Sort HostName | Export-Csv -Path "D:PatchStatus1.csv"

#Disconnect the vCenter server
 Disconnect-VIServer -Confirm:$false

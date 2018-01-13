<#
.SYNOPSIS PowerCLI script: How to hide the speculative-execution control mechanism for virtual machines running on Intel Haswell and Broadwell processors ?" 
.NOTES  Author:  Vikas Shitole
.NOTES  Site:    www.vThinkBeyondVM.com
.NOTES Reference:  KB https://kb.vmware.com/s/article/52345 & http://vthinkbeyondvm.com/category/powercli/
.NOTES Please add the vCenter server IP/credetails as per your environment
#>
#vCenter Connection, please modify as per your env.

Connect-VIServer -Server 10.192.10.20 -Protocol https -User administrator@vsphere.local -Password VMware#123

$DCName="IndiaDC" # VC datacenter name, script assumes that datacenter is available
$dc= Get-Datacenter -Name $DCName
 
#Get connected hosts from above datacenter
$esxhosts = Get-Datacenter $dc | Get-VMHost -State Connected

#Get Common ESXi host root credentials, please do modify this code if ESXi hosts are having different credentials.
Write-Host "Please enter host root username and password:"
$Creds = Get-Credential

#Get Current Date/Time to append file names while taking backup
$FileDate = get-date -format ddMMyyyy"_"HHmm
$workaround='cpuid.7.edx = "----:00--:----:----:----:----:----:----"' 

#Iterating through each host 
Foreach ($ESXHost in ($esxhosts)){
if (($ESXHost.MaxEVCMode -eq "intel-haswell") -or ($ESXHost.MaxEVCMode -eq "intel-broadwell")){
    $startSSH = $false
    Write-Host "Modifying ESXi Host:"$ESXHost.Name
    #Start the SSH Service
    if($ESXHost | Get-VMHostService | Where { $_.Key -match "TSM-SSH" -and $_.Running -ne "true"}){
       Write-Host "SSH Service is not running on $ESXHost" -ForegroundColor Cyan
       Write-Host "Starting SSH Service ..." -ForegroundColor Green
       $ESXHost | Get-VMHostService | Where { $_.Key -match "TSM-SSH"} | Start-VMHostService Confirm:$false | Out-Null
       $startSSH = $true
    }
#Create a SSH Session using Posh-SSH module ($esxhosts.name or $Host.Name)
 $SSHSession = New-SSHSession -ComputerName $ESXHost.Name -Credential $Creds -Verbose -Force
 Write-Host "Backing up config file for $Host.Name" -ForegroundColor DarkCyan
 Invoke-SSHCommand -Command "cp /etc/vmware/config /etc/vmware/config_backup_$FileDate" -SessionId $SSHSession.SessionId | Out-Null
 Invoke-SSHCommand -Command "sed -i '$ a $workaround' /etc/vmware/config" -SessionId $SSHSession.SessionId | Out-Null
 
 Write-Host $ESXHost.Name "is" $ESXHost.MaxEVCMode ",hence workaround applied, checkout" 
 if($startSSH){
     Write-Host "Since SSH was disabled on" $ESXHost.Name " even before applying workaround, hence 
  disabling it back"
  $ESXHost | Get-VMHostService | Where { $_.Key -match "TSM-SSH"} |  Stop-VMHostService -Confirm:$false | Out-Null
  if($ESXHost | Get-VMHostService | Where { $_.Key -match "TSM-SSH" -and $_.Running -ne "true"}){
      Write-Host "SSH Service is disabled successfully on $ESXHost" -ForegroundColor Cyan
  }
 }
}else{
Write-Host $ESXHost.Name "is" $ESXHost.MaxEVCMode ",hence no change required" 
}
}
#Disconnect the vCenter server
 Disconnect-VIServer -Confirm:$false

# NOTE: These samples were developed on older vSphere versions. While they may work on the latest versions, they are not tested release over release. Use at your own risk.
<#
.SYNOPSIS
    PowerCLI script to hide the speculative-execution control mechanism (new CPU IDs) for virtual machines running on Intel Haswell and Broadwell processors.
.DESCRIPTION
    This script connects to a specified vCenter, iterates through all connected ESXi hosts in a given datacenter, and checks if the hosts expose new speculative-execution CPU IDs (IBPB, IBRS, STIBP). If so, it uses SSH to append a workaround line to /etc/vmware/config on the host to hide these CPU IDs, as per VMware KB 52345. The script also handles SSH service state and takes a backup of the config file before making changes.
.PARAMETER DCName
    The name of the vCenter datacenter to target. Modify the $DCName variable as needed.
.PARAMETER vCenter Connection
    Update the Connect-VIServer command with your vCenter server address, username, and password.
.PARAMETER Credentials
    The script prompts for ESXi host root credentials. All hosts must share the same credentials, or modify the script for per-host credentials.
.EXAMPLE
    # Update variables as needed, then run:
    ./hideNewCpuids.ps1
.NOTES
    Author:  Vikas Shitole
    Site:    www.vThinkBeyondVM.com
    Reference:  KB https://kb.vmware.com/s/article/52345
    Detailed blog: http://vthinkbeyondvm.com/powercli-script-hide-speculative-execution-control-mechanism-vms-running-intel-haswell-broadwell-processors/
    Please update vCenter server IP/credentials and datacenter name as per your environment.
#>

        #vCenter Connection, please modify as per your env.
        
        Connect-VIServer -Server 10.192.10.20 -Protocol https -User administrator@vsphere.local -Password VMware#!23
      
        #vCenter Connection, please modify as per your env.

        $DCName="IndiaDC" # VC datacenter name, script assumes that datacenter is available
        $dc= Get-Datacenter -Name $DCName
 
        #Get connected hosts from above datacenter
        $esxhosts = Get-Datacenter $dc | Get-VMHost -State Connected

        #Get Common ESXi host root credentials, please do modify this code if ESXi hosts are having different credentials.
        Write-Host "Please enter host root username and password:"
        $Creds = Get-Credential

        #Get Current Date/Time to append file names while taking backup
        $FileDate = get-date -format ddMMyyyy"_"HHmm
        # line to be added into /etc/vmware/config
        $workaround='cpuid.7.edx = "----:00--:----:----:----:----:----:----"' 

        #Iterating through each host 
        Foreach ($ESXHost in ($esxhosts)){

            $hostView= Get-View -ViewType HostSystem -Property Config.FeatureCapability -Filter @{"name"=$ESXHost.Name}

            $cpuFeatureCapabilities=$hostView.Config.FeatureCapability

        
            if (($ESXHost.MaxEVCMode -eq "intel-haswell") -or ($ESXHost.MaxEVCMode -eq "intel-broadwell")){

            $newCpuId=$false
            Foreach ($cpuCapability in $cpuFeatureCapabilities){
           
            if($cpuCapability.key -eq "cpuid.IBPB" -and $cpuCapability.value -eq 1){
                $newCpuId=$true
                break
            }elseif($cpuCapability.key -eq "cpuid.IBRS" -and $cpuCapability.value -eq 1){
               $newCpuId=$true
                break
            }elseif($cpuCapability.key -eq "cpuid.STIBP" -and $cpuCapability.value -eq 1){
                $newCpuId=$true
                break
            }

         }
         
         if($newCpuId -eq $true){
                $startSSH = $false
                #Start the SSH Service
                if($ESXHost | Get-VMHostService | Where { $_.Key -match "TSM-SSH" -and $_.Running -ne "true"}){
                Write-Host "SSH Service is not running on $ESXHost" -ForegroundColor Cyan
                Write-Host "Starting SSH Service ..." -ForegroundColor Green
                $ESXHost | Get-VMHostService | Where { $_.Key -match "TSM-SSH"} | Start-VMHostService Confirm:$false | Out-Null
                $startSSH = $true
            }
            #Create a SSH Session using Posh-SSH module
             $SSHSession = New-SSHSession -ComputerName $ESXHost.Name -Credential $Creds -Verbose -Force
            Write-Host "Backing up config file for $ESXHost.Name" -ForegroundColor DarkCyan
            Invoke-SSHCommand -Command "cp /etc/vmware/config /etc/vmware/config_backup_$FileDate" -SessionId $SSHSession.SessionId | Out-Null
            Invoke-SSHCommand -Command "sed -i '$ a $workaround' /etc/vmware/config" -SessionId $SSHSession.SessionId | Out-Null
 
             Write-Host $ESXHost.Name "is" $ESXHost.MaxEVCMode " & new cpuids found, hence edited the file" 
         if($startSSH){
             Write-Host "Since SSH was disabled on" $ESXHost.Name " even before file edit, hence 
            disabling it back"
            $ESXHost | Get-VMHostService | Where { $_.Key -match "TSM-SSH"} |  Stop-VMHostService -Confirm:$false | Out-Null
             if($ESXHost | Get-VMHostService | Where { $_.Key -match "TSM-SSH" -and $_.Running -ne "true"}){
              Write-Host "SSH Service is disabled successfully on $ESXHost" -ForegroundColor Cyan
             }
         }
       }else{
             Write-Host $ESXHost.Name "is" $ESXHost.MaxEVCMode "but new cpuids are not exposed on this host yet, hence no file edit required"
             }
     }else{
             Write-Host $ESXHost.Name "is" $ESXHost.MaxEVCMode ",hence no change required" 
        }
        }
        #Disconnect the vCenter server
         Disconnect-VIServer -Confirm:$false

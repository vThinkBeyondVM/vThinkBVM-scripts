# NOTE: These samples were developed on older vSphere versions. While they may work on the latest versions, they are not tested release over release. Use at your own risk.
<#
.SYNOPSIS
    Retrieves DRS VM-VM affinity and anti-affinity rules associated with each VM in a specified vSphere cluster.

.DESCRIPTION
    This script connects to a vCenter Server and generates a report of all DRS VM-VM rules (affinity/anti-affinity) associated with each virtual machine in a given cluster. The output is exported as a CSV file for further analysis.

.PARAMETER clusterName
    The name of the vSphere cluster to analyze. Update the $clusterName variable in the script as needed.

.PARAMETER vCenter Credentials
    Update the Connect-VIServer command with your vCenter Server address, username, and password.

.OUTPUTS
    CSV file containing VM names and their associated DRS rules. The file is saved to the path specified in the Export-Csv command (default: D:\VMsRules.csv).

.EXAMPLE
    # Update the cluster name and vCenter credentials, then run the script:
    PS> .\FindRulesForVMInCluster.ps1

.REQUIREMENTS
    - VMware PowerCLI module installed
    - Sufficient privileges to connect to vCenter and read cluster/VM/rule information

.NOTES
    Author:  Vikas Shitole
    Site:    www.vThinkBeyondVM.com
    Reference: http://vthinkbeyondvm.com/category/powercli/
    Please update the vCenter server IP/credentials and cluster name as per your environment.
    The script assumes the output directory exists and is writable.
#>

Connect-VIServer -Server 10.192.x.y -User Administrator@vsphere.local -Password xyz!23

$clusterName = "BLR" #Your cluster name
$cluster = Get-Cluster -Name $clusterName
$vms = Get-View ($cluster| Get-VM)
$cluster_view = Get-View ($cluster)

$report = @()

Write-host "Report Generation is in Progress..."

foreach ($vm in $vms  ){

  $row = '' | select VMName, Rules
  $rules=$cluster_view.FindRulesForVm($vm.MoRef)
  $ruleNameArray=" "
  #There can be more than one rule assciated with single VM
  foreach($rule in $rules){
  $ruleNameArray+=$rule.Name
  $ruleNameArray+=","
  }
  $row.VMName =$vm.Name
  $row.Rules = $ruleNameArray
  $report += $row
  }
$report | Sort Name | Export-Csv -Path "D:VMsRules.csv" #Please change the CSV file location

Write-host "Report Generation is completed, please chekc the CSV file"

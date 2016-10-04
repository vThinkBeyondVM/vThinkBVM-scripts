<#
.SYNOPSIS Getting DRS VM-VM affinity rules associated with each VMs in the cluster.
.NOTES  Author:  Vikas Shitole
.NOTES  Site:    www.vThinkBeyondVM.com
.NOTES  Reference: http://vthinkbeyondvm.com/category/powercli/ 
.NOTES Please add the vCenter server IP/credetails as per your environment

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

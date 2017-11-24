
# Description: Python sample to get VMs and its moid using vCenter server REST API.
# Reference:https://code.vmware.com/apis/191/vsphere-automation
#  Make sure you have "rest.py" file into your python directory.

import vcrest
import json
vcip="10.192.23.143" # vCenter server ip address/FQDN

#Get vCenter server session and can be used as needed. pass vcenter username & password
vcsession = vcrest.get_vc_session(vcip,"Administrator@vsphere.local","VMware1!")

#Get all the VMs from inventory using below method from "vcrest" module.
vms = vcrest.get_vms(vcip)

# Parsing the JSON response we got from above function call (it has all the Vms present in inventory
vm_response=json.loads(vms.text)
json_data=vm_response["value"]

print "VM names and its unique MOID"
print "============================"
for vm in json_data:
        print vm.get("name")+" :: "+vm.get("vm")
        #We are powering on all the VMs those are in powered off state
        if vm.get("power_state") == "POWERED_OFF":
                vcrest.poweron_vm(vm.get("vm"),vcip)

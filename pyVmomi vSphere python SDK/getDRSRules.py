# Author: Vikas Shitole
# Website: www.vThinkBeyondVM.com
# Product: vCenter server/vSphere DRS rules
# Description: Python script to get associated DRS rules for a Virtual Machine (from DRS cluster)
# Reference:http://vthinkbeyondvm.com/pyvmomi-tutorial-how-to-get-all-the-core-vcenter-server-inventory-objects-and-play-around/
# How to setup pyVmomi environment?: http://vthinkbeyondvm.com/how-did-i-get-started-with-the-vsphere-python-sdk-pyvmomi-on-ubuntu-distro/


from pyVim.connect import SmartConnect
from pyVmomi import vim
import ssl

s=ssl.SSLContext(ssl.PROTOCOL_TLSv1)
s.verify_mode=ssl.CERT_NONE
c= SmartConnect(host="10.161.2.3", user="Administrator@vsphere.local", pwd="VMware1!",sslContext=s)
content=c.content

#Below method gets all objects those are matching with provided "vimtype"
def get_all_objs(content, vimtype):
        obj = {}
        container = content.viewManager.CreateContainerView(content.rootFolder, vimtype, True)
        for managed_object_ref in container.view:
                obj.update({managed_object_ref: managed_object_ref.name})
        return obj

# Scanning a input VM inside inventory using special  python construct i.e. List comprehension
# It will get all the Vms and check whether input VM is available inside inventory or not, finally it returns list with matching condition
vmName="NTP-1"
vmToScan = [vm for vm in get_all_objs(content,[vim.VirtualMachine]) if vmName  == vm.name]
if len(vmToScan) == 0:
        print "VM is not found in inventory, please check the VM name passed"
        quit()

# Scanning a input cluster inside invetory the way we did for VM above. here also we used list comprehension.
clusterName="Cluster-India"
cluster = [cluster for cluster in get_all_objs(content,[vim.ClusterComputeResource]) if clusterName  == cluster.name]
if len(cluster) == 0:
        print "Cluster is not found inventory , please check the Cluster name passed"
        quit()

# Now we can call the method on input cluster by passing input VM as parameter, it returns array of rule objects associated with input VM.
ClusterRuleInfo=cluster[0].FindRulesForVm(vmToScan[0])

# Now iterate through rule objects and print the rule name
if len(ClusterRuleInfo) != 0:
        for rule in ClusterRuleInfo:
                print rule.name
else:
        print "There is no DRS rule associated with input VM"

# Author: Vikas Shitole
# Website: www.vThinkBeyondVM.com
# Product: vCenter server/EVC (Enhanced Compatibility Mode)
# Description: Script to get Max EVC Mode supported on all the hosts in the cluster
# Reference: http://vthinkbeyondvm.com/tutorial-how-to-manage-enhanced-vmotion-compatibility-evc-using-vsphere-python-sdk-pyvmomi
# How to setup pyVmomi environment?: http://vthinkbeyondvm.com/how-did-i-get-started-with-the-vsphere-python-sdk-pyvmomi-on-ubuntu-distro/


from pyVim.connect import SmartConnect, Disconnect
from pyVmomi import vim
import atexit
import ssl
import sys

s=ssl.SSLContext(ssl.PROTOCOL_TLSv1)
s.verify_mode=ssl.CERT_NONE
si= SmartConnect(host="10.160.50.60", user="Administrator@vsphere.local", pwd="VMware#12",sslContext=s)
content=si.content

# Your cluster name
cluster_name="vThinkBVMCluster"

# Below method helps us to get MOR of the object (vim type) that we passed.
def get_obj(content, vimtype, name):
        obj = None
        container = content.viewManager.CreateContainerView(content.rootFolder, vimtype, True)
        for c in container.view:
                if name:
                        if c.name == name:
                                obj = c
                                break
                        else:
                                obj = c
                                break
        return obj

#Cluster object
cluster = get_obj(content,[vim.ClusterComputeResource],cluster_name)
print "ClusterName::"+cluster.name

# Get all the hosts available inside cluster
hosts = cluster.host

#Iterate through each host to get MaxEVC mode supported on the host
for host in hosts:

        host_max_evcmode = host.summary.maxEVCModeKey

        if host_max_evcmode == None:
                print host.name+" does not support EVC"
        else:
                print host.name+"::"+host_max_evcmode

#Disconnect the vCenter server session.
atexit.register(Disconnect, si)

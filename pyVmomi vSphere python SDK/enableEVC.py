# Author: Vikas Shitole
# Website: www.vThinkBeyondVM.com
# Product: vCenter server/EVC (Enhanced Compatibility Mode)
# Description: Script to get enbale/disable EVC on cluster
# Reference: http://vthinkbeyondvm.com/tutorial-how-to-manage-enhanced-vmotion-compatibility-evc-using-vsphere-python-sdk-pyvmomi
# How to setup pyVmomi environment?: http://vthinkbeyondvm.com/how-did-i-get-started-with-the-vsphere-python-sdk-pyvmomi-on-ubuntu-distro/

from pyVim.connect import SmartConnect, Disconnect
from pyVmomi import vim
import atexit
import ssl
import sys

#Script to enable EVC on cluster

s=ssl.SSLContext(ssl.PROTOCOL_TLSv1)
s.verify_mode=ssl.CERT_NONE
si= SmartConnect(host="10.160.50.60", user="Administrator@vsphere.local", pwd="VMware#12",sslContext=s)
content=si.content

#Your cluster input
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
                                obj = None                                
        return obj

#Cluster object
cluster = get_obj(content,[vim.ClusterComputeResource],cluster_name)

# Major EVC cluster manager object
evc_cluster_manager=cluster.EvcManager()

print "ClusterName::"+cluster.name

# Configure EVC mode by passing EVCMode
task=evc_cluster_manager.ConfigureEvcMode_Task("intel-broadwell")

atexit.register(Disconnect, si)

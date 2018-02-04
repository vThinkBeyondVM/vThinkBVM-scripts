# Author: Vikas Shitole
# Website: www.vThinkBeyondVM.com
# Product: vCenter server/EVC (Enhanced Compatibility Mode)
# Description: Script to check whether EVC can be enabled on cluster with given EVCMode
# Reference: http://vthinkbeyondvm.com/tutorial-how-to-manage-enhanced-vmotion-compatibility-evc-using-vsphere-python-sdk-pyvmomi
# How to setup pyVmomi environment?: http://vthinkbeyondvm.com/how-did-i-get-started-with-the-vsphere-python-sdk-pyvmomi-on-ubuntu-distro/


from pyVim.connect import SmartConnect, Disconnect
from pyVmomi import vim
import atexit
import ssl
import sys
import time

#Script to check whether EVC can be enabled on cluster with given EVCMode

s=ssl.SSLContext(ssl.PROTOCOL_TLSv1)
s.verify_mode=ssl.CERT_NONE
si= SmartConnect(host="10.160.50.60", user="Administrator@vsphere.local", pwd="VMware#12",sslContext=s)
content=si.content
cluster_name="vThinkBVMCluster";

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
evc_cluster_manager=cluster.EvcManager()

print "ClusterName::"+cluster.name

task=evc_cluster_manager.CheckConfigureEvcMode_Task("intel-broadwell")
time.sleep(5)
checkResult= task.info.result

if(checkResult):
         print "EVC can not be enabled on this cluster, please take look at below reasons and hosts causing this issue"
        for result in checkResult:
                print result.error.msg
                for h in result.host:
                        print h.name
                print "---------------------"
else:
        print "EVC can be successfully enabled on the cluster with no issues"

atexit.register(Disconnect, si)

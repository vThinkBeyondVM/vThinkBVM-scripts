# Author: Vikas Shitole
# Website: www.vThinkBeyondVM.com
# Product: vCenter server/EVC (Enhanced Compatibility Mode)
# Description: Script to get EVC cluster state, current EVC Mode, cpu feature capability, maksed feature set
# Reference: http://vthinkbeyondvm.com/tutorial-how-to-manage-enhanced-vmotion-compatibility-evc-using-vsphere-python-sdk-pyvmomi
# How to setup pyVmomi environment?: http://vthinkbeyondvm.com/how-did-i-get-started-with-the-vsphere-python-sdk-pyvmomi-on-ubuntu-distro/

from pyVim.connect import SmartConnect, Disconnect
from pyVmomi import vim
import atexit
import ssl
import sys
#Script to get Max EVC Mode supported on all the hosts in the cluster
s=ssl.SSLContext(ssl.PROTOCOL_TLSv1)
s.verify_mode=ssl.CERT_NONE
si= SmartConnect(host="10.160.50.60", user="Administrator@vsphere.local", pwd="VMware#12",sslContext=s)
content=si.content
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
evc_cluster_manager=cluster.EvcManager()

evc_state=evc_cluster_manager.evcState
current_evcmode_key= evc_state.currentEVCModeKey

if(current_evcmode_key):
        print "Current EVC Mode::"+current_evcmode_key
else:
        print "EVC is NOT enabled on the cluster"
        quit()

feature_capabilities = evc_state.featureCapability

for capability in feature_capabilities:
        print "Feature Capability\n"
        print capability.featureName
        print capability.key
        print capability.value
        print "-------------"

features_masked = evc_state.featureMask

for mask in features_masked:
        print "Feature Masked\n"
        print mask.featureName
        print mask.key
        print mask.value
        print "-------------"


atexit.register(Disconnect, si)

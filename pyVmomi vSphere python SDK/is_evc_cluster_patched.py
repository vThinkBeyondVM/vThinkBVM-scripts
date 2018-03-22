# Author: Vikas Shitole
# Product: vCenter server/EVC (Enhanced Compatibility Mode): Applicable to vSphere 6.0 and 6.5 (Not applicable for vSphere 5.5 EVC clusters)
# Description: Script to check whether EVC cluster and its hosts are patched or not : vCenter/ESXi patches for Spectre vulnerability.
# Reference: https://kb.vmware.com/s/article/52085
# How to setup pyVmomi environment?: http://vthinkbeyondvm.com/how-did-i-get-started-with-the-vsphere-python-sdk-pyvmomi-on-ubuntu-distro/

from pyVim.connect import SmartConnect, Disconnect
from pyVmomi import vim
import atexit
import ssl
import sys
import argparse
import getpass

# Script to confirm whether EVC cluster is patched or not for Spectre vulenerability.

def get_args():
    """ Get arguments from CLI """
    parser = argparse.ArgumentParser(
        description='Arguments for talking to vCenter')

    parser.add_argument('-s', '--host',
                        required=True,
                        action='store',
                        help='vSpehre service to connect to')

    parser.add_argument('-o', '--port',
                        type=int,
                        default=443,
                        action='store',
                        help='Port to connect on')

    parser.add_argument('-u', '--user',
                        required=True,
                        action='store',
                        help='Username to use')

    parser.add_argument('-p', '--password',
                        required=False,
                        action='store',
                        help='Password to use')

    parser.add_argument('-c', '--cluster',
                        required=True,
                        action='store',
                        default=None,
                        help='Name of the cluster you wish to check')	

    args = parser.parse_args()

    if not args.password:
        args.password = getpass.getpass(
            prompt='Enter vCenter password:')

    return args




# Below method helps us to get MOR of the object (vim type) that we passed.
def get_obj(content, vimtype, name):
 obj = None
 container = content.viewManager.CreateContainerView(content.rootFolder, vimtype, True)
 for c in container.view:
  if name and c.name == name:
   obj = c
   break
 container.Destroy()
 return obj


args = get_args()
s=ssl.SSLContext(ssl.PROTOCOL_TLSv1)
s.verify_mode=ssl.CERT_NONE
si= SmartConnect(host=args.host, user=args.user, pwd=args.password,sslContext=s)
content=si.content
cluster_name=args.cluster

#Cluster object
cluster = get_obj(content,[vim.ClusterComputeResource],cluster_name)
if(not cluster):
 print ("Cluster not found, please enter correct EVC cluster name")
 quit()

print ("Cluster Name:"+cluster.name)
evc_cluster_manager=cluster.EvcManager()


evc_state=evc_cluster_manager.evcState
current_evcmode_key= evc_state.currentEVCModeKey

if(current_evcmode_key):
 print ("Current EVC Mode::"+current_evcmode_key)
else:
 print ("EVC is NOT enabled on the cluster, please enable it first")
 quit()

feature_capabilities = evc_state.featureCapability

flag=False
for capability in feature_capabilities:


    if(capability.key in ["cpuid.STIBP", "cpuid.IBPB","cpuid.IBRS"] and capability.value=="1"):
        print ("Found::"+capability.key)
        flag=True

if(not flag):
    print ("No new cpubit found on EVC cluster,hence cluster is NOT fully patched/upgraded")
else:
    print ("EVC cluster is patched, enjoy!, this also confirms all the hosts inside this EVC cluster are patched as well")


atexit.register(Disconnect, si)




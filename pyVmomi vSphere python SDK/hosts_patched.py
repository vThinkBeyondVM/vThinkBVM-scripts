# Author: Vikas Shitole
# Product: vCenter server
# Description: Script to confirm whether both hypervisor and microcode patches are applied or not : vCenter/ESXi patches for Spectre vulnerability.
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

# Get all the hosts available inside cluster
hosts = cluster.host

#Iterate through each host to get MaxEVC mode supported on the host
for host in hosts:
 print ("----------------------------------")
 print ("Host:"+host.name)
 feature_capabilities = host.config.featureCapability
 flag=False
 for capability in feature_capabilities:
  if(capability.key in ["cpuid.STIBP", "cpuid.IBPB","cpuid.IBRS"] and capability.value=="1"):
   print ("Found::"+capability.key)
   flag=True
 if(not flag):
  print ("No new  cpubit found, hence "+host.name+" is NOT patched")
 else:
  print ("New CPU bit is found, hence "+host.name+" is patched")
	

atexit.register(Disconnect, si)





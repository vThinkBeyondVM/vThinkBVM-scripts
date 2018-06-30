# Author: Vikas Shitole
# Product: vCenter server
# Description: Script to confirm whether vCenter server, hypervisor and microcode patches are applied or not : vCenter/ESXi patches for SSBD vulnerability.
# Reference: 
# 1. https://kb.vmware.com/s/article/55111 
# 2. http://vthinkbeyondvm.com/pyvmomi-script-to-confirm-speculative-store-bypass-disable-ssbd-mitigation-on-vsphere-patches/
# How to setup pyVmomi environment?: 
# Linux: http://vthinkbeyondvm.com/how-did-i-get-started-with-the-vsphere-python-sdk-pyvmomi-on-ubuntu-distro/
# Windows: http://vthinkbeyondvm.com/getting-started-with-pyvmomi-on-windows-supports-vsphere-6-7/

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
s=ssl.SSLContext(ssl.PROTOCOL_SSLv23) # # For VC 6.5/6.0 s=ssl.SSLContext(ssl.PROTOCOL_TLSv1)
s.verify_mode=ssl.CERT_NONE
si= SmartConnect(host=args.host, user=args.user, pwd=args.password,sslContext=s)
content=si.content
cluster_name=args.cluster

print ("-------------------------------------")
#Check whether vCenter server is patched or not
supported_evc_mode=si.capability.supportedEVCMode
# It is not required check only "ivy-bridge" EVC mode, you can pass anything from "intel-penryn" onwards
for evc_mode in supported_evc_mode:
    if(evc_mode.key == "intel-ivybridge"):
        ivy_masks=evc_mode.featureMask
        break

vCenter_patched=False
for capability in ivy_masks:
  if(capability.key in ["cpuid.SSBD"] and capability.value=="Val:1"):
   print ("Found::"+capability.key)
   vCenter_patched=True
if(not vCenter_patched):
  print ("No new cpubit found, hence vCenter server is NOT patched")
else:
  print ("New CPU bit is found, hence vCenter Server is patched")
print ("Current vCenter server build::"+si.content.about.fullName)

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
  if(capability.key in ["cpuid.SSBD"] and capability.value=="1"):
   print ("Found::"+capability.key)
   flag=True
 if(not flag):
  print ("No new cpubit found, hence "+host.name+" is NOT patched")
 else:
  print ("New CPU bit is found, hence "+host.name+" is patched")
	

atexit.register(Disconnect, si)

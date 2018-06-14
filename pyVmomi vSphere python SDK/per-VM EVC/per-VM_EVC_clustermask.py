# Author: Vikas Shitole
# Website: www.vThinkBeyondVM.com
# Product: vCenter server/ per-VM EVC (Enhanced Compatibility Mode)
# Description: Script to get enbale/disable per-VM EVC on VM (featureMask copied from cluster)
# Reference: 
# How to setup pyVmomi environment?:
# On linux:  http://vthinkbeyondvm.com/how-did-i-get-started-with-the-vsphere-python-sdk-pyvmomi-on-ubuntu-distro/
# On windows: http://vthinkbeyondvm.com/getting-started-with-pyvmomi-on-windows-supports-vsphere-6-7/
 
from pyVim.connect import SmartConnect
import ssl
from pyVmomi import vim
import atexit
import sys
import argparse
import getpass
 
 
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

    parser.add_argument('-v', '--vmname',
                        required=True,
                        action='store',
                        default=None,
                        help='Name of the VM to be configured per VM EVC')	
										

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
s=ssl.SSLContext(ssl.PROTOCOL_SSLv23) # For VC 6.5/6.0 s=ssl.SSLContext(ssl.PROTOCOL_TLSv1)
s.verify_mode=ssl.CERT_NONE
 
si= SmartConnect(host=args.host, user=args.user, pwd=args.password, sslContext=s)
content=si.content
vm= get_obj(content, [vim.VirtualMachine],args.vmname)
cluster_name="EVCCluster" #Pass your EVC cluster name

if(vm and vm.capability.perVmEvcSupported):
        print ("VM available in vCenter server and it supports perVm EVC, thats good")
else:
        print ("VM either NOT found or perVMEvc is NOT supported on the VM")
        quit()

#Cluster object
cluster = get_obj(content,[vim.ClusterComputeResource], cluster_name )

if(cluster):
        print ("Cluster available in vCenter server, thats good")
else:
        print ("Cluster is NOT available in vCenter server, please enter correct name")
        quit()
		
evc_cluster_manager=cluster.EvcManager()

evc_state=evc_cluster_manager.evcState
current_evcmode_key= evc_state.currentEVCModeKey

if(current_evcmode_key):
        print ("Current cluster EVC Mode::"+current_evcmode_key)
else:
        print ("EVC is NOT enabled on the cluster")
        quit()
features_masked = evc_state.featureMask
	 
vm.ApplyEvcModeVM_Task(features_masked,True)
print ("ApplyEvcModeVM_Task() API is invoked, check out your H5 client")

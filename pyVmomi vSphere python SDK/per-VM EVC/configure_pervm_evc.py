# Author: Vikas Shitole
# Website: www.vThinkBeyondVM.com
# Product: vCenter server/ per-VM EVC (Enhanced Compatibility Mode)
# Description: Script to get enbale/disable per-VM EVC on VM
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

if(vm and vm.capability.perVmEvcSupported):
        print ("VM available in vCenter server and it supports perVm EVC, thats good")
else:
        print ("VM either NOT found or perVMEvc is NOT supported on the VM")
        quit()

supported_evc_mode=si.capability.supportedEVCMode
for evc_mode in supported_evc_mode:
	if(evc_mode.key == "intel-ivybridge"):
		ivy_mask=evc_mode.featureMask
		break
	 
vm.ApplyEvcModeVM_Task(ivy_mask,True)
print ("ApplyEvcModeVM_Task() API is invoked, check out your H5 client")

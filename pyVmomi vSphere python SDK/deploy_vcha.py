# Author: Vikas Shitole
# Website: www.vThinkBeyondVM.com
# Product: vCenter server/vCenter Server High Availability (VCHA)
# Description: Python script to deploy vCenter server HA 
# Reference:http://vthinkbeyondvm.com/how-to-manage-vcenter-server-ha-using-vsphere-python-sdk-pyvmomi-part-1/
# How to setup pyVmomi environment?: http://vthinkbeyondvm.com/how-did-i-get-started-with-the-vsphere-python-sdk-pyvmomi-on-ubuntu-distro/

from pyVim.connect import SmartConnect
from pyVmomi import vim
import ssl
# Deploying vCenter HA in basic mode using self managed VC 

# For VC 6.5/6.0
#s=ssl.SSLContext(ssl.PROTOCOL_TLSv1)
# For VC 6.7
s = ssl.SSLContext(ssl.PROTOCOL_SSLv23)
s.verify_mode=ssl.CERT_NONE
si= SmartConnect(host="10.161.34.35", user="Administrator@vsphere.local", pwd="VMware#12",sslContext=s)
content=si.content

#Parameters required are hardcoded below, please do change as per your environment.

vcha_network_name="VCHA" #port group name, I am using standard switch.
vcha_dc_name="IndiaDC"    #Datacenter name 
vcha_subnet_mask="255.255.255.0"  #Subnect mask for vCenter HA/Private network
active_vcha_ip="192.168.0.1"    # Active node vCenter HA IP
passive_vcha_ip="192.168.0.2"   # Passive node vCenter HA IP
witness_vcha_ip="192.168.0.3"   # Witness node vCenter HA IP
active_vcha_vm_name="vThinkBVM-VC1" #Active node/VC VM name
active_vc_username="Administrator@vsphere.local"  #Active VC username
active_vc_password="VMware#23"  #Active VC password
active_vc_url="https://10.61.34.35"  #Active VC public IP
active_vc_thumbprint="55:A9:C5:7E:0C:CD:46:26:D3:5C:C2:92:B7:0F:A7:91:E5:CD:0D:5D" #Active VC thumbprint
passive_vc_datastore="SharedVMFS-1"  #Passive node datastore
witness_vc_datastore="SharedVMFS-2"  #Witness node datastore

vcha=si.content.failoverClusterConfigurator #Getting managed object responsible for vCenter HA deployment

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


vcha_network=get_obj(content,[vim.Network],vcha_network_name) 
vcha_dc=get_obj(content,[vim.Datacenter],vcha_dc_name)

#I would highly recommend  to read vSphere API reference for "failoverClusterConfigurator", this will help to understand below specs.
 
deployment_spec=vim.vcha.FailoverClusterConfigurator.VchaClusterDeploymentSpec()

#Active node related data/parameter population
active_nw_config_spec=vim.vcha.FailoverClusterConfigurator.ClusterNetworkConfigSpec()
active_nw_config_spec.networkPortGroup=vcha_network
active_ipSettings=vim.vm.customization.IPSettings()
active_ipSettings.subnetMask = vcha_subnet_mask
active_ip_spec=vim.vm.customization.FixedIp()
active_ip_spec.ipAddress= active_vcha_ip
active_ipSettings.ip=active_ip_spec
active_nw_config_spec.ipSettings=active_ipSettings
deployment_spec.activeVcNetworkConfig=active_nw_config_spec

#Active node service locator 
active_vc_spec=vim.vcha.FailoverClusterConfigurator.SourceNodeSpec()
active_vc_vm=get_obj(content,[vim.VirtualMachine],active_vcha_vm_name)
active_vc_spec.activeVc=active_vc_vm
service_locator=vim.ServiceLocator()
cred=vim.ServiceLocator.NamePassword()
cred.username=active_vc_username
cred.password=active_vc_password
service_locator.credential=cred
service_locator.instanceUuid=si.content.about.instanceUuid
service_locator.url=active_vc_url  #Source active VC
service_locator.sslThumbprint=active_vc_thumbprint
active_vc_spec.managementVc=service_locator
deployment_spec.activeVcSpec=active_vc_spec

#Passive node configuration spec
passive_vc_spec=vim.vcha.FailoverClusterConfigurator.PassiveNodeDeploymentSpec()
passive_ipSettings=vim.vm.customization.IPSettings()
passive_ipSettings.subnetMask = vcha_subnet_mask
passive_ip_spec=vim.vm.customization.FixedIp()
passive_ip_spec.ipAddress= passive_vcha_ip
passive_ipSettings.ip=passive_ip_spec
passive_vc_spec.ipSettings=passive_ipSettings
passive_vc_spec.folder=vcha_dc.vmFolder
passive_vc_spec.nodeName= active_vcha_vm_name+"-passive"
passive_datastore=get_obj(content,[vim.Datastore],passive_vc_datastore)
passive_vc_spec.datastore=passive_datastore
deployment_spec.passiveDeploymentSpec=passive_vc_spec

#Witness node configuration spec
witness_vc_spec=vim.vcha.FailoverClusterConfigurator.NodeDeploymentSpec()
witness_ipSettings=vim.vm.customization.IPSettings()
witness_ipSettings.subnetMask = vcha_subnet_mask
witness_ip_spec=vim.vm.customization.FixedIp()
witness_ip_spec.ipAddress= witness_vcha_ip
witness_ipSettings.ip=witness_ip_spec
witness_vc_spec.ipSettings=witness_ipSettings
witness_vc_spec.folder=vcha_dc.vmFolder
witness_vc_spec.nodeName=active_vcha_vm_name+"-witness"
witness_datastore=get_obj(content,[vim.Datastore],witness_vc_datastore)
witness_vc_spec.datastore=witness_datastore
deployment_spec.witnessDeploymentSpec=witness_vc_spec

# Calling the method we aimed to invoke by passing complete deployment spec

task= vcha.deployVcha_Task(deployment_spec)

if(task.info.state == "running"):
        print("VCHA deployment is started, it will take few minutes, please monitor web client for its completion")

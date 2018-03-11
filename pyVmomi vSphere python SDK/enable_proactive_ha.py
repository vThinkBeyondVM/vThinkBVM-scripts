# Author: Vikas Shitole
# Website: www.vThinkBeyondVM.com
# Product: vCenter server/Proactive HA
# Description: #Script to enable Proactive HA
# Reference: http://vthinkbeyondvm.com/category/vsphere-api/
# How to setup pyVmomi environment?: http://vthinkbeyondvm.com/how-did-i-get-started-with-the-vsphere-python-sdk-pyvmomi-on-ubuntu-distro/

from pyVim.connect import SmartConnect, Disconnect
from pyVmomi import vim
import atexit
import ssl
import sys
import time

s=ssl.SSLContext(ssl.PROTOCOL_TLSv1)
s.verify_mode=ssl.CERT_NONE
si= SmartConnect(host="10.161.20.30", user="Administrator@vsphere.local", pwd="VMware#123",sslContext=s)
content=si.content

#Cluster where ProactiveHA will be enbaled
cluster_name="ClusterProHA"

# Managed object that exposes all the methods to configure & manage proactiveHA
health_update_mgr=content.healthUpdateManager

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
if not cluster:
        print "Cluster is NOT found, please enter correct cluster name"
        sys.exit()
cluster_spec=vim.cluster.ConfigSpecEx()
drs_enabled = cluster.configuration.drsConfig.enabled

if (not drs_enabled):
        drs_info=vim.cluster.DrsConfigInfo()
        drs_info.enabled = True
        cluster_spec.drsConfig=drs_info
else:
        print "DRS is already enabled, cool"

pro_ha_spec=vim.cluster.InfraUpdateHaConfigInfo()
pro_ha_spec.behavior="Automated"
pro_ha_spec.enabled=True
pro_ha_spec.moderateRemediation="QuarantineMode"
provider_list =health_update_mgr.QueryProviderList()
if(provider_list):
        pro_ha_spec.providers = provider_list  #In our case, it was just one but their can be multiple
else:
        print "Provider is not registered, please do it first before enabling Proactive HA"
        sys.exit()

pro_ha_spec.severeRemediation="MaintenanceMode"
cluster_spec.infraUpdateHaConfig=pro_ha_spec

cluster.ReconfigureComputeResource_Task(cluster_spec, True)

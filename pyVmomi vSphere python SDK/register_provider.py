# Author: Vikas Shitole
# Website: www.vThinkBeyondVM.com
# Product: vCenter server/Proactive HA
# Description: #Script to register a health update provider and adding monitored entities
# Reference: http://vthinkbeyondvm.com/tutorial-part-1-configure-manage-proactive-ha-using-pyvmomi/
# How to setup pyVmomi environment?: http://vthinkbeyondvm.com/how-did-i-get-started-with-the-vsphere-python-sdk-pyvmomi-on-ubuntu-distro/

from pyVim.connect import SmartConnect, Disconnect
from pyVmomi import vim
import atexit
import ssl
import sys
import time
#Script to register a health update provider and adding monitored entities
s=ssl.SSLContext(ssl.PROTOCOL_TLSv1)
s.verify_mode=ssl.CERT_NONE
si= SmartConnect(host="10.161.20.30", user="Administrator@vsphere.local", pwd="VMware#123",sslContext=s)
content=si.content

#Cluster where ProactiveHA will be enbaled
cluster_name="ClusterProHA"

# Managed object that exposes all the methods to configure & manage proactiveHA
heath_update_mgr=content.healthUpdateManager

name="ProHAProvider1"  #Name of the provider to be registered

# Building an object with fake/simulated health update
health_update_info=vim.HealthUpdateInfo()
health_update_info.componentType="Power" # Can also be Storage, Memory, Network
health_update_info.description="Power failure Detected"
health_update_info.id="1000"

# Array of health updates, we can have multiple health updates per component type
health_updates = [health_update_info]

#Register a provider with health_updates, it returns a providerid
providerId = heath_update_mgr.RegisterHealthUpdateProvider(name,health_updates)

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
# Hosts inside the cluster
hosts = cluster.host
#Converting array of HostSystem objects to array of managed entities
cluster_entities=vim.ManagedEntity.Array(hosts)

# Adding entities i.e hosts to be monitored
heath_update_mgr.AddMonitoredEntities(providerId,cluster_entities)

#Disconnecting vCenter session
atexit.register(Disconnect, si)

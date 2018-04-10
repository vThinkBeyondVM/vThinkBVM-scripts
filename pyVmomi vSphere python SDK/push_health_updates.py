# Author: Vikas Shitole
# Website: www.vThinkBeyondVM.com
# Product: vCenter server/Proactive HA
# Description: #Script to push meaningful health updates to Proactive HA. In this script we are initializing ESXI host from gray to green health status.
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

# Below method helps us to get MOR of the object (vim type) that we passed.
def get_obj(content, vimtype, name):
        obj = None
        container = content.viewManager.CreateContainerView(content.rootFolder, vimtype, True)
        for c in container.view:
                if name:
                        if c.name == name:
                                obj = c
                                break

        return obj

#Cluster where ProactiveHA is enabled, this parameter is NOT required for this script
cluster_name="ClusterProHA"
# initialize health status of below host, we need to repeat for all the hosts inside the cluster. 
host_name="10.160.20.15" 
host = get_obj(content,[vim.HostSystem],host_name)
# Managed object that exposes all the methods to configure & manage proactiveHA
heath_update_mgr=content.healthUpdateManager

#Provider ID for the registered provider
providerid="52 b5 17 d2 2f 46 7e 9b-5f 4e 1a 25 a3 db 49 85"
health_update=vim.HealthUpdate()
health_update.entity=host
health_update.healthUpdateInfoId="1000"
health_update.id="vThinkBeyondVM"
health_update.remediation =""
health_update.status="green"
#Below is the array of health updates
updates = [health_update]

#Method to post health updates for Proactive HA to consume
heath_update_mgr.PostHealthUpdates(providerid,updates)

#Disconnecting vCenter session
atexit.register(Disconnect, si)

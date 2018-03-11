# Author: Vikas Shitole
# Website: www.vThinkBeyondVM.com
# Product: vCenter server/Proactive HA
# Description: #Script to verify a health update provider and monitored entities
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

# Managed object that exposes all the methods to configure & manage proactiveHA
health_update_mgr=content.healthUpdateManager

# Listing provider list
provider_list =health_update_mgr.QueryProviderList()

print "Provider Id:"+provider_list[0]

#Quering specific provider
provider_name = health_update_mgr.QueryProviderName(provider_list[0])
print "Provider Name:"+provider_name

#Getting monitored entities by a provider
monitored_entities=health_update_mgr.QueryMonitoredEntities(provider_list[0])
print monitored_entities

#Disconnecting vCenter session
atexit.register(Disconnect, si)

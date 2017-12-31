# Author: Vikas Shitole
# Website: www.vThinkBeyondVM.com
# Product: vCenter server/vCenter Server High Availability (VCHA)
# Description: Python script to destroy vCenter server HA 
# Reference:http://vthinkbeyondvm.com/how-to-manage-vcenter-server-ha-using-vsphere-python-sdk-pyvmomi-part-1/
# How to setup pyVmomi environment?: http://vthinkbeyondvm.com/how-did-i-get-started-with-the-vsphere-python-sdk-pyvmomi-on-ubuntu-distro/

from pyVim.connect import SmartConnect
from pyVmomi import vim
import ssl
#Destroy vCenter server HA
s=ssl.SSLContext(ssl.PROTOCOL_TLSv1)
s.verify_mode=ssl.CERT_NONE
si= SmartConnect(host="10.161.34.35", user="Administrator@vsphere.local", pwd="VMware#23",sslContext=s)
content=si.content

#Getting VCHA configurator managed object
vcha=si.content.failoverClusterConfigurator

#Getting VCHA cluster manager
vcha_cluster_manager=si.content.failoverClusterManager

# Setting vCenter HA to "disabled" mode.
task = vcha_cluster_manager.setClusterMode_Task("disabled")
while(task.info.state != "success"):
        continue

#Getting VCHA cluster mode
VCHA_mode=vcha_cluster_manager.getClusterMode()
if (VCHA_mode == "disabled"):
        vcha.destroyVcha_Task() #Destroing it
else:
        print "VCHA must be in disabled mode before destrying it"

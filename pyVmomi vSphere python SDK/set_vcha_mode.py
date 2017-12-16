
# Author: Vikas Shitole
# Website: www.vThinkBeyondVM.com
# Product: vCenter server/vCenter Server High Availability (VCHA)
# Description: Python Script to set vCenter Server High Availability (VCHA) mode
# Reference:http://vthinkbeyondvm.com/how-to-manage-vcenter-server-ha-using-vsphere-python-sdk-pyvmomi-part-1/
# How to setup pyVmomi environment?: http://vthinkbeyondvm.com/how-did-i-get-started-with-the-vsphere-python-sdk-pyvmomi-on-ubuntu-distro/

from pyVim.connect import SmartConnect
from pyVmomi import vim
import ssl

s=ssl.SSLContext(ssl.PROTOCOL_TLSv1)
s.verify_mode=ssl.CERT_NONE
c= SmartConnect(host="10.192.1.2", user="Administrator@vsphere.local", pwd="VMW!23A",sslContext=s)

vcha=c.content.failoverClusterManager

task = vcha.setClusterMode_Task("maintenance")

while(task.info.state != "success"):
        continue
print "VCHA mode is set to ::", vcha.getClusterMode()

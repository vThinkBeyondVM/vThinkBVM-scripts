# Author: Vikas Shitole
# Website: www.vThinkBeyondVM.com
# Product: vCenter server/vCenter Server High Availability (VCHA)
# Description: Script to initiate vCenter Server High Availability failover
# Reference:http://vthinkbeyondvm.com/how-to-manage-vcenter-server-ha-using-vsphere-python-sdk-pyvmomi-part-1/
# How to setup pyVmomi environment?: http://vthinkbeyondvm.com/how-did-i-get-started-with-the-vsphere-python-sdk-pyvmomi-on-ubuntu-distro/

from pyVim.connect import SmartConnect
from pyVmomi import vim
import ssl

s=ssl.SSLContext(ssl.PROTOCOL_TLSv1)
s.verify_mode=ssl.CERT_NONE
c= SmartConnect(host="10.160.20.40", user="Administrator@vsphere.local", pwd="VMW!23A",sslContext=s)

vcha=c.content.failoverClusterManager
task = vcha.initiateFailover_Task(True)

while(task.info.state != "success"):
        continue
print "Initiate Failover task is completed"

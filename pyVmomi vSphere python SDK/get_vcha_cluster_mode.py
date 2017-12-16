# Author: Vikas Shitole
# Website: www.vThinkBeyondVM.com
# Product: vCenter server/vCenter Server High Availability (VCHA)
# Description: Python Script to get vCenter Server High Availability (VCHA) mode
# Reference:http://vthinkbeyondvm.com/how-to-manage-vcenter-server-ha-using-vsphere-python-sdk-pyvmomi-part-1/
# How to setup pyVmomi environment?: http://vthinkbeyondvm.com/how-did-i-get-started-with-the-vsphere-python-sdk-pyvmomi-on-ubuntu-distro/

from pyVim.connect import SmartConnect
from pyVmomi import vim
import ssl

# Script to get vCenter Server High Availability (VCHA) mode
# Below is Python 2.7.x code, which can be easily converted to python 3.x version

s=ssl.SSLContext(ssl.PROTOCOL_TLSv1)
s.verify_mode=ssl.CERT_NONE
c= SmartConnect(host="10.192.45.10", user="Administrator@vsphere.local", pwd="VMware!1",sslContext=s)
vcha=c.content.failoverClusterManager

VCHA_mode=vcha.getClusterMode()
print "VCHA Cluster mode::", VCHA_mode

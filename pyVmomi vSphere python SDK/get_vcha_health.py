# Author: Vikas Shitole
# Website: www.vThinkBeyondVM.com
# Product: vCenter server/vCenter Server High Availability (VCHA)
# Description: Python script to Get vCenter server HA health information
# Reference:http://vthinkbeyondvm.com/how-to-manage-vcenter-server-ha-using-vsphere-python-sdk-pyvmomi-part-1/
# How to setup pyVmomi environment?: http://vthinkbeyondvm.com/how-did-i-get-started-with-the-vsphere-python-sdk-pyvmomi-on-ubuntu-distro/

from pyVim.connect import SmartConnect
from pyVmomi import vim
import ssl

s=ssl.SSLContext(ssl.PROTOCOL_TLSv1)
s.verify_mode=ssl.CERT_NONE
c= SmartConnect(host="10.192.20.30", user="Administrator@vsphere.local", pwd="VMW!23A",sslContext=s)

vcha = c.content.failoverClusterManager

VchaClusterHealth = vcha.GetVchaClusterHealth()

vcha_health_Messages = VchaClusterHealth.healthMessages
print "VCHA Health messages::"
for health_data in vcha_health_Messages:
        print health_data.message

print "\nAdditional Information::",VchaClusterHealth.additionalInformation

vcha_runtime_info = VchaClusterHealth.runtimeInfo
print "\nVCHA Cluster Mode::",vcha_runtime_info.clusterMode
print "\nVCHA Cluster State::",vcha_runtime_info.clusterState

vcha_node_info = vcha_runtime_info.nodeInfo

print "\nVCHA Node information:"
for node in vcha_node_info:
        print node.nodeRole+":"+node.nodeIp+":"+node.nodeState

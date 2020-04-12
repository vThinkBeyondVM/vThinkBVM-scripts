# Author: Vikas Shitole
# Website: www.vThinkBeyondVM.com
# Product: vCenter server/vMotion REST API
# Description: Python script to vMotion a VM from a cluster to destination DRS enabled cluster
# Reference: http://vthinkbeyondvm.com/getting-started-with-vcenter-server-rest-apis-using-python/
# How to setup vCenter REST API environment?: http://vthinkbeyondvm.com/getting-started-with-vcenter-server-rest-apis-using-python/

import requests
import json
import ssl
import atexit
import sys
import argparse
import getpass


from requests.packages.urllib3.exceptions import InsecureRequestWarning
requests.packages.urllib3.disable_warnings(InsecureRequestWarning)



s=requests.Session()
s.verify=False

def get_args():
    """ Get arguments from CLI """
    parser = argparse.ArgumentParser(
        description='Arguments for VM relocation')

    parser.add_argument('-s', '--host',
                        required=True,
                        action='store',
                        help='VC IP or FQDN')
						
    parser.add_argument('-u', '--user',
                        required=True,
                        action='store',
                        help='VC username')
						
    parser.add_argument('-p', '--password',
                        required=False,
                        action='store',
                        help='VMC password:')

    parser.add_argument('-vm', '--vmname',
                        required=True,
                        action='store',
                        help='VM name to be relocated')
						
    parser.add_argument('-ds', '--dsname',
                        required=True,
                        action='store',
                        help='Destination datastore name')
						
    parser.add_argument('-cl', '--clustername',
                        required=True,
                        action='store',
                        help='Destination cluster name')


    args = parser.parse_args()

    if not args.password:
        args.password = getpass.getpass(
            prompt='Enter VC password:')

    return args

args = get_args()
headers = {'content-type':'application/json'}
session_response= s.post('https://'+args.host+'/rest/com/vmware/cis/session',auth=(args.user,args.password))

if session_response.ok:
	print ("Session creation is successful")
else:
	print ("Session creation is failed, please check")
	quit()

vm_object=s.get('https://'+args.host+'/rest/vcenter/vm?filter.names='+args.vmname)
if len(json.loads(vm_object.text)["value"])==0:
	print ("NO VM found, please enter valid Name")
	sys.exit()
vm_id=json.loads(vm_object.text)["value"][0].get("vm")
print ("vm-id::"+vm_id)

disk_object=s.get('https://'+args.host+'/rest/vcenter/vm/'+vm_id+'/hardware/disk')
if len(json.loads(disk_object.text)["value"])==0:
	print ("NO VM disk key found, please check if VM has disk or not")
	sys.exit()
disk_key=json.loads(disk_object.text)["value"][0].get("disk")
print ("disk-key::"+disk_key)

datastore_object=s.get('https://'+args.host+'/rest/vcenter/datastore?filter.names='+args.dsname)
if len(json.loads(datastore_object.text)["value"])==0:
	print ("NO Datastore found, please enter valid Datastore name")
	sys.exit()
ds_id=json.loads(datastore_object.text)["value"][0].get("datastore")
print ("datastore-id::"+ds_id)

cluster_object=s.get('https://'+args.host+'/rest/vcenter/cluster?filter.names='+args.clustername)
if len(json.loads(cluster_object.text)["value"])==0:
	print ("NO cluster found, please enter valid cluster name")
	sys.exit()
cluster_id=json.loads(cluster_object.text)["value"][0].get("cluster")
print ("cluster-id::"+cluster_id)



payload = {
    "spec" : {
        "disks" : [
            {
                "value" : {
                    "datastore" : ds_id
                },
                "key" : disk_key
            }
        ],
        "placement" : {
            "cluster" : cluster_id,
            "datastore" : ds_id
        }
    }
}

json_payload = json.loads(json.dumps(payload))
json_response = s.post('https://'+args.host+'/rest/vcenter/vm/'+vm_id+'?action=relocate',headers=headers,json=json_payload)
if json_response.ok:
	print ("Relocate VM  API invoked")
else:
	print ("Relocate VM API failed, please check")
print (json_response)
session_delete=s.delete('https://'+args.host+'/rest/com/vmware/cis/session',auth=(args.user,args.password))
print (session_delete)

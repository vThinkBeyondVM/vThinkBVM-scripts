# Author: Vikas Shitole
# Product: vCenter server/ vSphere Supervisor namespace configuration
# Description: Python script to create namespace on the top of vSphere Supervisor cluster
# Reference: https://vthinkbeyondvm.com/script-to-configure-vsphere-supervisor-cluster-using-rest-apis/
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
						
    parser.add_argument('-cl', '--clustername',
                        required=True,
                        action='store',
                        help='cluster name')
						
    parser.add_argument('-ns', '--namespacename',
                        required=True,
						default="my-ns",
                        action='store',
                        help='Pass DNS complaint namespace name')
						
    parser.add_argument('-nd', '--description',
                        required=False,
						default="My first namespace",
                        action='store',
                        help='Any description of your choice')
						
    parser.add_argument('-role', '--nsrole',
                        required=True,
						default="EDIT",
                        action='store',
                        help='Role for the Namespace user EDIT or VIEW')
						
    parser.add_argument('-st', '--subjecttype',
                        required=True,
						default="USER",
                        action='store',
                        help='Subject type i.e. USER or GROUP')
						
    parser.add_argument('-subject', '--nsuser',
                        required=True,
						default="Administrator",
                        action='store',
                        help='Namespace for which we need to assign the permissions with')
						
    parser.add_argument('-domain', '--nsdomain',
                        required=True,
						default="vsphere.local",
                        action='store',
                        help='Master management network subnet mask')
						
    parser.add_argument('-sp', '--storagepolicy',
                        required=True,
                        action='store',
                        help='Storage policy name for namespace')

    parser.add_argument('-slimit', '--storagelimit',
                        required=False,
						default=None,
                        action='store',
                        help='Pass the storage limit in mebibytes i.e. 10240 for 10 GB')

    						
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

#Getting cluster moid
cluster_object=s.get('https://'+args.host+'/rest/vcenter/cluster?filter.names='+args.clustername)
if len(json.loads(cluster_object.text)["value"])==0:
	print ("NO cluster found, please enter valid cluster name")
	sys.exit()
cluster_id=json.loads(cluster_object.text)["value"][0].get("cluster")
print ("cluster-id::"+cluster_id)

#Getting storage policy id
storage_policy_object=s.get('https://'+args.host+'/rest/vcenter/storage/policies')
sp_policy_response=json.loads(storage_policy_object.text)
sp_policies=sp_policy_response["value"]
sp_id=""
for policy in sp_policies:
	if (policy.get("name")==args.storagepolicy):
		sp_id=policy.get("policy")
		break
if sp_id=="":
	print ("policy name not found, please check")
	sys.exit()
print ("storage policy id:"+sp_id)


payload = {
"cluster" : cluster_id,
"namespace" : args.namespacename,
"description" : args.description,
"access_list" : [
{
"role" : args.nsrole,
"subject_type" : args.subjecttype,
"subject" : args.nsuser,
"domain" : args.nsdomain
}
],
"storage_specs" : [
{
"limit" : args.storagelimit,
"policy" : sp_id 
}
],
"resource_spec" : {
#TODO: Passing cpu, mem quota needs to add
}
}

json_payload = json.loads(json.dumps(payload))
json_response = s.post('https://'+args.host+'/api/vcenter/namespaces/instances',headers=headers,json=json_payload)
if json_response.ok:
	print ("Supervisor Namespace creation invoked, checkout your H5C")
else:
	print ("Supervisor Namespace creation NOT invoked, please check your inputs once again")
print (json_response.text)
session_delete=s.delete('https://'+args.host+'/rest/com/vmware/cis/session',auth=(args.user,args.password))

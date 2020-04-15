# Author: Vikas Shitole
# Product: vCenter server/ WCP cluster configuration
# Description: Python script to configure/enable WCP on the given cluster
# How to setup vCenter REST API environment?: http://vthinkbeyondvm.com/getting-started-with-vcenter-server-rest-apis-using-python/

#TODO: Some additional formatting and null check pending but that is not a blocker to run this script

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
						
    parser.add_argument('-wsize', '--wcpsize',
                        required=False,
						default="TINY",
                        action='store',
                        help='Pass either TINY, SMALL, MEDIUM, LARGE')
						
    parser.add_argument('-mnw', '--mastervmnetwork',
                        required=True,
                        action='store',
                        help='Master mgmt management network port group')
						
    parser.add_argument('-sip', '--startingip',
                        required=True,
                        action='store',
                        help='Master mgmt network starting IP for Supervisor control plane VM')
						
    parser.add_argument('-sm', '--mastersm',
                        required=True,
                        action='store',
                        help='Master management network subnet mask')
						
    parser.add_argument('-gw', '--gatewayip',
                        required=True,
                        action='store',
                        help='Master mgmt network Gateway IP')
						
    parser.add_argument('-dns', '--dnsserver',
                        required=True,
                        action='store',
                        help='DNS server IP for worker and master')
						
    parser.add_argument('-ntp', '--ntpserver',
                        required=True,
                        action='store',
                        help='NTP server IP')
						
    parser.add_argument('-sp', '--storagepolicy',
                        required=True,
                        action='store',
                        help='Storage policy name for workloads,mastervms,images')

    parser.add_argument('-pcidr', '--podcidr',
                        required=False,
						default="10.244.0.0/21",
                        action='store',
                        help='Pass POD CIDR, usually default works fine, in this format 10.196.2.3/24')

    parser.add_argument('-scidr', '--servicecidr',
                        required=False,
						default="10.96.0.0/24",
                        action='store',
                        help='Pass service CIDR, usually default works fine, in this format 10.20.2.3/24')						
						
    parser.add_argument('-egress', '--egressaddress',
                        required=True,
                        action='store',
                        help='Egress starting IP')
						
    parser.add_argument('-ingress', '--ingressaddress',
                        required=True,
                        action='store',
                        help='Ingress starting IP')
						
    parser.add_argument('-prefix', '--egressingressprefix',
                        required=False,
						default="27",
                        action='store',
                        help='Pass common ingress and egress prefix (min is /27)')
    						


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

#Getting distributed switch id , assuming only one VDS associated with cluster that NSX TZ is configured with. Just avoiding further processing
#TODO: null check handling if we do not get compatible cluster
dvs_object=s.get('https://'+args.host+'/api/vcenter/namespace-management/distributed-switch-compatibility?cluster='+cluster_id+'&compatible=true')
dvs_id=json.loads(dvs_object.text)[0]['distributed_switch']
print (dvs_id)
	
#Getting edge cluster id. Assuming only one edge cluster asscociated with this VDS
edge_object=s.get('https://'+args.host+'/api/vcenter/namespace-management/edge-cluster-compatibility?cluster='+cluster_id+'&compatible=true&distributed_switch='+dvs_id)
edge_id=json.loads(edge_object.text)[0]['edge_cluster']
print (edge_id)

#Getting master network portgroup id
network_object=s.get('https://'+args.host+'/rest/vcenter/network?filter.names='+args.mastervmnetwork)
if len(json.loads(network_object.text)["value"])==0:
	print ("NO network found, please enter valid master network port group name")
	sys.exit()
network_id=json.loads(network_object.text)["value"][0].get("network")
print ("network id::"+network_id)


payload = {
   "image_storage":{
      "storage_policy": sp_id
   },
   "ncp_cluster_network_spec":{
      "nsx_edge_cluster": edge_id,
      "pod_cidrs":[
         {
            "address": args.podcidr.split('/')[0],
            "prefix": args.podcidr.split("/",1)[1]
         }
      ],
      "egress_cidrs":[
         {
            "address": args.egressaddress,
            "prefix": args.egressingressprefix
         }
      ],
      "cluster_distributed_switch": dvs_id,
      "ingress_cidrs":[
         {
            "address": args.ingressaddress,
            "prefix": args.egressingressprefix
         }
      ]
   },
   "master_management_network":{
      "mode":"STATICRANGE",
      "address_range":{
         "subnet_mask": args.mastersm,
         "starting_address": args.startingip,
         "gateway": args.gatewayip,
         "address_count":5
      },
      "network": network_id
   },
   "master_NTP_servers":[
      args.ntpserver
   ],
   "ephemeral_storage_policy": sp_id,
   "service_cidr":{
      "address":args.servicecidr.split('/')[0],
      "prefix":args.servicecidr.split("/",1)[1]
   },
    "size_hint": args.wcpsize,
   "master_DNS":[
      args.dnsserver
   ],
    "worker_DNS":[
      args.dnsserver
   ],
   "network_provider":"NSXT_CONTAINER_PLUGIN",
   "master_storage_policy": sp_id
}

json_payload = json.loads(json.dumps(payload))
json_response = s.post('https://'+args.host+'/api/vcenter/namespace-management/clusters/'+cluster_id+'?action=enable',headers=headers,json=json_payload)
if json_response.ok:
	print ("WCP  API invoked, checkout your H5C")
else:
	print ("WCP  API NOT invoked, please check your inputs once again")
print (json_response.text)
session_delete=s.delete('https://'+args.host+'/rest/com/vmware/cis/session',auth=(args.user,args.password))

# Author: Vikas Shitole
# Website: www.vThinkBeyondVM.com
# Product: VMware Cloud on AWS
# Description: Python script to configure Hybrid Linked Mode (HLM) between Onprem and VMC VC
# Reference: http://vthinkbeyondvm.com/how-to-configure-hybrid-linked-mode-hlm-using-vcenter-rest-api/
# How to setup vCenter REST API environment?: http://vthinkbeyondvm.com/getting-started-with-vcenter-server-rest-apis-using-python/
#Note : Thumbprint being passed is NOT required parameter if you are calling this API against vCenter cloud gateway
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
        description='Arguments for HLM linking')

    parser.add_argument('-s', '--host',
                        required=True,
                        action='store',
                        help='VMC VC IP or FQDN')
						
    parser.add_argument('-u', '--user',
                        required=True,
                        action='store',
                        help='VMC VC username')
						
    parser.add_argument('-p', '--password',
                        required=False,
                        action='store',
                        help='VMC password:')

    parser.add_argument('-o', '--port',
                        type=int,
                        default=443,
                        action='store',
                        help='PSC port')

    parser.add_argument('-d', '--domainname',
                        type=str,
			default='vsphere.local',
                        action='store',
                        help='Onprem PSC domain name')
						
    parser.add_argument('-pu', '--pscuser',
                        required=False,
			default='Administrator@vsphere.local',
                        action='store',
                        help='Onprem PSC username')

    parser.add_argument('-pp', '--pscpass',
                        required=False,
                        action='store',
                        help='Onprem PSC password')
						
    parser.add_argument('-ph', '--pschost',
                        required=True,
                        action='store',
                        help='Onprem PSC host IP or FQDN')

    parser.add_argument('-a', '--admingroup',
                        required=False,
                        action='store',
                        default='cloudadmin@yourdomain.com',
                        help='Cloud admins group')	
						
    parser.add_argument('-pt', '--pscthumb',
                        required=True,
                        action='store',
                        help='Onprem PSC thumbprint')

    args = parser.parse_args()

    if not args.password:
        args.password = getpass.getpass(
            prompt='Enter VMC VC password:')
			
    if not args.pscpass:
        args.pscpass = getpass.getpass(
            prompt='Enter PSC password:')

    return args

args = get_args()
headers = {'content-type':'application/json'}
session_response= s.post('https://'+args.host+'/rest/com/vmware/cis/session',auth=(args.user,args.password))

if session_response.ok:
	print ("Session creation is successful")
else:
	print ("Session creation is failed, please check")
	quit()

payload = {
  "spec": {
    "port": args.port,
    "domain_name": args.domainname,
    "username": args.pscuser,
    "ssl_thumbprint": args.pscthumb,
    "admin_groups": [
      args.admingroup
    ],
    "password": args.pscpass,
    "psc_hostname": args.pschost
  }
}

json_payload = json.loads(json.dumps(payload))
json_response = s.post('https://'+args.host+'/rest/hvc/links',headers=headers,json=json_payload)
if json_response.ok:
	print ("HLM link is established")
else:
	print ("HLM link is NOT established, please check")
print (json_response)
print (json_response.text)

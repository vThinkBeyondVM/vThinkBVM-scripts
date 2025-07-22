/**
 * NOTE: This sample was written using the legacy open source VIJava project, which is no longer maintained by its original maintainer.
 * The legacy VIJava project was last supported up to vSphere 6.0. These samples may still work on newer vSphere versions, but this is not guaranteed and they have not been tested on the latest versions.
 * For all new development, please use the official VMware vSphere Management SDKs (also known as vSphere Web Services SDK).
 * Download and documentation for the latest vSphere SDKs: https://developer.broadcom.com/sdks?tab=Compute%2520Virtualization
 */
//:: # Author: Vikas Shitole
//:: # Website: www.vThinkBeyondVM.com
//:: # Product/Feature: vCenter Server/storage
//:: # Reference: http://vthinkbeyondvm.com/tutorial-vsphere-api-using-java-how-to-get-datastore-summary-for-all-data-stores-connected-to-a-esxi-host/
//:: # Description: Tutorial: How to navigate and access all the ESXi and vCenter top level objects
//::# How to run this sample: http://vthinkbeyondvm.com/getting-started-with-yavi-java-opensource-java-sdk-for-vmware-vsphere-step-by-step-guide-for-beginners/


package com.vmware.yavijava;

import java.net.URL;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostDatastoreBrowser;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;

public class GetDatastoreSummary {
public static void main(String[] args) throws Exception {
if (args.length != 3) {
System.out.println("Usage: java SearchDatastore "
+ "username password");
return;
}

/*
 * you need to pass 3 parameters
 * 1. https://ESXi_IP/sdk
 * 2. username
 * 3. password
 */
 ServiceInstance si = new ServiceInstance(new URL(args[0]), args[1],
 args[2], true);
String hostname = "XYZ.vmware.com"; //Pass the FQDN i.e. DNS name of the ESXi host, ESXi host IP will not work
 Folder rootFolder = si.getRootFolder();
 HostSystem host = null;

 host = (HostSystem) new InventoryNavigator(rootFolder)
 .searchManagedEntity("HostSystem", hostname);

if (host == null) {
System.out.println("Host not found");
 si.getServerConnection().logout();
return;
}

 HostDatastoreBrowser hdb = host.getDatastoreBrowser();

System.out.println("Datastore Summary connected to ESXi host");
System.out.println();
 Datastore[] ds = hdb.getDatastores();

for (int i = 0; ds != null && i < ds.length; i++) {
System.out.println("DatastoreName:" + ds[i].getName() + " "
+ "DSType:" + ds[i].getSummary().getType() + " "
+ "TotalCapacity(in GB):"
+ (ds[i].getSummary().getCapacity()) / (1024 * 1024 * 1024)
+ " " + "FreeSpace (in GB): "
+ (ds[i].getSummary().getFreeSpace())
/ (1024 * 1024 * 1024) + " ");
System.out.println();

}
 si.getServerConnection().logout();
}
}

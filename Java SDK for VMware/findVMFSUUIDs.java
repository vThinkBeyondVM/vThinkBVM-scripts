//:: # Author: Vikas Shitole
//:: # Website: www.vThinkBeyondVM.com
//:: # Product/Feature: vCenter Server/ESXi/Storage
//:: # Reference: http://vthinkbeyondvm.com/how-to-get-vmfs-datastore-uuids-across-all-the-datastores-in-a-vcenter-server-using-vsphere-apis/
//:: # Description: Script to get all hosts and all of its associated datastores . Also each datastore and its associated UUID
//::# How to run this sample: http://vthinkbeyondvm.com/getting-started-with-yavi-java-opensource-java-sdk-for-vmware-vsphere-step-by-step-guide-for-beginners/

package com.vmware.yavijava;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import com.vmware.vim25.DatastoreInfo;
import com.vmware.vim25.VmfsDatastoreInfo;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostDatastoreBrowser;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;


public class findVMFSUUIDs {
     public static void main(String[] args) throws Exception {
         ServiceInstance si = new ServiceInstance(new URL(
                 "https://10.120.30.40/sdk"), "administrator@vsphere.local",
                 "Administrator!23", true);
         // Get the rootFolder
         Folder rootFolder = si.getRootFolder();

         // Get all the hosts in the vCenter server
         ManagedEntity[] hosts = new InventoryNavigator(rootFolder)
                 .searchManagedEntities("HostSystem");

         if (hosts == null) {
             System.out.println("Host not found on vCenter");
             si.getServerConnection().logout();
             return;
         }

         // Map to store the datastore name as key and its UUID as the value
         Map< String , String> vmfsdatastoreUUIDs = new HashMap< String , String>();

         // Map to store host as key and all of its datastores as the value
         Map< ManagedEntity , Datastore[]> hostDatastores = new HashMap< ManagedEntity , Datastore[]>();
         for (ManagedEntity hostSystem : hosts) {
             HostDatastoreBrowser hdb = ((HostSystem) hostSystem)
                     .getDatastoreBrowser();
             Datastore[] ds = hdb.getDatastores();
             hostDatastores.put(hostSystem, ds);
         }

         System.out.println("Hosts and all of its associated datastores");
         for (Map.Entry < ManagedEntity , Datastore[]> datastores : hostDatastores
                 .entrySet()) {
             System.out.println("");
             System.out.print("[" + datastores.getKey().getName() + "::");
             for (Datastore datastore : datastores.getValue()) {
                 System.out.print(datastore.getName() + ",");
                 DatastoreInfo dsinfo = datastore.getInfo();
                 if (dsinfo instanceof VmfsDatastoreInfo) {
                     VmfsDatastoreInfo vdinfo = (VmfsDatastoreInfo) dsinfo;
                     vmfsdatastoreUUIDs.put(datastore.getName(), vdinfo
                             .getVmfs().getUuid());
                 }

             }
             System.out.print("]");
         }
         System.out.println(" ");
         System.out.println("Datastore and its UUID");
         for (Map.Entry< String , String> dsuuid : vmfsdatastoreUUIDs.entrySet()) {
             System.out.println("[" + dsuuid.getKey() + "::" + dsuuid.getValue()
                     + "]");
         }
         si.getServerConnection().logout();

     }
     
}

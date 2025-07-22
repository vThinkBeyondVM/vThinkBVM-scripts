// NOTE: These samples were developed on older vSphere versions. While they may work on the latest versions, they are not tested release over release. Use at your own risk.
/**
 * NOTE: This sample was written using the legacy open source VIJava project, which is no longer maintained by its original maintainer.
 * For all new development, please use the official VMware vSphere Management SDKs (also known as vSphere Web Services SDK).
 * Download and documentation for the latest vSphere SDKs: https://developer.broadcom.com/sdks?tab=Compute%2520Virtualization
 */
//:: # Author: Vikas Shitole
//:: # Website: www.vThinkBeyondVM.com
//:: # Product/Feature: vCenter Server/Storage
//:: # Reference: http://vthinkbeyondvm.com/vsphere-6-0-cool-apis-to-mark-local-host-hdd-to-ssd-ssd-to-hdd-sample-api-script/
//:: # Description: Mark the local Lun of the host as SSD for testing purpose.
//::# How to run this sample: http://vthinkbeyondvm.com/getting-started-with-yavi-java-opensource-java-sdk-for-vmware-vsphere-step-by-step-guide-for-beginners/

package com.vmware.yavijava;
import java.net.MalformedURLException;
import java.net.URL;
import com.vmware.vim25.ScsiLun;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostStorageSystem;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;

public class MarkAsSSD {

     public static void main(String[] args) throws Exception {
         if(args.length!=4)
         {
             System.out.println("Usage: Java MarkAsSSD url username password hostip/fqdn");
             System.exit(-1);
         }

         URL url = null;
         try 
         { 
             url = new URL(args[0]); 
         } catch ( MalformedURLException urlE)
         {
             System.out.println("The URL provided is NOT valid. Please check it.");
             System.exit(-1);
         }
         String username = args[1]; //vCenter username
         String password = args[2]; //vCenter password
         String hostname = args[3]; // host IP on which local HDD is available 
         String LunDisplayName="Local VMware Disk (mpx.vmhba1:C0:T2:L0)"; //Add the Display name of the lun that can be seen from VI client or NGC
         // Initialize the system, set up web services
         ServiceInstance si = new ServiceInstance(url, username,
                 password, true);

         Folder rootFolder = si.getRootFolder();
         HostSystem host = null;

         host = (HostSystem) new InventoryNavigator(rootFolder)
         .searchManagedEntity("HostSystem", hostname);


         if (host == null) {
             System.out.println("Host not found on vCenter");
             si.getServerConnection().logout();
             return;
         }

         HostStorageSystem hhostsystem = host.getHostStorageSystem();
         ScsiLun[] scsilun = hhostsystem.getStorageDeviceInfo().getScsiLun();
         boolean flag = false;
         for (ScsiLun lun : scsilun) {
             System.out.println("Display Name"+lun.getDisplayName());
             if (lun.getDisplayName().equals(
                     LunDisplayName)) {
                 hhostsystem.markAsSsd_Task(lun.getUuid());
                 flag = true;
                 break;
                 // hhostsystem.markAsNonSsd_Task(lun.getUuid());

             }

         }
         if (flag) {
             System.out.println("LUN is marked as SSD successfully");

         }else{
             System.out.println("LUN is NOT marked as SSD, plz check if local lun is in use");
             
         }
         
         si.getServerConnection().logout();

     }
}

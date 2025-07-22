// NOTE: These samples were developed on older vSphere versions. While they may work on the latest versions, they are not tested release over release. Use at your own risk.
/**
 * NOTE: This sample was written using the legacy open source VIJava project, which is no longer maintained by its original maintainer.
 * The legacy VIJava project was last supported up to vSphere 6.0. These samples may still work on newer vSphere versions, but this is not guaranteed and they have not been tested on the latest versions.
 * For all new development, please use the official VMware vSphere Management SDKs (also known as vSphere Web Services SDK).
 * Download and documentation for the latest vSphere SDKs: https://developer.broadcom.com/sdks?tab=Compute%2520Virtualization
 */
//:: # Author: Vikas Shitole
//:: # Website: www.vThinkBeyondVM.com
//:: # Product/Feature: vCenter Server/DRS
//:: # Reference: http://vthinkbeyondvm.com/how-to-quickly-get-vsphere-cluster-resource-usage-cpu-mem-storage-using-api-cool-api-in-vsphere-6-0/
//:: # Description: Script to quickly get Cluster resource usage (cpu,memory and storage).
//::# How to run this sample: http://vthinkbeyondvm.com/getting-started-with-yavi-java-opensource-java-sdk-for-vmware-vsphere-step-by-step-guide-for-beginners/

package com.vmware.yavijava;

import java.net.MalformedURLException;
import java.net.URL;
import com.vmware.vim25.ClusterResourceUsageSummary;
import com.vmware.vim25.mo.ClusterComputeResource;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;

public class GetClusterResourceUsage {

     public static void main(String[] args) throws Exception {
         if(args.length!=3)
         {
             System.out.println("Usage: Java GetClusterResourceUsage VCurl username password");
             System.exit(-1);
         }

         URL url = null;
         try 
         { 
             url = new URL(args[0]); 
         } catch ( MalformedURLException urlE)
         {
             System.out.println("The URL provided is NOT valid. Please check it...");
             System.exit(-1);
         }
         String username = args[1];
         String password = args[2];
         String ClusterName = "Cluster1"; //Your cluster Name

         // Initialize the system, set up web services
         ServiceInstance si = new ServiceInstance(url, username,
                 password, true);
         Folder rootFolder = si.getRootFolder();

         ClusterComputeResource cluster = null;
         cluster = (ClusterComputeResource) new InventoryNavigator(rootFolder)
         .searchManagedEntity("ClusterComputeResource", ClusterName);

         System.out.println();
         System.out.println("Resource Usagae Summary for cluster::"+cluster.getName());
         //Get the Cluster resource summary object
         ClusterResourceUsageSummary resourceSummary= cluster.getResourceUsage();
         System.out.println("CPU Capacity::"+resourceSummary.getCpuCapacityMHz()+" MHz");
         System.out.println("CPU used::"+resourceSummary.getCpuUsedMHz()+" MHz");
         System.out.println("Memory Capacity::"+resourceSummary.getMemCapacityMB()+" MB");
         System.out.println("Memory used::"+resourceSummary.getMemUsedMB()+" MB");
         System.out.println("Storage Capacity::"+resourceSummary.getStorageCapacityMB()+" MB");
         System.out.println("Storage used::"+resourceSummary.getStorageUsedMB()+" MB");
         si.getServerConnection().logout();
     }
}

// NOTE: These samples were developed on older vSphere versions. While they may work on the latest versions, they are not tested release over release. Use at your own risk.
/**
 * NOTE: This sample was written using the legacy open source VIJava project, which is no longer maintained by its original maintainer.
 * For all new development, please use the official VMware vSphere Management SDKs (also known as vSphere Web Services SDK).
 * Download and documentation for the latest vSphere SDKs: https://developer.broadcom.com/sdks?tab=Compute%2520Virtualization
 */
//:: # Author: Vikas Shitole
//:: # Website: www.vThinkBeyondVM.com
//:: # Product/Feature: vCenter Server/VMware ESXi
//:: # Reference: http://vthinkbeyondvm.com/tutorial-part-ii-how-to-accessnavigate-vcenter-server-or-esxi-host-inventory-using-vsphere-api/
//:: # Description: Tutorial: How to navigate and access all the ESXi and vCenter top level objects
//::# How to run this sample: http://vthinkbeyondvm.com/getting-started-with-yavi-java-opensource-java-sdk-for-vmware-vsphere-step-by-step-guide-for-beginners/

package com.vmware.yavijava;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.ClusterComputeResource;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class VCESXInventoryNavigator {
	
     public static void main(String[] args) throws InvalidProperty,
             RuntimeFault, RemoteException, MalformedURLException {
     ServiceInstance si = new ServiceInstance(new URL("https://192.168.1.1/sdk"), "root", "vmw", true);
     System.out.println(si);
     
     Folder rootFolder = si.getRootFolder();
    
     //Note: It is expected to pass all the parameters as per your environment
     
     //Getting hold of a datacenter in vCenter server
     String dcName = "IND-BLR";
     Datacenter datacenter = null;
     datacenter = (Datacenter) new InventoryNavigator(rootFolder).searchManagedEntity("Datacenter", dcName);
     System.out.println("Data center Name::" + datacenter.getName());
     
     //Getting hold of All datacenters in vCenter server
         ManagedEntity[] dcenters = new InventoryNavigator(rootFolder).searchManagedEntities("Datacenter");
         System.out.println("Number of Datacenters in vCenter::" + dcenters.length);
         
         //Getting hold of a host in vCenter server
         String hostName = "10.192.34.2";
         HostSystem host = null;
         host = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", hostName);
         System.out.println("Host Name::" + host.getName());
         
         //Getting hold of  All hosts in vCenter server
                 ManagedEntity[] hosts = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
         System.out.println("Number of hosts in vCenter ::" + hosts.length); 
         
         
       //Getting hold of a cluster in vCenter server
         String clusterName = "My-Cluster";
         ClusterComputeResource cluster = null;
         cluster = (ClusterComputeResource) new InventoryNavigator(rootFolder)
                 .searchManagedEntity("ClusterComputeResource", hostName);
         System.out.println("Cluster Name::" + cluster.getName());
         
       //Getting hold of All clusters in vCenter server
                 ManagedEntity[] clusters = new InventoryNavigator(rootFolder)
                 .searchManagedEntities("ClusterComputeResource");
         System.out.println("Number of clusters in vCenter ::" + clusters.length);
         
       //Getting hold of a datastore in vCenter server
         
         String DSName = "VMFS_3";
         Datastore datastore = null;
         datastore = (Datastore) new InventoryNavigator(rootFolder).searchManagedEntity("Datastore", DSName);
         System.out.println("Datastore Name::" + datastore.getName());
         
       //Getting hold of All datastores in vCenter server
         ManagedEntity[] datastores = new InventoryNavigator(rootFolder).searchManagedEntities("Datastore");
         System.out.println("Number of datastores in vCenter ::"+ datastores.length);
         
         
       //Getting hold of a VM in vCenter server
         String VMName = "My-VM";
         VirtualMachine vm = null;
         vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", VMName);
         System.out.println("VM Name::" + vm.getName());
         
       //Getting hold of  All VMs in vCenter server
         ManagedEntity[] vms = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
         System.out.println("Number of VMs in vCenter ::"+ vms.length);
         
       //Getting hold of a Resource pool in vCenter server
         String ResourcePoolName = "My-RP";
         VirtualMachine rpool = null;
         rpool = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("ResourcePool", ResourcePoolName);
         System.out.println("VM Name::" + rpool.getName());
         
       //Getting hold of All resource pool in vCenter server
         ManagedEntity[] rpools = new InventoryNavigator(rootFolder).searchManagedEntities("ResourcePool");
         System.out.println("Number of VMs in vCenter ::"+ rpools.length);
         
         si.getServerConnection().logout();
         
     }
}
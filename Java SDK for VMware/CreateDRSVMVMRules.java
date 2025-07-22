/**
 * NOTE: This sample was written using the legacy open source VIJava project, which is no longer maintained by its original maintainer.
 * The legacy VIJava project was last supported up to vSphere 6.0. These samples may still work on newer vSphere versions, but this is not guaranteed and they have not been tested on the latest versions.
 * For all new development, please use the official VMware vSphere Management SDKs (also known as vSphere Web Services SDK).
 * Download and documentation for the latest vSphere SDKs: https://developer.broadcom.com/sdks?tab=Compute%2520Virtualization
 */
//:: # Author: Vikas Shitole
//:: # Website: www.vThinkBeyondVM.com
//:: # Product/Feature: vCenter Server/DRS
//:: # Reference: http://vthinkbeyondvm.com/drs-rules-part-ii-how-to-create-vm-vm-affinity-rules-using-vsphere-api/
//:: # Description: Script to create DRS VM-VM affinity/anti-affinity rules
//::# How to run this sample: http://vthinkbeyondvm.com/getting-started-with-yavi-java-opensource-java-sdk-for-vmware-vsphere-step-by-step-guide-for-beginners/


package com.vmware.yavijava;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import com.vmware.vim25.ArrayUpdateOperation;
import com.vmware.vim25.ClusterAffinityRuleSpec;
import com.vmware.vim25.ClusterAntiAffinityRuleSpec;
import com.vmware.vim25.ClusterConfigSpec;
import com.vmware.vim25.ClusterRuleSpec;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.ClusterComputeResource;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.util.MorUtil;

// NOTE: These samples were developed on older vSphere versions. While they may work on the latest versions, they are not tested release over release. Use at your own risk.
public class CreateDRSVMVMRules {

     public static void main(String[] args) throws InvalidProperty,
             RuntimeFault, RemoteException, MalformedURLException {
         ServiceInstance si = new ServiceInstance(new URL(args[0]), args[1],
                 args[2], true); // Pass 3 argument as vCenterIP/username/password
                 String ClusterName = "BLR-NTP"; // Cluster Name
         String affineVM1 = "CentOS6_x64_2GB_1"; // First VM for affinity rule
         String affineVM2 = "CentOS6_x64_2GB_2"; // Second VM for affinity rule
         String anti_affineVM1 = "CentOS6_x64_2GB_3"; // First VM for anti-affinity rule
         String anti_affineVM2 = "CentOS6_x64_2GB_4"; // Second VM for anti-affinity rule
                 Folder rootFolder = si.getRootFolder();

         ClusterComputeResource cluster = null;
         cluster = (ClusterComputeResource) new InventoryNavigator(rootFolder)
                 .searchManagedEntity("ClusterComputeResource", ClusterName);
                 ManagedObjectReference ClusterMor = cluster.getMOR();
         ClusterComputeResource ccr = (ClusterComputeResource) MorUtil
                 .createExactManagedEntity(si.getServerConnection(), ClusterMor);

         // VM-VM affinity rule configuration
         ClusterConfigSpec ccs = new ClusterConfigSpec();
         ClusterAffinityRuleSpec cars = null;
         VirtualMachine vm1 = (VirtualMachine) new InventoryNavigator(rootFolder)
                 .searchManagedEntity("VirtualMachine", affineVM1);
         VirtualMachine vm2 = (VirtualMachine) new InventoryNavigator(rootFolder)
                 .searchManagedEntity("VirtualMachine", affineVM2);
         ManagedObjectReference vmMor1 = vm1.getMOR();
         ManagedObjectReference vmMor2 = vm2.getMOR();
         ManagedObjectReference[] vmMors1 = new ManagedObjectReference[] {
                 vmMor1, vmMor2 };
         cars = new ClusterAffinityRuleSpec();
         cars.setName("VM-VM Affinity Rule");
         cars.setEnabled(true);
         cars.setVm(vmMors1);
         ClusterRuleSpec crs1 = new ClusterRuleSpec();
         crs1.setOperation(ArrayUpdateOperation.add);
         crs1.setInfo(cars);

         // VM-VM Anti-affinity rule configuration
         ClusterAntiAffinityRuleSpec caars = null;
         VirtualMachine vm3 = (VirtualMachine) new InventoryNavigator(rootFolder)
                 .searchManagedEntity("VirtualMachine", anti_affineVM1);
         VirtualMachine vm4 = (VirtualMachine) new InventoryNavigator(rootFolder)
                 .searchManagedEntity("VirtualMachine", anti_affineVM2);
         ManagedObjectReference vmMor3 = vm3.getMOR();
         ManagedObjectReference vmMor4 = vm4.getMOR();
         ManagedObjectReference[] vmMors2 = new ManagedObjectReference[] {
                 vmMor3, vmMor4 };
         caars = new ClusterAntiAffinityRuleSpec();
         caars.setName("VM-VM Anti-Affinity Rule");
         caars.setEnabled(true);
         caars.setVm(vmMors2);
         ClusterRuleSpec crs2 = new ClusterRuleSpec();
         crs2.setOperation(ArrayUpdateOperation.add);
         crs2.setInfo(caars);

         // Passing the rule spec
         ccs.setRulesSpec(new ClusterRuleSpec[] { crs1, crs2 });
         // Reconfigure the cluster
         ccr.reconfigureCluster_Task(ccs, true);
         System.out.println("Rules are created with no issues:");
         si.getServerConnection().logout();

     }
}

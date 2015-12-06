//:: # Author: Vikas Shitole
//:: # Website: www.vThinkBeyondVM.com
//:: # Product/Feature: vCenter Server/DRS
//:: # Reference: http://vthinkbeyondvm.com/drs-rules-part-i-how-to-create-vm-host-drs-affinity-rule-using-vsphere-api/
//:: # Description: Script to create DRS VM-HOST affinity/anti-affinity rules
//::# How to run this sample: http://vthinkbeyondvm.com/getting-started-with-yavi-java-opensource-java-sdk-for-vmware-vsphere-step-by-step-guide-for-beginners/


package com.vmware.yavijava;

import java.net.URL;
import com.vmware.vim25.ArrayUpdateOperation;
import com.vmware.vim25.ClusterConfigSpecEx;
import com.vmware.vim25.ClusterGroupInfo;
import com.vmware.vim25.ClusterGroupSpec;
import com.vmware.vim25.ClusterHostGroup;
import com.vmware.vim25.ClusterRuleSpec;
import com.vmware.vim25.ClusterVmGroup;
import com.vmware.vim25.ClusterVmHostRuleInfo;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.mo.ClusterComputeResource;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.util.MorUtil;

public class CreateDRS_VMHOSTRules {

     public static void main(String[] args) throws Exception {
         if (args.length != 3) {
             System.out.println("Usage: java SearchDatastore [url] "
                     + "[username] [password]");
             return;
         }

         ServiceInstance si = new ServiceInstance(new URL(args[0]), args[1],
                 args[2], true);
         /*
          * you need to pass 3 parameters 1. https://x.y.z.r/sdk 2. username 3.
          * password. Plz connect to vCenter Server
          */
         String vmGroupName = "vmGroup_1";
         String hostGroupName = "HostGroup_1";
         Folder rootFolder = si.getRootFolder();

         ClusterComputeResource clu = null;

         clu = (ClusterComputeResource) new InventoryNavigator(rootFolder)
                 .searchManagedEntity("ClusterComputeResource", "India_Cluster");
         ManagedObjectReference ClusterMor = clu.getMOR();

         HostSystem host1 = (HostSystem) new InventoryNavigator(rootFolder)
                 .searchManagedEntity("HostSystem", "10.10.1.1");
         HostSystem host2 = (HostSystem) new InventoryNavigator(rootFolder)
                 .searchManagedEntity("HostSystem", "10.10.1.2");
         ManagedObjectReference hostMor1 = host1.getMOR();
         ManagedObjectReference hostMor2 = host2.getMOR();
         VirtualMachine vm1 = (VirtualMachine) new InventoryNavigator(rootFolder)
                 .searchManagedEntity("VirtualMachine", "CentOS6_x64_2GB_1");
         VirtualMachine vm2 = (VirtualMachine) new InventoryNavigator(rootFolder)
                 .searchManagedEntity("VirtualMachine", "CentOS6_x64_2GB_2");
         ManagedObjectReference vmMor1 = vm1.getMOR();
         ManagedObjectReference vmMor2 = vm2.getMOR();
         ClusterComputeResource ccr = (ClusterComputeResource) MorUtil
                 .createExactManagedEntity(si.getServerConnection(), ClusterMor);
         ManagedObjectReference[] vmMors = new ManagedObjectReference[] {
                 vmMor1, vmMor2 };
         ManagedObjectReference[] hostMors = new ManagedObjectReference[] {
                 hostMor1, hostMor2 };

         ClusterGroupInfo vmGroup = new ClusterVmGroup();
         ((ClusterVmGroup) vmGroup).setVm(vmMors);
         vmGroup.setUserCreated(true);
         vmGroup.setName(vmGroupName);

         ClusterGroupInfo hostGroup = new ClusterHostGroup();
         ((ClusterHostGroup) hostGroup).setHost(hostMors);
         hostGroup.setUserCreated(true);
         hostGroup.setName(hostGroupName);

         ClusterVmHostRuleInfo vmHostAffRule = new ClusterVmHostRuleInfo();
         vmHostAffRule.setEnabled(new Boolean(true));
         vmHostAffRule.setName("VMHOSTAffinityRule");
         vmHostAffRule.setAffineHostGroupName("HostGrp_1");
         vmHostAffRule.setVmGroupName("VMGrp_1");
                 vmHostAffRule.setMandatory(true);
         ClusterGroupSpec groupSpec[] = new ClusterGroupSpec[2];
         groupSpec[0] = new ClusterGroupSpec();
         groupSpec[0].setInfo(vmGroup);
         groupSpec[0].setOperation(ArrayUpdateOperation.add);
         groupSpec[0].setRemoveKey(null);
         groupSpec[1] = new ClusterGroupSpec();
         groupSpec[1].setInfo(hostGroup);
         groupSpec[1].setOperation(ArrayUpdateOperation.add);
         groupSpec[1].setRemoveKey(null);
         /* RulesSpec for the rule populated here */
         ClusterRuleSpec ruleSpec[] = new ClusterRuleSpec[1];

         ruleSpec[0] = new ClusterRuleSpec();
         ruleSpec[0].setInfo(vmHostAffRule);
         ruleSpec[0].setOperation(ArrayUpdateOperation.add);
         ruleSpec[0].setRemoveKey(null);
         ClusterConfigSpecEx ccs = new ClusterConfigSpecEx();
         ccs.setRulesSpec(ruleSpec);
         ccs.setGroupSpec(groupSpec);

         ccr.reconfigureComputeResource_Task(ccs, true);
         System.out.println("VM Host Rule created successfully");
         si.getServerConnection().logout();

     }

}
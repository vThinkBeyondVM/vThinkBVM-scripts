/**
 * NOTE: This sample was written using the legacy open source VIJava project, which is no longer maintained by its original maintainer.
 * The legacy VIJava project was last supported up to vSphere 6.0. These samples may still work on newer vSphere versions, but this is not guaranteed and they have not been tested on the latest versions.
 * For all new development, please use the official VMware vSphere Management SDKs (also known as vSphere Web Services SDK).
 * Download and documentation for the latest vSphere SDKs: https://developer.broadcom.com/sdks?tab=Compute%2520Virtualization
 */
//:: # Author: Vikas Shitole
//:: # Description: Script to edit DRS VM VM affinity rules
//:: # Website: www.vThinkBeyondVM.com
//:: # Reference: http://vthinkbeyondvm.com/how-to-edit-drs-vm-vm-affinity-rules-using-vsphere-api-bit-tricky-solution/
//:: # Product/Feature: vCenter Server/DRS


package com.vmware.yavijava.samples.cluster;

import java.util.Arrays;
import org.apache.commons.lang.ArrayUtils;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import com.vmware.vim25.ArrayUpdateOperation;
import com.vmware.vim25.ClusterAffinityRuleSpec;
import com.vmware.vim25.ClusterAntiAffinityRuleSpec;
import com.vmware.vim25.ClusterConfigInfoEx;
import com.vmware.vim25.ClusterConfigSpec;
import com.vmware.vim25.ClusterRuleInfo;
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

public class EditVmVmDrsRule {

	public static void main(String[] args) throws InvalidProperty,
	RuntimeFault, RemoteException, MalformedURLException {
		ServiceInstance si = new ServiceInstance(new URL(args[0]), args[1],
				args[2], true); // Pass 3 argument as vCenterIP/username/password
		String ClusterName = "Cluster"; // Cluster Name
		String VMToBeRemoved="VM1";
		String VMToBeAdded="VM3";
		Folder rootFolder = si.getRootFolder();
		VirtualMachine vm1 = (VirtualMachine) new InventoryNavigator(rootFolder)
		.searchManagedEntity("VirtualMachine", VMToBeRemoved);
		VirtualMachine vm2 = (VirtualMachine) new InventoryNavigator(rootFolder)
		.searchManagedEntity("VirtualMachine", VMToBeAdded);
		ManagedObjectReference vmMor1 = vm1.getMOR();
		ManagedObjectReference vmMor2 = vm2.getMOR();



		ClusterComputeResource cluster = null;
		cluster = (ClusterComputeResource) new InventoryNavigator(rootFolder)
		.searchManagedEntity("ClusterComputeResource", ClusterName);


		// Number of rules in a cluster
		ClusterRuleInfo[] ruleinfo = ((ClusterConfigInfoEx) cluster
				.getConfigurationEx()).getRule();


		if (ruleinfo == null || ruleinfo.length == 0) {
			System.out.println("There is no DRS rule in the cluster:: "
					+ cluster.getName());
		}

		for (ClusterRuleInfo rule : ruleinfo) {
			if (((rule instanceof ClusterAffinityRuleSpec)) && (rule.getName().equals("VM VM Rule"))){
				ManagedObjectReference[] vms=((ClusterAffinityRuleSpec) rule).getVm();
				for(ManagedObjectReference vm:vms){
					if(vm.getVal().equals(vmMor1.getVal())){
						//Removed the VM from rule
						vms=(ManagedObjectReference[]) ArrayUtils.removeElement(vms, vm );
						break;
					}

				}
				//Added the new VM to the rule
				vms=(ManagedObjectReference[]) ArrayUtils.add(vms, vmMor2 );

				ClusterAffinityRuleSpec cars=(ClusterAffinityRuleSpec) rule;
				cars.setVm(vms);

				ClusterRuleSpec crs1 = new ClusterRuleSpec();
				crs1.setInfo(cars);
				crs1.setOperation(ArrayUpdateOperation.edit);

				ClusterConfigSpec ccs = new ClusterConfigSpec();
				ccs.setRulesSpec(new ClusterRuleSpec[]{crs1} );

				cluster.reconfigureCluster_Task(ccs, true);
				System.out.println("Rule reconfigured successfully ");
				si.getServerConnection().logout();

			}
		}

	}
}




package com.vmware.yavijava.samples.cluster;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import com.vmware.vim25.ArrayUpdateOperation;
import com.vmware.vim25.ClusterAffinityRuleSpec;
import com.vmware.vim25.ClusterAntiAffinityRuleSpec;
import com.vmware.vim25.ClusterConfigSpec;
import com.vmware.vim25.ClusterRuleInfo;
import com.vmware.vim25.ClusterRuleSpec;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.ClusterComputeResource;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.util.MorUtil;

//:: # Author: Vikas Shitole
//:: # Website: www.vThinkBeyondVM.com
//:: #Blog post link: http://vthinkbeyondvm.com/how-to-get-all-drs-rules-associated-with-each-vm-in-a-drs-cluster-using-api-interesting-api-in-vsphere-6-0/
//:: # Product/Feature: vCenter Server/DRS
//:: # Description: Script to find all the rules associated with all the VMs in the DRS cluster

public class FindRulesForVMsInCluster {

	public static void main(String[] args) throws Exception {
		if(args.length!=3)
		{
			System.out.println("Usage: FindRulesForVM url username password");
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
		String username = args[1];
		String password = args[2];
		String ClusterName = "My-Cluster"; //Your DRS cluster Name
		ManagedEntity vms[] = null;

		// Initialize the system, set up web services
		ServiceInstance si = new ServiceInstance(url, username,
				password, true);

		Folder rootFolder = si.getRootFolder();

		ClusterComputeResource cluster = null;
		cluster = (ClusterComputeResource) new InventoryNavigator(rootFolder)
		.searchManagedEntity("ClusterComputeResource", ClusterName);

		System.out.println("Cluster name::"+cluster.getName());

		vms = (ManagedEntity[]) new InventoryNavigator(cluster)
		.searchManagedEntities("VirtualMachine");

		for(ManagedEntity vm:vms){
			//New vSphere 6.0 method to find the VM affinity rules associated with particular VM
			ClusterRuleInfo[] rules=cluster.findRulesForVm((VirtualMachine) vm);
			System.out.println("Rules assciated with VM "+vm.getName()+" are::");
			for(ClusterRuleInfo rule:rules){
				System.out.println(rule.getName()+":" + rule.getEnabled());
			}
			System.out.println("================================");
		}
		si.getServerConnection().logout();
	}
}
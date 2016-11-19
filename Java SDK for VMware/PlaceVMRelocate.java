//:: # Author: Vikas Shitole
//:: # Website: www.vThinkBeyondVM.com
//:: # Product/Feature: vCenter Server/DRS
//:: # Reference:
//:: # Description: Tutorial: PlaceVM API: Live relocate a VM from one DRS cluster to another DRS cluster (in a Datacenter or across Datacenter)
//:: # How cool is it when DRS takes care of placement from cpu/mem perspective and at the same time SDRS take care of storage placement
//::# How to run this sample: http://vthinkbeyondvm.com/getting-started-with-yavi-java-opensource-java-sdk-for-vmware-vsphere-step-by-step-guide-for-beginners/

package com.vmware.yavijava;

import java.net.MalformedURLException;
import java.net.URL;
import com.vmware.vim25.ClusterAction;
import com.vmware.vim25.ClusterRecommendation;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.PlacementAction;
import com.vmware.vim25.PlacementResult;
import com.vmware.vim25.PlacementSpec;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.mo.ClusterComputeResource;
import com.vmware.vim25.StoragePlacementSpecPlacementType;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.StoragePod;
import com.vmware.vim25.mo.VirtualMachine;

public class PlaceVMRelocate {

	public static void main(String[] args) throws Exception {
		if(args.length!=3)
		{
			System.out.println("Usage: PlaceVMRelocate url username password");
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
		String SourceClusterName = "Cluster1"; //Source cluster Name, It is not required to have DRS enabled
		String DestinationClusterName="Cluster2"; //Destination cluster with DRS enabled
		String SDRSClusterName1="POD_1"; //SDRS POD
		String VMTobeRelocated="VM2"; //VM Name to be relocated to other cluster
		ManagedEntity[] hosts=null;

		// Initialize the system, set up web services
		ServiceInstance si = new ServiceInstance(url, username,
				password, true);
		if(si==null){
			System.out.println("ServiceInstance Returned NULL, please check your vCenter is up and running ");
		}
		Folder rootFolder = si.getRootFolder();
		ManagedObjectReference folder=rootFolder.getMOR();
		StoragePod pod1=null;

		//Getting datacenter object
		Datacenter dc=(Datacenter) new InventoryNavigator(rootFolder)
				.searchManagedEntity("Datacenter", "vcqaDC");

		//Getting SDRS POD object
		pod1=(StoragePod) new InventoryNavigator(rootFolder)
				.searchManagedEntity("StoragePod", SDRSClusterName1);
		ManagedObjectReference podMor1=pod1.getMOR();
		ManagedObjectReference[] pods={podMor1};

		//Getting source cluster object, It is NOT needed to enable DRS on source cluster
		ClusterComputeResource cluster1 = null;
		cluster1 = (ClusterComputeResource) new InventoryNavigator(rootFolder)
				.searchManagedEntity("ClusterComputeResource", SourceClusterName);

		//Getting VM object to be relocated
		VirtualMachine vm=null;
		vm=(VirtualMachine) new InventoryNavigator(cluster1)
				.searchManagedEntity("VirtualMachine", VMTobeRelocated);
		ManagedObjectReference vmMor=vm.getMOR();

		//Getting destination cluster object, DRS must be enabled on the destination cluster
		ClusterComputeResource cluster2 = null;
		cluster2 = (ClusterComputeResource) new InventoryNavigator(rootFolder)
				.searchManagedEntity("ClusterComputeResource", DestinationClusterName);
		ManagedObjectReference cluster2Mor=cluster2.getMOR();

		//Getting all the host objects from destination cluster.
		hosts =  new InventoryNavigator(cluster2).searchManagedEntities("HostSystem");
		System.out.println("Number of hosts in the destination cluster::" + hosts.length);
		ManagedObjectReference[] hostMors=new ManagedObjectReference[hosts.length];
		int i=0;
		for(ManagedEntity hostMor: hosts){
			hostMors[i]=hostMor.getMOR();
			i++;
		}

		//Buiding placement Spec to be sent to PlaceVM API
		PlacementSpec placeSpec=new PlacementSpec();
		placeSpec.setPlacementType(StoragePlacementSpecPlacementType.relocate.name());
		placeSpec.setPriority(VirtualMachineMovePriority.highPriority);
		// placeSpec.setDatastores(dss); //We can pass array of datastores of choice as well
		placeSpec.setStoragePods(pods); // Destination storage SDRS POD (s)
		placeSpec.setVm(vmMor); //VM to be relocated
		placeSpec.setHosts(hostMors); //Destination DRS cluster hosts/ We can keep this unset as well
		placeSpec.setKey("xvMotion placement");
		VirtualMachineRelocateSpec vmrelocateSpec=new VirtualMachineRelocateSpec();
		vmrelocateSpec.setPool(cluster2.getResourcePool().getMOR()); //Destination cluster root resource pool
		vmrelocateSpec.setFolder(dc.getVmFolder().getMOR()); //Destination Datacenter Folder
		placeSpec.setRelocateSpec(vmrelocateSpec);
		PlacementResult placeRes=   cluster2.placeVm(placeSpec); 
		System.out.println("PlaceVM() API is called");

		//Getting all the recommendations generated by placeVM API
		ClusterRecommendation[] clusterRec=placeRes.getRecommendations();
		ClusterAction[] action= clusterRec[0].action;
		VirtualMachineRelocateSpec vmrelocateSpecNew=null;
		vmrelocateSpecNew=((PlacementAction) action[0]).getRelocateSpec();
		vm.relocateVM_Task(vmrelocateSpecNew, VirtualMachineMovePriority.highPriority);

		si.getServerConnection().logout();
	}

}




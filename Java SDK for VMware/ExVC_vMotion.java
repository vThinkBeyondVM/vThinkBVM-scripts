/**
 * NOTE: This sample was written using the legacy open source VIJava project, which is no longer maintained by its original maintainer.
 * The legacy VIJava project was last supported up to vSphere 6.0. These samples may still work on newer vSphere versions, but this is not guaranteed and they have not been tested on the latest versions.
 * For all new development, please use the official VMware vSphere Management SDKs (also known as vSphere Web Services SDK).
 * Download and documentation for the latest vSphere SDKs: https://developer.broadcom.com/sdks?tab=Compute%2520Virtualization
 */
//:: # Author: Vikas Shitole
//:: # Website: www.vThinkBeyondVM.com
//:: # Product/Feature: vCenter Server/DRS/vMotion
//:: # Description: Extended Cross VC vMotion using RelocateVM_Task() API. vMotion between vCenters (with same SSO domain or different SSO domain)
package com.vmware.yavijava;

import java.net.MalformedURLException;
import java.net.URL;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ServiceLocator;
import com.vmware.vim25.ServiceLocatorCredential;
import com.vmware.vim25.ServiceLocatorNamePassword;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualDeviceDeviceBackingInfo;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.mo.ClusterComputeResource;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;



public class ExVC_vMotion {

	public static void main(String[] args) throws Exception {
		if(args.length!=7)
		{  
			//Parameters you need to pass
			System.out.println("Usage: ExVC_vMotion srcVCIP srcVCusername srcVCpassword destVCIP destVCusername destVCpassword destHostIP");
			System.exit(-1);
		}

		URL url1 = null;
		try 
		{ 
			url1 = new URL("https://"+args[0]+"/sdk"); 
		} catch ( MalformedURLException urlE)
		{
			System.out.println("The URL provided is NOT valid. Please check it.");
			System.exit(-1);
		}

		String srcusername = args[1];
		String srcpassword = args[2];
		String DestVC=args[3];
		String destusername=args[4];
		String destpassword = args[5];
		String destvmhost=args[6];
		
		//Hardcoded parameters for simplification
		String vmName="VM1"; //VM name to be migrated
		String vmNetworkName="VM Network"; //destination vSphere VM port group name where VM will be migrated
		String destClusterName="ClusterVC2"; //Destination VC cluster where VM will be migrated
		String destdatastoreName="DS1"; //destination datastore where VM will be migrated
		String destVCThumpPrint="c7:bc:0c:a3:15:35:57:bd:fe:ac:60:bf:87:25:1c:07:a9:31:50:85"; //SSL Thumbprint (SHA1) of the destination VC

		// Initialize source VC
		ServiceInstance vc1si = new ServiceInstance(url1, srcusername,
				srcpassword, true);
		URL url2 = null;
		try 
		{ 
			url2 = new URL("https://"+DestVC+"/sdk"); 
		} catch ( MalformedURLException urlE)
		{
			System.out.println("The URL provided is NOT valid. Please check it.");
			System.exit(-1);
		}

		// Initialize destination VC

		ServiceInstance vc2si = new ServiceInstance(url2, destusername,
				destpassword, true);
		Folder vc1rootFolder = vc1si.getRootFolder();
		Folder vc2rootFolder = vc2si.getRootFolder();

		//Virtual Machine Object to be migrated
		VirtualMachine vm = null;
		vm = (VirtualMachine) new InventoryNavigator(vc1rootFolder)
		.searchManagedEntity("VirtualMachine", vmName);
		
		//Destination host object where VM will be migrated
		HostSystem host = null;
		host = (HostSystem) new InventoryNavigator(vc2rootFolder)
		.searchManagedEntity("HostSystem", destvmhost);
		ManagedObjectReference hostMor=host.getMOR();

		//Destination cluster object creation
		ClusterComputeResource cluster = null;
		cluster = (ClusterComputeResource) new InventoryNavigator(vc2rootFolder)
		.searchManagedEntity("ClusterComputeResource", destClusterName);
		
		//Destination datastore object creation
		Datastore ds=null;
		ds = (Datastore) new InventoryNavigator(vc2rootFolder)
		.searchManagedEntity("Datastore", destdatastoreName);
		ManagedObjectReference dsMor=ds.getMOR();

		VirtualMachineRelocateSpec vmSpec=new VirtualMachineRelocateSpec();
		vmSpec.setDatastore(dsMor);
		vmSpec.setHost(hostMor);
		vmSpec.setPool(cluster.getResourcePool().getMOR());

		//VM device spec for the VM to be migrated
		VirtualDeviceConfigSpec vdcSpec=new VirtualDeviceConfigSpec();
		VirtualDevice[] devices= vm.getConfig().getHardware().getDevice();
		for(VirtualDevice device:devices){

			if(device instanceof VirtualEthernetCard){

				VirtualDeviceDeviceBackingInfo vddBackingInfo= (VirtualDeviceDeviceBackingInfo) device.getBacking();
				vddBackingInfo.setDeviceName(vmNetworkName);
				device.setBacking(vddBackingInfo);
				vdcSpec.setDevice(device); 
			}

		}

		vdcSpec.setOperation(VirtualDeviceConfigSpecOperation.edit);
		VirtualDeviceConfigSpec[] vDeviceConSpec={vdcSpec};
		vmSpec.setDeviceChange(vDeviceConSpec);

		//Below is code for ServiceLOcator which is key for this vMotion happen
		ServiceLocator serviceLoc=new ServiceLocator();
		ServiceLocatorCredential credential=new ServiceLocatorNamePassword();
		((ServiceLocatorNamePassword) credential).setPassword(destpassword);
		((ServiceLocatorNamePassword) credential).setUsername(destusername);
		serviceLoc.setCredential(credential);

		String instanceUuid=vc2si.getServiceContent().getAbout().getInstanceUuid();
		serviceLoc.setInstanceUuid(instanceUuid);
		serviceLoc.setSslThumbprint(destVCThumpPrint);
		serviceLoc.setUrl("https://"+DestVC);
		vmSpec.setService(serviceLoc);
		System.out.println("VM relocation started....please wait");
		boolean flag=false;
		vm.relocateVM_Task(vmSpec, VirtualMachineMovePriority.highPriority);
		flag=true;
		if(flag){
			System.out.println("VM is relocated to 2nd vCenter server");
		}
		vc1si.getServerConnection().logout();
		vc2si.getServerConnection().logout();
	}
}

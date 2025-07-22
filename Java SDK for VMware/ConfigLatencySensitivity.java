/**
 * NOTE: This sample was written using the legacy open source VIJava project, which is no longer maintained by its original maintainer.
 * The legacy VIJava project was last supported up to vSphere 6.0. These samples may still work on newer vSphere versions, but this is not guaranteed and they have not been tested on the latest versions.
 * For all new development, please use the official VMware vSphere Management SDKs (also known as vSphere Web Services SDK).
 * Download and documentation for the latest vSphere SDKs: https://developer.broadcom.com/sdks?tab=Compute%2520Virtualization
 */
//:: # Author: Vikas Shitole
//:: # Website: www.vThinkBeyondVM.com
//:: # Product/Feature: vCenter Server/Latency Sensitivity of the VM(Exclusive pCPU-vCPU affinity)
//:: # Description: Script to configure latency sensitivity feature on the VM and getting list of VMs where LS is configured.
//:# How to run this sample: http://vthinkbeyondvm.com/getting-started-with-yavi-java-opensource-java-sdk-for-vmware-vsphere-step-by-step-guide-for-beginners/
//:# Reference: http://www.vmware.com/files/pdf/techpaper/latency-sensitive-perf-vsphere55.pdf

package com.vmware.yavijava;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.LatencySensitivity;
import com.vmware.vim25.LatencySensitivitySensitivityLevel;
import com.vmware.vim25.ResourceAllocationInfo;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInfoState;
import com.vmware.vim25.VirtualHardware;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.util.MorUtil;


public class ConfigLatencySensitivity {

	public static void main(String[] args) throws InvalidProperty,
	RuntimeFault, RemoteException, MalformedURLException {
		
		if(args.length!=5)
		{
			System.out.println("Usage: Java ConfigLatencySensitivity vCurl username password hostname/IP VMName");
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
		
		ServiceInstance si = new ServiceInstance(new URL(args[0]), args[1],
				args[2], true); // Pass 3 argument as vCenterIP/username/password

		String VMHost=args[3]; //Host on which VM is resided
		String VMToBeConfigured=args[4];  //VM name to be configured.
		
		Folder rootFolder = si.getRootFolder();
		VirtualMachine vm1 = (VirtualMachine) new InventoryNavigator(rootFolder)
				.searchManagedEntity("VirtualMachine", VMToBeConfigured);

		HostSystem host = (HostSystem) new InventoryNavigator(rootFolder)
				.searchManagedEntity("HostSystem",  VMHost);



		//Check how many Mhz CPU can be reserved per vCPU of the VM
		int cpuMhz=host.getSummary().getHardware().getCpuMhz();
		System.out.println("cpuMHz that can be reserved per vCPU::"+cpuMhz);

		//Get RAM and vCPU configured to VM while creating that VM
		VirtualHardware vHw=vm1.getConfig().getHardware();
		int vmMem=vHw.getMemoryMB();
		System.out.println("RAM of the VM " +vm1.getName()+" ::"+vmMem);
		int vCpu=vHw.getNumCPU();
		System.out.println("Number of vCPUs configured on VM "+vm1.getName()+ " are::"+ vCpu);

		VirtualMachineConfigSpec vmSpec=new VirtualMachineConfigSpec();

		//Set the latency sensitive level flag to "high"
		LatencySensitivity ls=new LatencySensitivity();
		ls.setLevel(LatencySensitivitySensitivityLevel.high);
		vmSpec.setLatencySensitivity(ls);

		// It is highly recommended to reserve the CPU in Mhz equal to Multiples of vCPU
		ResourceAllocationInfo cpuAlloc=new ResourceAllocationInfo();
		cpuAlloc.setReservation((long) (vCpu*cpuMhz));
		vmSpec.setCpuAllocation(cpuAlloc);

		//It is highly recommended to reserve the memory equal to RAM of the VM
		ResourceAllocationInfo memAlloc=new ResourceAllocationInfo();
		memAlloc.setReservation((long) vmMem);
		vmSpec.setMemoryAllocation(memAlloc);

		//Reconfigure the VM and check reconfigure task status
		Task task=vm1.reconfigVM_Task(vmSpec);
		System.out.println("Reconfigure Task started ......");

		//Wait till task is either queued or running, that will help us to verify whether task is successful or not
		while(task.getTaskInfo().getState().equals(TaskInfoState.queued)||task.getTaskInfo().getState().equals(TaskInfoState.running) ){
			System.out.println(task.getTaskInfo().getState());
		}
		if(task.getTaskInfo().getState().equals(TaskInfoState.success))
		{
			System.out.println("Latency sensitive feature is enabled on the VM "+vm1.getName()+" successfully");
		}else{

			System.out.println("Latency sensitive feature is NOT enabled on the VM "+vm1.getName()+" successfully");
		}

		//List of VMs with latency sensitivity enabled will be printed for reference.
		System.out.println("==============================================================");
		System.out.println("List of VMs where Latency sensitivity feature is enabled");
		System.out.println("==============================================================");
		ManagedEntity[] vms = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
		for(ManagedEntity vm:vms){
			VirtualMachine vmMo = (VirtualMachine) MorUtil
					.createExactManagedEntity(si.getServerConnection(), vm.getMOR()); 
			if(vmMo.getConfig().getLatencySensitivity().getLevel().equals(LatencySensitivitySensitivityLevel.high)){

				System.out.println(vmMo.getName());
			}
		}

		si.getServerConnection().logout();
	}

}



/**
 * NOTE: This sample was written using the legacy open source VIJava project, which is no longer maintained by its original maintainer.
 * The legacy VIJava project was last supported up to vSphere 6.0. These samples may still work on newer vSphere versions, but this is not guaranteed and they have not been tested on the latest versions.
 * For all new development, please use the official VMware vSphere Management SDKs (also known as vSphere Web Services SDK).
 * Download and documentation for the latest vSphere SDKs: https://developer.broadcom.com/sdks?tab=Compute%2520Virtualization
 */
//:: # Author: Vikas Shitole
//:: # Website: www.vThinkBeyondVM.com
//:: # Product/Feature: vCenter Server/Latency Sensitivity of the VM(Exclusive pCPU-vCPU affinity)
//:: # Description: Script to audit Latency sensitivity feature configuration on all the VMs in a vCenter Server.
//:# How to run this sample: http://vthinkbeyondvm.com/getting-started-with-yavi-java-opensource-java-sdk-for-vmware-vsphere-step-by-step-guide-for-beginners/
//:# Reference: http://www.vmware.com/files/pdf/techpaper/latency-sensitive-perf-vsphere55.pdf

package com.vmware.yavijava;

import java.util.HashMap;
import java.util.Map;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.LatencySensitivitySensitivityLevel;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.VirtualHardware;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.util.MorUtil;


public class AuditLatencySensitivityConfig {

	public static void main(String[] args) throws InvalidProperty,
	RuntimeFault, RemoteException, MalformedURLException {

		if(args.length!=3)
		{
			System.out.println("Usage: Java AuditLatencySensitivityConfig vCurl username password ");
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

		// Initialize the system, set up web services

		ServiceInstance si = new ServiceInstance(url, username,
				password, true); // Pass 3 argument as vCenter URL/username/password

		Folder rootFolder = si.getRootFolder();
		System.out.println("===========================================================================================");
		System.out.println("Auditing configuration of Latency Sensitivity Enabled VMs");
		System.out.println("Audit criteria:");

		System.out.println("1. VM with LS level set to high should have CPU reservation in multiples of vCPU configured");
		System.out.println("2. VM with LS level set to high should have memory reservation equal to VM RAM");
		System.out.println("===========================================================================================");

		//Maps to store VMs with LS configured and mis-configured
		Map<String,String> lsConfiguredVms=new HashMap<String, String>();
		Map<String,String> lsMisConfiguredVms=new HashMap<String, String>();

		//Getting all the hosts available in vCenter Server inventory
		ManagedEntity[] hosts = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");

		//Iterating through 1 host at a time as follows
		for(ManagedEntity host1 :hosts){

			HostSystem host = (HostSystem) MorUtil
					.createExactManagedEntity(si.getServerConnection(), host1.getMOR());

			//Check how many Mhz CPU can be reserved per vCPU wrt specified host
			int cpuMhz=host.getSummary().getHardware().getCpuMhz();

			//Getting all the VMs available on the host
			ManagedEntity[] vms = new InventoryNavigator(host).searchManagedEntities("VirtualMachine");
			if(vms!=null){
				//Iterating through each and very VMs available on perticular host
				for(ManagedEntity vm:vms){
					VirtualMachine vmMo = (VirtualMachine) MorUtil
							.createExactManagedEntity(si.getServerConnection(), vm.getMOR()); 

					//Check whether latency sensitivity property set on the hosts or not
					if(vmMo.getConfig().getLatencySensitivity().getLevel().equals(LatencySensitivitySensitivityLevel.high)){
						VirtualHardware vHw=vmMo.getConfig().getHardware();
						int vmMem=vHw.getMemoryMB(); //RAM of the VM
						int vCpu=vHw.getNumCPU(); //vCPUs configured to the VM
						VirtualMachineConfigInfo vmConfigInfo=vmMo.getConfig();
						long cpuReservation=vmConfigInfo.getCpuAllocation().getReservation(); //CPU reservation on the VM
						long memReservation=vmConfigInfo.getMemoryAllocation().getReservation(); //Memory reservation on the VM

						//Compare mem/cpu reservation wrt configured memory/vCPUs on the host
						if((cpuReservation==(vCpu*cpuMhz))&&(memReservation==vmMem)){
							lsConfiguredVms.put(vmMo.getName(),host.getName());
						}else{
							lsMisConfiguredVms.put(vmMo.getName(),host.getName());
						}
					}
				}
			}else{
				System.out.println("NO VMs available on the specified host");
			}
		}
		System.out.println("VM list where Latency sensitivity is configured properly");
		System.out.println("-------------------------------------------------------------");
		for (Map.Entry< String , String> lsConfiguredVm : lsConfiguredVms.entrySet()) {
			System.out.println("[" + lsConfiguredVm.getKey() + "::" + lsConfiguredVm.getValue()
			+ "]");
		}
		System.out.println("\n VM list where Latency sensitivity is NOT configured properly");
		System.out.println("-------------------------------------------------------------");
		for (Map.Entry< String , String> lsMisConfiguredVm : lsMisConfiguredVms.entrySet()) {
			System.out.println("[" + lsMisConfiguredVm.getKey() + "::" + lsMisConfiguredVm.getValue()
			+ "]");
		}
		si.getServerConnection().logout();
	}
}



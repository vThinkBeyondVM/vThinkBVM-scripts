/**
 * NOTE: This sample was written using the legacy open source VIJava project, which is no longer maintained by its original maintainer.
 * For all new development, please use the official VMware vSphere Management SDKs (also known as vSphere Web Services SDK).
 * Download and documentation for the latest vSphere SDKs: https://developer.broadcom.com/sdks?tab=Compute%2520Virtualization
 */
//:: # Author: Vikas Shitole
//:: # Website: www.vThinkBeyondVM.com
//:: # Product/Feature: vCenter Server/ESXi/Storage/vFlash
//:: # Description: Script to get vFlash Cache read read info for each vmdk of the VM. such as vFlashCacheReservation//Allocation//Block size etc.
//::# How to run this sample: http://vthinkbeyondvm.com/getting-started-with-yavi-java-opensource-java-sdk-for-vmware-vsphere-step-by-step-guide-for-beginners/
//:# Refer APi reference: http://www.yavijava.com/docs/


package com.vmware.yavijava;

import java.net.MalformedURLException;
import java.net.URL;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskVFlashCacheConfigInfo;
import com.vmware.vim25.VirtualHardware;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class GetvFlashReadCacheRunTimeInfoForVM {

	public static void main(String[] args) throws Exception {

		if(args.length!=3)
		{
			System.out.println("Usage: GetvFlashReadCacheRunTimeInfoForVM url username password");
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
		ServiceInstance si = new ServiceInstance(url, username,
				password, true);

		String vmName="VM3"; //VM with vFlash read cache is enabled
		Folder rootFolder = si.getRootFolder();
		VirtualMachine vm=null;
		vm = (VirtualMachine) new InventoryNavigator(rootFolder)
		.searchManagedEntity("VirtualMachine", vmName);
		if (vm == null) {
			System.out.println("vm not found on vCenter");
			si.getServerConnection().logout();
			return;
		}
		VirtualMachineConfigInfo vmConInfo=vm.getConfig();
		VirtualMachineRuntimeInfo vmRunInfo=vm.getRuntime();
		long vFlashCacheReservation=vmConInfo.getVFlashCacheReservation();
		System.out.println("vFlash Cache Reservation for VM "+vm.getName()+" is ::"+vFlashCacheReservation/(1024*1024*1024)+"GB");
		if(vmRunInfo!=null){
			if((vmRunInfo.getVFlashCacheAllocation()!=null)){
				System.out.println("vFlash Cache Allocation for VM "+vm.getName()+" is :: "+(vmRunInfo.getVFlashCacheAllocation())/(1024*1024*1024)+"GB");
			}else{
				System.out.println("vFlashCacheAllocation is NULL/UNSET");
			}
		}

		VirtualHardware hardware=vm.getConfig().getHardware();
		VirtualDevice[] devices=hardware.getDevice();
		for(VirtualDevice device:devices){
			if(device instanceof VirtualDisk){
				VirtualDiskVFlashCacheConfigInfo vflashCache=((VirtualDisk) device).getVFlashCacheConfigInfo();
				if(vflashCache!=null){
					System.out.println("Block size in KB::"+vflashCache.getBlockSizeInKB());
					System.out.println("vFlash Reservation in GB ::"+vflashCache.getReservationInMB()/1024);
				}else{
					System.out.println("VirtualDiskVFlashCacheConfigInfo is NULL/UNSET");
				}
			}
		}
		si.getServerConnection().logout();

	}
}



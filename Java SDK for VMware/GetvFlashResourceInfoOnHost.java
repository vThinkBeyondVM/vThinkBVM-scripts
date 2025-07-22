// NOTE: These samples were developed on older vSphere versions. While they may work on the latest versions, they are not tested release over release. Use at your own risk.
/**
 * NOTE: This sample was written using the legacy open source VIJava project, which is no longer maintained by its original maintainer.
 * For all new development, please use the official VMware vSphere Management SDKs (also known as vSphere Web Services SDK).
 * Download and documentation for the latest vSphere SDKs: https://developer.broadcom.com/sdks?tab=Compute%2520Virtualization
 */
//:: # Author: Vikas Shitole
//:: # Website: www.vThinkBeyondVM.com
//:: # Product/Feature: vCenter Server/ESXi/Storage/vFlash/VFFS
//:: # Description: Script to get vFlash Volume capacity, usage. 
//::# How to run this sample: http://vthinkbeyondvm.com/getting-started-with-yavi-java-opensource-java-sdk-for-vmware-vsphere-step-by-step-guide-for-beginners/
//:# Refer API reference: http://www.yavijava.com/docs/

package com.vmware.yavijava;

import java.net.MalformedURLException;
import java.net.URL;
import com.vmware.vim25.HostConfigInfo;
import com.vmware.vim25.HostRuntimeInfo;
import com.vmware.vim25.HostVFlashManagerVFlashConfigInfo;
import com.vmware.vim25.HostVFlashManagerVFlashResourceConfigInfo;
import com.vmware.vim25.HostVFlashManagerVFlashResourceRunTimeInfo;
import com.vmware.vim25.HostVffsVolume;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;

public class GetvFlashResourceInfoOnHost {

	public static void main(String[] args) throws Exception {

		if(args.length!=3)
		{
			System.out.println("Usage: GetvFlashResourceInfoOnHost url username password");
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

		String hostname = "192.168.10.20"; //Your ESXi host where VFFS volume is added.


		Folder rootFolder = si.getRootFolder();
		HostSystem host = null;

		host = (HostSystem) new InventoryNavigator(rootFolder)
		.searchManagedEntity("HostSystem", hostname);


		if (host == null) {
			System.out.println("Host not found on vCenter");
			si.getServerConnection().logout();
			return;
		}

		//Using HostConfigInfo
		HostConfigInfo hostconfig=host.getConfig();
		HostVFlashManagerVFlashConfigInfo vFlashConfigInfo= hostconfig.getVFlashConfigInfo();
		HostVFlashManagerVFlashResourceConfigInfo vFlashResourceConfigInfo=vFlashConfigInfo.getVFlashResourceConfigInfo();
		System.out.println("vFlash Resource Capacity ::"+vFlashResourceConfigInfo.getCapacity());

		//Using HostVffsVolume
		HostVffsVolume vffsVol=vFlashResourceConfigInfo.getVffs();
		System.out.println("vFlash Resource Capacity ::"+vffsVol.getCapacity());

		//Using HostRuntimeInfo
		HostRuntimeInfo hostInfo=host.getRuntime();
		HostVFlashManagerVFlashResourceRunTimeInfo vFlashInfo=hostInfo.getVFlashResourceRuntimeInfo();

		if (vFlashInfo == null) {
			System.out.println("vFlash Info is NULL, hence can not fetch Run Time vFlash Capacity/usage etc.");
			si.getServerConnection().logout();
			return;
		} else{

			System.out.println("is vFlash Resource accessible::"+vFlashInfo.isAccessible());
			System.out.println("vFlash Resource Capacity::"+vFlashInfo.getCapacity());
			System.out.println("vFlash Resource Usage::"+vFlashInfo.getUsage());
		}	
		si.getServerConnection().logout();
	}
}


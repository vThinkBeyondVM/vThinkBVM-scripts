//:: # Author: Vikas Shitole
//:: # Website: www.vThinkBeyondVM.com
//:: # Product/Feature: vCenter Server/NFS
//:: # Description: Creating/Mounting NFS datastore using vSphere API. 
//::# How to run this sample: http://vthinkbeyondvm.com/getting-started-with-yavi-java-opensource-java-sdk-for-vmware-vsphere-step-by-step-guide-for-beginners/


package com.vmware.vim25.mo.samples.storage;

import java.net.MalformedURLException;
import java.net.URL;

import com.vmware.vim25.HostNasVolumeSpec;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostDatastoreSystem;

import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;

public class CreateNFSDatastore {

	public static void main(String[] args) throws Exception {
		if(args.length!=4)
		{
			System.out.println("Usage: CreateNFSDatastore url username password hostip/fqdn");
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
		String hostname = args[3]; // host IP on which to mount NFS share

		ServiceInstance si = new ServiceInstance(url, username,
				password, true);

		Folder rootFolder = si.getRootFolder();
		HostSystem host = null;

		host = (HostSystem) new InventoryNavigator(rootFolder)
				.searchManagedEntity("HostSystem", hostname);


		if (host == null) {
			System.out.println("Host not found on vCenter");
			si.getServerConnection().logout();
			return;
		}
		HostDatastoreSystem dssystem=host.getHostDatastoreSystem();

		HostNasVolumeSpec NasSpec=new HostNasVolumeSpec();
		NasSpec.setAccessMode("readWrite");
		NasSpec.setLocalPath("NFSShare2"); //Name of your choice for NFS share/datastore
		NasSpec.setRemoteHost("192.168.2.3"); //IP address for NFS server
		NasSpec.setRemotePath("/store1"); //NFS mount point on NFS server

		try{
			Datastore nfsDs=dssystem.createNasDatastore(NasSpec);
			System.out.println("NFS share created"+nfsDs.getName());
			System.out.println("createNasDatastore API is called");
		}
		catch(Exception e){
			System.out.println("Exception is raised, please check the datastoreSpec"+e);
		}

		si.getServerConnection().logout();

	}
}

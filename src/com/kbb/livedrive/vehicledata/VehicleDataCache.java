package com.kbb.livedrive.vehicledata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.ford.syncV4.proxy.rpc.OnVehicleData;



public class VehicleDataCache {
	
	
	public List<OnVehicleData> dataCache = new ArrayList<OnVehicleData>();
	
	public synchronized void addVehicleData(OnVehicleData item){
			dataCache.add(item);	
	}
	
	public synchronized List<OnVehicleData> getDataCache() {
			List<OnVehicleData> data = new ArrayList<OnVehicleData>(dataCache);
			return data;	
	}
	
	public synchronized List<OnVehicleData> getDataCacheAndClear() {

		List<OnVehicleData> data = new ArrayList<OnVehicleData>(dataCache);
			
		dataCache.clear();
			
		return data;
	}
	
	public synchronized void clearVehicleDataCache(){
		dataCache.clear();
	}
	
}

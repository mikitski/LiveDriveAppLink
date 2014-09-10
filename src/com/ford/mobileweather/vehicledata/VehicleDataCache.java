package com.ford.mobileweather.vehicledata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.ford.syncV4.proxy.rpc.OnVehicleData;



public class VehicleDataCache {
	
	private static Object blah = new Object();
	
	public static List<OnVehicleData> dataCache = new ArrayList<OnVehicleData>();
	
	public static void addVehicleData(OnVehicleData item){
		synchronized (blah) {
			dataCache.add(item);	
		}
		
	}
	
	public static List<OnVehicleData> getDataCache() {
		synchronized (blah) {
			List<OnVehicleData> data = new ArrayList<OnVehicleData>(dataCache);
			return data;	
		}
	}
	
	public static List<OnVehicleData> getDataCacheAndClear() {
		synchronized (blah) {
			List<OnVehicleData> data = new ArrayList<OnVehicleData>(dataCache);
			
			dataCache.clear();
			
			return data;
		}
	}
	
	public static void clearVehicleDataCache(){
		dataCache.clear();
	}
	
}

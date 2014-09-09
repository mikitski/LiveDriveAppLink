package com.ford.mobileweather.vehicledata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.ford.syncV4.proxy.rpc.OnVehicleData;



public class VehicleDataCache {
	
	public static List<OnVehicleData> dataCache = new ArrayList<OnVehicleData>();
	
	public static void addVehicleData(OnVehicleData item){
		dataCache.add(item);
	}
	
	public static void clearVehicleDataCache(){
		dataCache.clear();
	}
	
}

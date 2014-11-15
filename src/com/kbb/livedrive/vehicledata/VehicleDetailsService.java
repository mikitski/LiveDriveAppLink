package com.kbb.livedrive.vehicledata;

import com.google.android.gms.common.api.ResultCallback;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class VehicleDetailsService extends Service {
	
	
	private static VehicleDetailsService instance;
	
	static{
		instance = null;
	}

	public static VehicleDetailsService getInstance(){
		return instance;
	}
	
	
	private VehicleDetails currentVehicle;
	
	public VehicleDetails getCurrent(){
		if(currentVehicle == null){
			currentVehicle = VehicleDetails.createDummy();
		}
		
		return currentVehicle;
	}

	
	@Override
	public void onCreate() {
		instance = this;
		
		super.onCreate();
	}
	
	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}
}



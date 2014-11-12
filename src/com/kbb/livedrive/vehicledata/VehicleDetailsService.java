package com.kbb.livedrive.vehicledata;

import com.google.android.gms.common.api.ResultCallback;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class VehicleDetailsService extends Service {
	
	private VehicleDetails currentVehicle;
	
	public VehicleDetails getCurrent(){
		if(currentVehicle == null){
			currentVehicle = VehicleDetails.createDummy();
		}
		
		return currentVehicle;
	}

	
	@Override
	public void onCreate() {
		
		super.onCreate();
	}
	
	public void getVehilceDetails(VehicleDetailsCallback callback){
		
		if(callback != null){
			callback.onResult(getCurrent());
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}



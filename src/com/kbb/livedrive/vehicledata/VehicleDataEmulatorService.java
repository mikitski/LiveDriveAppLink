package com.kbb.livedrive.vehicledata;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import com.ford.syncV4.proxy.rpc.OnVehicleData;

import android.app.LauncherActivity;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class VehicleDataEmulatorService extends Service {
	
	private static VehicleDataEmulatorService instance = null;
	
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	private ScheduledFuture handle;
	
	private IVehicleDataReceiver sendTo = null;
	
	private Random rand = new Random();
	private static int SPEED_LIMIT = 50;
	
	private static final int DRIVING_MODE_GOOD = 101;
	private static final int DRIVING_MODE_SPEEDING = 102;
	private static final int DRIVING_MODE_SLOW = 103;
	private static final int DRIVING_MODE_RECKLESS = 104;
	private static final int DRIVING_MODE_RACING = 105;

	private int drivingMode = DRIVING_MODE_GOOD;

	
	public static VehicleDataEmulatorService getInstance(){
		return instance;
	}
	
	@Override
	public void onCreate() {
		instance = this;
		super.onCreate();
		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		luanchBackgroundWorker();
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {

		if(handle != null){
			handle.cancel(true);
		}
		
		super.onDestroy();
	}
	
	public synchronized int getDrivingMode() {
		return drivingMode;
	}
	
	public synchronized void setDrivingMode(int mode){
		drivingMode = mode;
	}
	
	private void luanchBackgroundWorker(){
    	final Runnable dataSender = new Runnable() {
    		
    		public void run() {
    			
	    			//do something
	    			switch(getDrivingMode()) {
	    			case DRIVING_MODE_GOOD:
	    				sendGoodData();
	    				break;
	    			case DRIVING_MODE_SPEEDING:
	    				sendSpeedingData();
	    				break;
	    			case DRIVING_MODE_SLOW:
	    				sendSlowData();
	    				break;
	    			case DRIVING_MODE_RECKLESS:
	    				sendRecklessData();
	    				break;
	    			case DRIVING_MODE_RACING:
	    				sendRacingData();
	    				break;
	    			}
    		}

    	};
    	
    	handle = scheduler.scheduleAtFixedRate(dataSender, 15, 15, SECONDS);
	}
	
	private void sendRacingData() {
		
		if(sendTo != null) {
			float currSpeed = 80 + 10 * rand.nextFloat();
			
			OnVehicleData vData = new OnVehicleData();
			vData.setSpeed(Double.valueOf(currSpeed));
			sendTo.onOnVehicleData(vData);
		}
		
	}
    
	private void sendRecklessData() {
		
		if(sendTo != null){
			float currSpeed = SPEED_LIMIT + 6 + 10 * rand.nextFloat();
			
			OnVehicleData vData = new OnVehicleData();
			vData.setSpeed(Double.valueOf(currSpeed));
			sendTo.onOnVehicleData(vData);
		}
		
	}

	private void sendSlowData() {
		
		if(sendTo != null){
			float currSpeed = SPEED_LIMIT - 10 - 5 * rand.nextFloat();
			
			OnVehicleData vData = new OnVehicleData();
			vData.setSpeed(Double.valueOf(currSpeed));
			sendTo.onOnVehicleData(vData);
		}

	}

	private void sendSpeedingData() {
		if(sendTo != null){		
			float currSpeed = SPEED_LIMIT + 8 + 5 * rand.nextFloat();
			
			OnVehicleData vData = new OnVehicleData();
			vData.setSpeed(Double.valueOf(currSpeed));
			sendTo.onOnVehicleData(vData);

		}
	}

	private void sendGoodData() {
		
		if(sendTo != null) {
			float currSpeed = SPEED_LIMIT + 5 - 10 * rand.nextFloat();
			
			OnVehicleData vData = new OnVehicleData();
			vData.setSpeed(Double.valueOf(currSpeed));
			sendTo.onOnVehicleData(vData);
		}
	};

}

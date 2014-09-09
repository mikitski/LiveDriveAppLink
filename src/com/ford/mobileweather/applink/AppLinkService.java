package com.ford.mobileweather.applink;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Vector;

import com.ford.mobileweather.R;
import com.ford.mobileweather.artifact.Location;
import com.ford.syncV4.exception.SyncException;
import com.ford.syncV4.exception.SyncExceptionCause;
import com.ford.syncV4.proxy.SyncProxyALM;
import com.ford.syncV4.proxy.interfaces.IProxyListenerALM;
import com.ford.syncV4.proxy.rpc.*;
import com.ford.syncV4.proxy.rpc.enums.ButtonName;
import com.ford.syncV4.proxy.rpc.enums.DisplayType;
import com.ford.syncV4.proxy.rpc.enums.FileType;
import com.ford.syncV4.proxy.rpc.enums.Language;
import com.ford.syncV4.proxy.rpc.enums.SoftButtonType;
import com.ford.syncV4.proxy.rpc.enums.SpeechCapabilities;
import com.ford.syncV4.proxy.rpc.enums.SystemAction;
import com.ford.syncV4.proxy.rpc.enums.TextAlignment;
import com.ford.syncV4.proxy.rpc.enums.TextFieldName;
import com.ford.syncV4.transport.BTTransportConfig;
import com.ford.syncV4.transport.BaseTransportConfig;
import com.ford.syncV4.transport.TCPTransport;
import com.ford.syncV4.transport.TCPTransportConfig;
import com.ford.syncV4.util.DebugTool;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class AppLinkService extends Service implements IProxyListenerALM {

	// Service shutdown timing constants
	private static final int CONNECTION_TIMEOUT = 60000;
	private static final int STOP_SERVICE_DELAY = 5000;

	private static final int STANDARD_FORECAST_DAYS = 3;
	private static final int EXTENDED_FORECAST_DAYS = 10;
    private static final int VIEW_CURRENT_CONDITIONS = 1;
	private static final int VIEW_STANDARD_FORECAST = 2;
	private static final int VIEW_EXTENDED_FORECAST = 3;

	private static final int TIMED_SHOW_DELAY = 8000;

	private static final int SHOW_CONDITIONS_ID = 101;
	private static final int SHOW_STANDARD_FORECAST_ID = 102;
	private static final int SHOW_EXTENDED_FORECAST_ID = 103;

	private static final int CHANGE_UNITS = 4;
	private static final int CHANGE_UNITS_CHOICESET = 1;
	private static final int METRIC_CHOICE = 1;
	private static final int IMPERIAL_CHOICE = 2;
	
	// variable used to increment correlation ID for every request sent to SYNC
	public int autoIncCorrId = 0;
	// variable to contain the current state of the service
	private static AppLinkService instance = null;
	// variable to access the BluetoothAdapter
	private BluetoothAdapter btAdapter;
	// variable to create and call functions of the SyncProxy
	private SyncProxyALM proxy = null;
	private Handler handler = null;

	private Language currentSyncLanguage = null;	// Stores the current language of the SYNC module
	private Language currentHmiLanguage = null;	// Stores the current language of the display
    private boolean firstHmiNone = true;
    private DisplayType displayType = null;	// Keeps track of the HMI display type
    private boolean graphicsSupported = false;	// Keeps track of whether graphics are supported on the display
    private int numberOfTextFields = 2;
	private int lengthOfTextFields = 40;
    private Vector<TextField> textFields = null;	// Keeps track of the text fields supported

	private Handler timedShowHandler = null;

    private Location currentLocation = null;	// Stores the current location for weather

	private SoftButton showConditions = null;
	private SoftButton showStandardForecast = null;
	private SoftButton showExtendedForecast = null;

	private Boolean unitsInMetric = true;
	private String tempUnitsShort = "C";
	private String speedUnitsShort = "KPH";
	private String speedUnitsFull = "kilometers per hour";
	private String lengthUnitsFull = "millimeters";

	/**
	 * Receiver for changes in location from the app UI.
	 */
	protected final BroadcastReceiver changeLocationReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
        }
	};

	/**
	 * Receiver to handle updates to weather conditions.
	 */
	private final BroadcastReceiver weatherConditionsReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
        }
	};

	/**
	 * Receiver to handle updates to the forecast.
	 */
	private final BroadcastReceiver forecastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
	};

	/**
	*  Runnable that stops this service if there hasn't been a connection to SYNC
	*  within a reasonable amount of time since ACL_CONNECT.
	*/
	private Runnable checkConnectionRunnable = new Runnable() {
		@Override
		public void run() {
			Boolean stopService = true;
			// If the proxy has connected to SYNC, do NOT stop the service
			if (proxy != null && proxy.getIsConnected()) {
				stopService = false;
			}
			if (stopService) {
				handler.removeCallbacks(checkConnectionRunnable);
				handler.removeCallbacks(stopServiceRunnable);
				stopSelf();
			}
		}
	};

	/**
	* Runnable that stops this service on ACL_DISCONNECT after a short time delay.
	* This is a workaround until some synchronization issues are fixed within the proxy.
	*/
	private Runnable stopServiceRunnable = new Runnable() {
		@Override
		public void run() {
			// As long as the proxy is null or not connected to SYNC, stop the service
			if (proxy == null || !proxy.getIsConnected()) {
				handler.removeCallbacks(checkConnectionRunnable);
				handler.removeCallbacks(stopServiceRunnable);
				stopSelf();
			}
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		handler = new Handler();
		
		LocalBroadcastManager lbManager = LocalBroadcastManager.getInstance(this);
		lbManager.registerReceiver(changeLocationReceiver, new IntentFilter("com.ford.mobileweather.Location"));
		lbManager.registerReceiver(weatherConditionsReceiver, new IntentFilter("com.ford.mobileweather.WeatherConditions"));
		lbManager.registerReceiver(forecastReceiver, new IntentFilter("com.ford.mobileweather.Forecast"));

		showConditions = new SoftButton();
		showConditions.setSoftButtonID(SHOW_CONDITIONS_ID);
		showConditions.setText("Current");
		showConditions.setType(SoftButtonType.SBT_TEXT);
		showConditions.setIsHighlighted(false);
		showConditions.setSystemAction(SystemAction.DEFAULT_ACTION);

		showStandardForecast = new SoftButton();
		showStandardForecast.setSoftButtonID(SHOW_STANDARD_FORECAST_ID);
		showStandardForecast.setText("3-Day");
		showStandardForecast.setType(SoftButtonType.SBT_TEXT);
		showStandardForecast.setIsHighlighted(false);
		showStandardForecast.setSystemAction(SystemAction.DEFAULT_ACTION);

		showExtendedForecast = new SoftButton();
		showExtendedForecast.setSoftButtonID(SHOW_EXTENDED_FORECAST_ID);
		showExtendedForecast.setText("10-Day");
		showExtendedForecast.setType(SoftButtonType.SBT_TEXT);
		showExtendedForecast.setIsHighlighted(false);
		showExtendedForecast.setSystemAction(SystemAction.DEFAULT_ACTION);

		unitsInMetric = true;
		tempUnitsShort = "C";
		speedUnitsShort = "KPH";
		speedUnitsFull = "kilometers per hour";
		lengthUnitsFull = "millimeters";

		timedShowHandler = new Handler();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Remove any previous stop service runnables that could be from a recent ACL Disconnect
		handler.removeCallbacks(stopServiceRunnable);

		startProxy();
		/*
        if (intent != null) {
	        	btAdapter = BluetoothAdapter.getDefaultAdapter();
	    		if (btAdapter != null) {
	    			if (btAdapter.isEnabled()) {
	    				startProxy();
	    			}
	    		}
		}
		*/

        // Queue the check connection runnable to stop the service if no connection is made
        handler.removeCallbacks(checkConnectionRunnable);
        handler.postDelayed(checkConnectionRunnable, CONNECTION_TIMEOUT);

        return START_STICKY;
	}

	@Override
	public void onDestroy() {
		disposeSyncProxy();
		LockScreenManager.clearLockScreen();
		instance = null;
		try {
			LocalBroadcastManager lbManager = LocalBroadcastManager.getInstance(this);
			lbManager.unregisterReceiver(changeLocationReceiver);
			lbManager.unregisterReceiver(weatherConditionsReceiver);
			lbManager.unregisterReceiver(forecastReceiver);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

	public static AppLinkService getInstance() {
		return instance;
	}

	public SyncProxyALM getProxy() {
		return proxy;
	}

	/**
	* Queue's a runnable that stops the service after a small delay,
	* unless the proxy manages to reconnects to SYNC.
	*/
	public void stopService() {
		handler.removeCallbacks(stopServiceRunnable);
		handler.postDelayed(stopServiceRunnable, STOP_SERVICE_DELAY);
	}

	public void startProxy() {
		if (proxy == null) {
			try {
				//BaseTransportConfig transport = new BTTransportConfig();
				BaseTransportConfig transport = new TCPTransportConfig(12345, "10.103.194.182", true);
				proxy = new SyncProxyALM(this, "MobileWeather", false, Language.EN_US, Language.EN_US, "330533107", transport);
			} catch (SyncException e) {
				e.printStackTrace();
				// error creating proxy, returned proxy = null
				if (proxy == null) {
					stopService();
				}
			}
		}
	}

	public void disposeSyncProxy() {
		if (proxy != null) {
			try {
				proxy.dispose();
			} catch (SyncException e) {
				e.printStackTrace();
			}
			proxy = null;
			LockScreenManager.clearLockScreen();
		}
	}

	public void reset() {
		firstHmiNone = true;

		if (proxy != null) {
			try {
				proxy.resetProxy();
			} catch (SyncException e1) {
				e1.printStackTrace();
				//something goes wrong, & the proxy returns as null, stop the service.
				// do not want a running service with a null proxy
				if (proxy == null) {
					stopService();
				}
			}
		} else {
			startProxy();
		}
	}

	@Override
	public void onProxyClosed(String info, Exception e) {
		LockScreenManager.setHMILevelState(null);
		LockScreenManager.clearLockScreen();
		firstHmiNone = true;

		if ((((SyncException) e).getSyncExceptionCause() != SyncExceptionCause.SYNC_PROXY_CYCLED))
		{
			if (((SyncException) e).getSyncExceptionCause() != SyncExceptionCause.BLUETOOTH_DISABLED) 
			{
				reset();
			}
		}
	}

    @Override
	public void onOnHMIStatus(OnHMIStatus notification) {
		switch(notification.getSystemContext()) {
			case SYSCTXT_MAIN:
				break;
			case SYSCTXT_VRSESSION:
				break;
			case SYSCTXT_MENU:
				break;
			default:
				return;
		}

		switch(notification.getAudioStreamingState()) {
			case AUDIBLE:
				break;
			case NOT_AUDIBLE:
				break;
			default:
				return;
		}

		LockScreenManager.setHMILevelState(notification.getHmiLevel());
		LockScreenManager.updateLockScreen();

		switch(notification.getHmiLevel()) {
			case HMI_FULL:
				if (notification.getFirstRun()) {
					// Perform welcome
				 	welcomeMessage();
				 	
				    // Add commands
				 	addCommands();

				 	// Subscribe buttons
				 	subscribeButtons();
				}
				// If nothing is being displayed and we're in FULL, default to current conditions

				break;
			case HMI_LIMITED:
				break;
			case HMI_BACKGROUND:
				 if (firstHmiNone) {
					 getSyncSettings();
				 }
				break;
			case HMI_NONE:				
				 if (firstHmiNone) {
					 getSyncSettings();
				 }
				 
		            PutFile msg = new PutFile();
		            msg.setSyncFileName("icon");
		            msg.setFileType(FileType.GRAPHIC_PNG);
		            msg.setPersistentFile(true);
		            msg.setCorrelationID(autoIncCorrId++);

		            Bitmap photo = BitmapFactory.decodeResource(getResources(), R.drawable.logo_ce);
		            ByteArrayOutputStream bas = new ByteArrayOutputStream();
		            photo.compress(CompressFormat.PNG, 100, bas);
		            byte[] data = new byte[bas.toByteArray().length];
		            data = bas.toByteArray();
		            msg.setBulkData(data);


		            try {
		                 proxy.sendRPCRequest(msg);
		                 } catch (SyncException e1) {
		                // TODO Auto-generated catch block
		                e1.printStackTrace();
		            }				 
				break;
			default:
				return;
		}
	}
    
    private void subscribeButtons() {
	 	try {
	 		proxy.subscribeButton(ButtonName.PRESET_1, autoIncCorrId++);
		} catch (SyncException e) {
			e.printStackTrace();
			DebugTool.logError("Failed to subscribe to buttons", e);
		}
    }
    
	private void getSyncSettings() {
		try {
			// Change registration to match the language of the head unit
			currentHmiLanguage = proxy.getHmiDisplayLanguage();
			currentSyncLanguage = proxy.getSyncLanguage();
			if (currentHmiLanguage != null && currentSyncLanguage != null) {
				proxy.changeregistration(currentSyncLanguage, currentHmiLanguage, autoIncCorrId++);
			}
		} catch (SyncException e) {
			DebugTool.logError("Failed to change language", e);
		}

		try {
			// Get the display capabilities
			DisplayCapabilities displayCapabilities = proxy.getDisplayCapabilities();
			if (displayCapabilities != null) {
				displayType = displayCapabilities.getDisplayType();
				graphicsSupported = displayCapabilities.getGraphicSupported();
				textFields = displayCapabilities.getTextFields();

				if (displayType == DisplayType.CID) {
					numberOfTextFields = 1;
				}
				else if (displayType == DisplayType.MFD3 ||
						displayType == DisplayType.MFD4 ||
						displayType == DisplayType.MFD5) {
						numberOfTextFields = 2;
				}
				else {
					numberOfTextFields = 1;
				}

				if (textFields != null && textFields.size() > 0) {
					for (TextField field : textFields) {
						if (field.getName() == TextFieldName.mainField1) {
							lengthOfTextFields = field.getWidth();
						}
					}
				}
			}
		} catch (SyncException e) {
			DebugTool.logError("Failed to get display capabilities", e);
		}

        firstHmiNone = false;
	}
	
    private void addCommands() {
    		Vector<String> vrCommands = null;

		try {
			vrCommands = new Vector<String>(Arrays.asList("Current", "Current Conditions"));
			proxy.addCommand(VIEW_CURRENT_CONDITIONS, "Current Conditions", vrCommands, autoIncCorrId++);
			vrCommands = new Vector<String>(Arrays.asList("three day", "three day forecast", "3 day", "3 day forecast"));
			proxy.addCommand(VIEW_STANDARD_FORECAST, "3-Day Forecast", vrCommands, autoIncCorrId++);
			vrCommands = new Vector<String>(Arrays.asList("ten day", "ten day Forecast", "10 day", "10 day forecast"));
			proxy.addCommand(VIEW_EXTENDED_FORECAST, "10-Day Forecast", vrCommands, autoIncCorrId++);
			vrCommands = new Vector<String>(Arrays.asList("Change Units", "Units"));
			proxy.addCommand(CHANGE_UNITS, "Change Units", vrCommands, autoIncCorrId++);

	 		Choice metricChoice = new Choice();
	 		metricChoice.setChoiceID(METRIC_CHOICE);
	 		metricChoice.setMenuName("Metric");
	 		metricChoice.setVrCommands(new Vector<String>(Arrays.asList("Metric")));
	 		Choice imperialChoice = new Choice();
	 		imperialChoice.setChoiceID(IMPERIAL_CHOICE);
	 		imperialChoice.setMenuName("Imperial");
	 		imperialChoice.setVrCommands(new Vector<String>(Arrays.asList("Imperial")));
	 		proxy.createInteractionChoiceSet(new Vector<Choice>(Arrays.asList(metricChoice, imperialChoice)), CHANGE_UNITS_CHOICESET, autoIncCorrId++);
		} catch (SyncException e) {
			e.printStackTrace();
			DebugTool.logError("Failed to add commands", e);
		}
	}

    /**
     * Shows and speaks a welcome message
     */
    private void welcomeMessage() {
	 	try {
	 		proxy.show("Welcome to", "MobileWeather", TextAlignment.CENTERED, autoIncCorrId++);
			proxy.speak("YOur Driving Behavio in the past 5 minutes caused you to lose 17 point on a good driver score. Consider driving slower.", autoIncCorrId++);
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (SyncException e) {
			DebugTool.logError("Failed to perform welcome message", e);
		}
    }

	@Override
	public void onOnDriverDistraction(OnDriverDistraction notification) {
		LockScreenManager.setDriverDistractionState(notification.getState());
		LockScreenManager.updateLockScreen();
	}

	@Override
	public void onOnCommand(OnCommand notification) {
		if (notification != null) {
			int command = notification.getCmdID();
			switch (command) {
				case VIEW_CURRENT_CONDITIONS:
					break;
				case VIEW_STANDARD_FORECAST:
					break;
				case VIEW_EXTENDED_FORECAST:
					break;
				case CHANGE_UNITS:
				 	try {
	 					proxy.performInteraction("Choose units in metric or imperial", "Units", CHANGE_UNITS_CHOICESET, autoIncCorrId++);
					} catch (SyncException e) {
						e.printStackTrace();
						DebugTool.logError("Failed to perform interaction", e);
					}
					break;
				default:
					break;
			}
		}
	}

	@Override
	public void onOnButtonPress(OnButtonPress notification) {
		switch (notification.getButtonName()) {
		    case CUSTOM_BUTTON:
	            switch (notification.getCustomButtonName()) {
		            case SHOW_CONDITIONS_ID:
		                break;
		            case SHOW_STANDARD_FORECAST_ID:
		                break;
		            case SHOW_EXTENDED_FORECAST_ID:
		                break;
		    			default:
		    				break;
	            }
	        case PRESET_1:

	    	    GetVehicleData msg = new GetVehicleData();  
	    	    msg.setCorrelationID(autoIncCorrId++);

	    	    //Location functional group
	    	    msg.setSpeed(true);
	    	    msg.setGps(true);

	    	    //VechicleInfo functional group
	    	    msg.setFuelLevel(true);
	    	    msg.setFuelLevel_State(true);
	    	    msg.setInstantFuelConsumption(true);
	    	    msg.setExternalTemperature(true);
	    	    msg.setTirePressure(true);
	    	    msg.setOdometer(true);
	    	    msg.setVin(true);

	    	    //DrivingCharacteristics functional group
	    	    msg.setBeltStatus(true);                        
	    	    msg.setDriverBraking(true);
	    	    msg.setPrndl(true);
	    	    msg.setRpm(true);

	    	    try{
	    	        proxy.sendRPCRequest(msg);
	    	    }       
	    	    catch (SyncException e) {
	    	    	Log.e("shit", "get Vehicle Data no woikie");
	    	    }	        		
	        		break;
			default:
				break;
	    }
	}
	
	@Override
	public void onPerformInteractionResponse(PerformInteractionResponse response) {
		if (response.getSuccess()) {
			Integer choiceID = response.getChoiceID();
			switch (choiceID) {
			case METRIC_CHOICE:
				unitsInMetric = true;
				tempUnitsShort = "C";
				speedUnitsShort = "KPH";
				speedUnitsFull = "kilometers per hour";
				lengthUnitsFull = "millimeters";
				break;
			case IMPERIAL_CHOICE:
				unitsInMetric = false;
				tempUnitsShort = "F";
				speedUnitsShort = "MPH";
				speedUnitsFull = "miles per hour";
				lengthUnitsFull = "inches";
				break;
			}
		}
	}

	@Override
	public void onListFilesResponse(ListFilesResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onError(String info, Exception e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onGenericResponse(GenericResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onAddCommandResponse(AddCommandResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onAddSubMenuResponse(AddSubMenuResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onCreateInteractionChoiceSetResponse(CreateInteractionChoiceSetResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onAlertResponse(AlertResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDeleteCommandResponse(DeleteCommandResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDeleteInteractionChoiceSetResponse(DeleteInteractionChoiceSetResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDeleteSubMenuResponse(DeleteSubMenuResponse response) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onPutFileResponse(PutFileResponse response) {
		  try {
		        String syncFileName = "icon";
		        SetAppIcon msg = new SetAppIcon();
		        msg.setSyncFileName(syncFileName);
		        msg.setCorrelationID(autoIncCorrId++);

		        proxy.sendRPCRequest(msg);
		    } catch (SyncException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }
	}
	
	@Override
	public void onOnVehicleData(OnVehicleData notification) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onResetGlobalPropertiesResponse(ResetGlobalPropertiesResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSetGlobalPropertiesResponse(SetGlobalPropertiesResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSetMediaClockTimerResponse(SetMediaClockTimerResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onShowResponse(ShowResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSpeakResponse(SpeakResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onOnButtonEvent(OnButtonEvent notification) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSubscribeButtonResponse(SubscribeButtonResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onUnsubscribeButtonResponse(UnsubscribeButtonResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onOnPermissionsChange(OnPermissionsChange notification) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onOnTBTClientState(OnTBTClientState notification) {
		// TODO Auto-generated method stub
	}

	@Override
	public IBinder onBind(Intent intent) {
		  return null;
	}

	@Override
	public void onSubscribeVehicleDataResponse(SubscribeVehicleDataResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onUnsubscribeVehicleDataResponse(UnsubscribeVehicleDataResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onGetVehicleDataResponse(GetVehicleDataResponse response) {
		
		try{
		 proxy.show("vehicleData callback", response.getSpeed().toString(), TextAlignment.CENTERED, autoIncCorrId++);
		 
		}
		catch(SyncException e) {
			Log.e("msg", "VehicleData show");
		}
//	    Alert msg = new Alert();
//	    msg.setCorrelationID(autoIncCorrId++);
//
//	    msg.setAlertText1("Text1");
//	    msg.setAlertText2(response.getSpeed().toString());
//	    msg.setAlertText3("Text3");
//	    msg.setDuration(10000); //Time in milliseconds
//	    msg.setPlayTone(true);
//
//
//	    try{
//	        proxy.sendRPCRequest(msg);
//	    }       
//	    catch (SyncException e) {
//	    	
//	    }
	}

	@Override
	public void onReadDIDResponse(ReadDIDResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onGetDTCsResponse(GetDTCsResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPerformAudioPassThruResponse(PerformAudioPassThruResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onEndAudioPassThruResponse(EndAudioPassThruResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onOnAudioPassThru(OnAudioPassThru notification) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDeleteFileResponse(DeleteFileResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSetAppIconResponse(SetAppIconResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onScrollableMessageResponse(ScrollableMessageResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onChangeRegistrationResponse(ChangeRegistrationResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSetDisplayLayoutResponse(SetDisplayLayoutResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onOnLanguageChange(OnLanguageChange notification) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSliderResponse(SliderResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onEncodedSyncPDataResponse(EncodedSyncPDataResponse arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onOnEncodedSyncPData(OnEncodedSyncPData arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onOnSyncPData(OnSyncPData arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSyncPDataResponse(SyncPDataResponse arg0) {
		// TODO Auto-generated method stub
	}
}

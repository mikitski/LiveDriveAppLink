package com.kbb.livedrive.applink;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.*;

import com.ford.syncV4.exception.SyncException;
import com.ford.syncV4.exception.SyncExceptionCause;
import com.ford.syncV4.proxy.SyncProxyALM;
import com.ford.syncV4.proxy.interfaces.IProxyListenerALM;
import com.ford.syncV4.proxy.rpc.*;
import com.ford.syncV4.proxy.rpc.enums.ButtonName;
import com.ford.syncV4.proxy.rpc.enums.DisplayType;
import com.ford.syncV4.proxy.rpc.enums.FileType;
import com.ford.syncV4.proxy.rpc.enums.Language;
import com.ford.syncV4.proxy.rpc.enums.PRNDL;
import com.ford.syncV4.proxy.rpc.enums.SoftButtonType;
import com.ford.syncV4.proxy.rpc.enums.SpeechCapabilities;
import com.ford.syncV4.proxy.rpc.enums.SystemAction;
import com.ford.syncV4.proxy.rpc.enums.TextAlignment;
import com.ford.syncV4.proxy.rpc.enums.TextFieldName;
import com.ford.syncV4.proxy.rpc.enums.VehicleDataEventStatus;
import com.ford.syncV4.transport.BTTransportConfig;
import com.ford.syncV4.transport.BaseTransportConfig;
import com.ford.syncV4.transport.TCPTransport;
import com.ford.syncV4.transport.TCPTransportConfig;
import com.ford.syncV4.util.DebugTool;

import com.kbb.livedrive.R;
import com.kbb.livedrive.app.LiveDriveApplication;
import com.kbb.livedrive.artifact.Location;
import com.kbb.livedrive.googleplay.GooglePlayService;
import com.kbb.livedrive.vehicledata.DriverScoreService;
import com.kbb.livedrive.vehicledata.IVehicleDataReceiver;
import com.kbb.livedrive.vehicledata.VehicleDataCache;
import com.kbb.livedrive.vehicledata.VehicleDataEmulatorService;

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

import com.google.android.gms.games.Games;
import com.google.android.gms.games.leaderboard.*;

public class AppLinkService extends Service implements IProxyListenerALM,
		IVehicleDataReceiver {

	private static final String ACTION_VEHICLE_DRIVING_CHANGED = "com.kbb.applink.AppLinkService.ACTION_VEHICLE_DRIVING_CHANGED";
	
	// Service shutdown timing constants
	private static final int CONNECTION_TIMEOUT = 30000;
	private static final int STOP_SERVICE_DELAY = 5000;

	private static final int VIEW_CURRENT_CONDITIONS = 1;
	private static final int VIEW_STANDARD_FORECAST = 2;
	private static final int VIEW_EXTENDED_FORECAST = 3;

	private static final int DRIVING_MODE_GOOD = 101;
	private static final int DRIVING_MODE_SPEEDING = 102;
	private static final int DRIVING_MODE_SLOW = 103;
	private static final int DRIVING_MODE_RECKLESS = 104;
	private static final int DRIVING_MODE_RACING = 105;

	private static final int SHOW_DRIVER_ID = 10000;
	private static final int SHOW_MPG_ID = 10010;
	private static final int SHOW_LEADERBOARD_ID = 10020;
	private static final int SHOW_RACING_ID = 10030;
	private static final int SHOW_SPLIT_ID = 10040;

	private static Object blah = new Object();
	private static int drivingMode = DRIVING_MODE_GOOD;

	private static String currentScoreDisplay = "76";

	private final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	private static final int CHANGE_UNITS = 4;

	private static final int NORMAL_LEAD_CHOICESET = 1;
	private static final int RACING_LEAD_CHOICESET = 2;

	private static final int NORMAL_ME_CHOICE = 1;
	private static final int NORMAL_FRIEND_1_CHOICE = 2;
	private static final int NORMAL_FRIEND_2_CHOICE = 3;
	private static final int NORMAL_FRIEND_3_CHOICE = 4;

	private static final int RACING_ME_CHOICE = 5;
	private static final int RACING_FRIEND_1_CHOICE = 6;
	private static final int RACING_FRIEND_2_CHOICE = 7;
	private static final int RACING_FRIEND_3_CHOICE = 8;

	private static final int LOG_INTERVAL = 5;

	// variable used to increment correlation ID for every request sent to SYNC
	public int autoIncCorrId = 0;
	// variable to contain the current state of the service
	private static AppLinkService instance = null;
	// variable to access the BluetoothAdapter
	private BluetoothAdapter btAdapter;
	// variable to create and call functions of the SyncProxy
	private SyncProxyALM proxy = null;
	private Handler handler = null;

	private Language currentSyncLanguage = null; // Stores the current language
													// of the SYNC module
	private Language currentHmiLanguage = null; // Stores the current language
												// of the display
	private boolean firstHmiNone = true;
	private DisplayType displayType = null; // Keeps track of the HMI display
											// type
	private boolean graphicsSupported = false; // Keeps track of whether
												// graphics are supported on the
												// display
	private int numberOfTextFields = 2;
	private int lengthOfTextFields = 40;
	private Vector<TextField> textFields = null; // Keeps track of the text
													// fields supported

	private Handler timedShowHandler = null;

	private Location currentLocation = null; // Stores the current location

	private boolean isEmulatorMode = true;
	private boolean isSimulatedData = false;

	private SoftButton showDriverScore = null;
	private SoftButton showMPGScore = null;
	private SoftButton showLeaderboard = null;
	private SoftButton showRacing = null;
	private SoftButton showSplit = null;

	private LiveDriveApplication app;

	private OnVehicleData prevVehicleData;
	private int prevDataLogSeconds;

	private boolean isMoving = false;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		handler = new Handler();

		app = LiveDriveApplication.getInstance();

		LocalBroadcastManager lbManager = LocalBroadcastManager
				.getInstance(this);
		lbManager.registerReceiver(changeLocationReceiver, new IntentFilter(
				"com.kbb.livedrive.Location"));
		lbManager.registerReceiver(forecastReceiver, new IntentFilter(
				"com.kbb.livedrive.Forecast"));

		showDriverScore = new SoftButton();
		showDriverScore.setSoftButtonID(SHOW_DRIVER_ID);
		showDriverScore.setText("Driver");
		showDriverScore.setType(SoftButtonType.SBT_TEXT);
		showDriverScore.setIsHighlighted(true);
		showDriverScore.setSystemAction(SystemAction.DEFAULT_ACTION);

		showMPGScore = new SoftButton();
		showMPGScore.setSoftButtonID(SHOW_MPG_ID);
		showMPGScore.setText("MPG");
		showMPGScore.setType(SoftButtonType.SBT_TEXT);
		showMPGScore.setIsHighlighted(true);
		showMPGScore.setSystemAction(SystemAction.DEFAULT_ACTION);

		showLeaderboard = new SoftButton();
		showLeaderboard.setSoftButtonID(SHOW_LEADERBOARD_ID);
		showLeaderboard.setType(SoftButtonType.SBT_TEXT);
		showLeaderboard.setIsHighlighted(true);
		showLeaderboard.setSystemAction(SystemAction.DEFAULT_ACTION);

		showRacing = new SoftButton();
		showRacing.setSoftButtonID(SHOW_RACING_ID);
		showRacing.setText("Race");
		showRacing.setType(SoftButtonType.SBT_TEXT);
		showRacing.setIsHighlighted(true);
		showRacing.setSystemAction(SystemAction.DEFAULT_ACTION);

		showSplit = new SoftButton();
		showSplit.setSoftButtonID(SHOW_SPLIT_ID);
		showSplit.setText("Split");
		showSplit.setType(SoftButtonType.SBT_TEXT);
		showSplit.setIsHighlighted(true);
		showSplit.setSystemAction(SystemAction.DEFAULT_ACTION);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Remove any previous stop service runnables that could be from a
		// recent ACL Disconnect
		handler.removeCallbacks(stopServiceRunnable);
		if (intent != null) {

			if (isEmulatorMode)
				startProxy();
			else {
				btAdapter = BluetoothAdapter.getDefaultAdapter();
				if (btAdapter != null) {
					if (btAdapter.isEnabled()) {
						startProxy();
					}
				}
			}
		}

		if (isSimulatedData) {
			Intent emulatorIntent = new Intent(this,
					VehicleDataEmulatorService.class);
			this.startService(emulatorIntent);
		}

		// Queue the check connection runnable to stop the service if no
		// connection is made
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
			LocalBroadcastManager lbManager = LocalBroadcastManager
					.getInstance(this);
			lbManager.unregisterReceiver(changeLocationReceiver);
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
	 * Receiver for changes in location from the app UI.
	 */
	protected final BroadcastReceiver changeLocationReceiver = new BroadcastReceiver() {

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
	 * Runnable that stops this service if there hasn't been a connection to
	 * SYNC within a reasonable amount of time since ACL_CONNECT.
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
	 * Runnable that stops this service on ACL_DISCONNECT after a short time
	 * delay. This is a workaround until some synchronization issues are fixed
	 * within the proxy.
	 */
	private Runnable stopServiceRunnable = new Runnable() {
		@Override
		public void run() {
			// As long as the proxy is null or not connected to SYNC, stop the
			// service
			if (proxy == null || !proxy.getIsConnected()) {
				handler.removeCallbacks(checkConnectionRunnable);
				handler.removeCallbacks(stopServiceRunnable);
				stopSelf();
			}
		}
	};

	/**
	 * Queue's a runnable that stops the service after a small delay, unless the
	 * proxy manages to reconnects to SYNC.
	 */
	public void stopService() {
		handler.removeCallbacks(stopServiceRunnable);
		handler.postDelayed(stopServiceRunnable, STOP_SERVICE_DELAY);
	}

	public void startProxy() {
		if (proxy == null) {
			try {

				BaseTransportConfig transport = null;
				
				if (isEmulatorMode)
					transport = new TCPTransportConfig(12345, "192.168.1.6", true);
				else
					transport = new BTTransportConfig();
				proxy = new SyncProxyALM(this, "Cox Automotive", false, Language.EN_US, Language.EN_US, "566020017", transport);
				
			} catch (SyncException e) {
				e.printStackTrace();
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
				// something goes wrong, & the proxy returns as null, stop the
				// service.
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

		if ((((SyncException) e).getSyncExceptionCause() != SyncExceptionCause.SYNC_PROXY_CYCLED)) {
			if (((SyncException) e).getSyncExceptionCause() != SyncExceptionCause.BLUETOOTH_DISABLED) {
				reset();
			}
		}
	}

	@Override
	public void onOnHMIStatus(OnHMIStatus notification) {
		switch (notification.getSystemContext()) {
		case SYSCTXT_MAIN:
			break;
		case SYSCTXT_VRSESSION:
			break;
		case SYSCTXT_MENU:
			break;
		// case SYSCTXT_HMI_OBSCURED:
		// break;
		default:
			return;
		}

		switch (notification.getAudioStreamingState()) {
		case AUDIBLE:
			break;
		case NOT_AUDIBLE:
			break;
		default:
			return;
		}

		//LockScreenManager.setHMILevelState(notification.getHmiLevel());
		//LockScreenManager.updateLockScreen();

		switch (notification.getHmiLevel()) {
		case HMI_FULL:
			if (notification.getFirstRun()) {
				// Perform welcome
				welcomeMessage();

				// Add commands
				addCommands();

				// Subscribe buttons
				subscribeButtons();

				subscribeVehicleData();

				launchWorker();

			}
			// If nothing is being displayed and we're in FULL, default to
			// current conditions

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

			Bitmap photo = BitmapFactory.decodeResource(getResources(),
					R.drawable.logo_ce);
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

	private void setDriverScoreDisplay(String score) {
		try {
			if (score == "Low" && currentScoreDisplay != "Low") {
				proxy.alert(
						"Your recent driving pattern lead to loosing 5 sponts on your Driving Score leaderboeard",
						true, autoIncCorrId++);
			}
			if (currentScoreDisplay == "Low" && score != "Low") {
				proxy.alert(
						"Your driving is improving! You gained 15 spots on you Driving Score eaderboard",
						true, autoIncCorrId++);
			}
		} catch (SyncException e) {
			Log.e("SyncException", e.getMessage());
		}

		currentScoreDisplay = score;
		updateDisplay("Driver Score", currentScoreDisplay);

		try {
			long numScore = Math.round(Double.parseDouble(score));
			GooglePlayService.getInstance().submitDriverScore(numScore);
		} catch (NumberFormatException ex) {
		}

	}

	private void launchWorker() {

		final Runnable scoreCalculator = new Runnable() {

			public void run() {

				int currentDrivingMode = 0;
				synchronized (blah) {
					currentDrivingMode = drivingMode;
				}

				if (currentDrivingMode != DRIVING_MODE_RACING) {
					calculateDriverScore();
				}
			}

		};

		scheduler.scheduleAtFixedRate(scoreCalculator, 30, 30, SECONDS);

		// scheduler.schedule(new Runnable() {
		// public void run() { handle.cancel(true); }
		// }, 60 * 60, SECONDS);

	};

	private void calculateDriverScore() {
		
		DriverScoreService.getInstance().calculateScores();

		String scoreDisplay = DriverScoreService.getInstance().getDriverScoreDisplay();

		setDriverScoreDisplay(scoreDisplay);

	}

	private void subscribeButtons() {
		try {
			proxy.subscribeButton(ButtonName.PRESET_1, autoIncCorrId++); // driving
																			// mode
																			// good
			proxy.subscribeButton(ButtonName.PRESET_2, autoIncCorrId++); // driving
																			// mode
																			// speeding
			proxy.subscribeButton(ButtonName.PRESET_3, autoIncCorrId++); // driving
																			// mode
																			// aggressive
			proxy.subscribeButton(ButtonName.PRESET_4, autoIncCorrId++); // diving
																			// mode
																			// slow
			proxy.subscribeButton(ButtonName.PRESET_5, autoIncCorrId++); // get
																			// Vehicle
																			// Data

			proxy.subscribeButton(ButtonName.PRESET_6, autoIncCorrId++); // get
																			// Vehicle
																			// Data
			proxy.subscribeButton(ButtonName.PRESET_7, autoIncCorrId++); // get
																			// Vehicle
																			// Data
			proxy.subscribeButton(ButtonName.PRESET_8, autoIncCorrId++); // get
																			// Vehicle
																			// Data
			proxy.subscribeButton(ButtonName.PRESET_9, autoIncCorrId++); // get
																			// Vehicle
																			// Data
			proxy.subscribeButton(ButtonName.PRESET_0, autoIncCorrId++); // get
																			// Vehicle
																			// Data

		} catch (SyncException e) {
			e.printStackTrace();
			DebugTool.logError("Failed to subscribe to buttons", e);
		}
	}

	private void subscribeVehicleData() {

		try {
			proxy.subscribevehicledata(true // gps,
					, true // speed,
					, true // rpm,
					, true // fuelLevel,
					, true // fuelLevel_State,
					, true // instantFuelConsumption,
					, true // externalTemperature,
					, true // prndl,
					, true // tirePressure,
					, true // odometer,
					, true // beltStatus,
					, true // bodyInformation,
					, false // deviceStatus,
					, false // driverBraking,
					, false // wiperStatus,
					, false // headLampStatus,
					, false // engineTorque,
					, false // accPedalPosition,
					, false // steeringWheelAngle,
					, false // eCallInfo,
					, false // airbagStatus,
					, false // emergencyEvent,
					, false // clusterModeStatus,
					, false // myKey,
					, autoIncCorrId++);
		} catch (SyncException e) {
			Log.e("syncexception", e.getMessage());
		}
	}

	private void getSyncSettings() {
		try {
			// Change registration to match the language of the head unit
			currentHmiLanguage = proxy.getHmiDisplayLanguage();
			currentSyncLanguage = proxy.getSyncLanguage();
			if (currentHmiLanguage != null && currentSyncLanguage != null) {
				proxy.changeregistration(currentSyncLanguage,
						currentHmiLanguage, autoIncCorrId++);
			}
		} catch (SyncException e) {
			DebugTool.logError("Failed to change language", e);
		}

		try {
			// Get the display capabilities
			DisplayCapabilities displayCapabilities = proxy
					.getDisplayCapabilities();
			if (displayCapabilities != null) {
				displayType = displayCapabilities.getDisplayType();
				graphicsSupported = displayCapabilities.getGraphicSupported();
				textFields = displayCapabilities.getTextFields();

				if (displayType == DisplayType.CID) {
					numberOfTextFields = 1;
				} else if (displayType == DisplayType.MFD3
						|| displayType == DisplayType.MFD4
						|| displayType == DisplayType.MFD5) {
					numberOfTextFields = 2;
				} else {
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

		Choice leadNormalMeChoice = new Choice();
		leadNormalMeChoice.setChoiceID(NORMAL_ME_CHOICE);
		leadNormalMeChoice.setMenuName("Me");
		leadNormalMeChoice
				.setVrCommands(new Vector<String>(Arrays.asList("Me")));

		Choice leadNormalFriend1Choice = new Choice();
		leadNormalFriend1Choice.setChoiceID(NORMAL_FRIEND_1_CHOICE);
		leadNormalFriend1Choice.setMenuName("CaDanceMom1974");
		leadNormalFriend1Choice.setVrCommands(new Vector<String>(Arrays
				.asList("CaDanceMom1974")));

		Choice leadNormalFriend2Choice = new Choice();
		leadNormalFriend2Choice.setChoiceID(NORMAL_FRIEND_2_CHOICE);
		leadNormalFriend2Choice.setMenuName("TwistTurny44");
		leadNormalFriend2Choice.setVrCommands(new Vector<String>(Arrays
				.asList("TwistTurny44")));

		Choice leadNormalFriend3Choice = new Choice();
		leadNormalFriend3Choice.setChoiceID(NORMAL_FRIEND_3_CHOICE);
		leadNormalFriend3Choice.setMenuName("KevinMevinLevin");
		leadNormalFriend3Choice.setVrCommands(new Vector<String>(Arrays
				.asList("KevinMevinLevin")));

		Choice leadRaceMeChoice = new Choice();
		leadRaceMeChoice.setChoiceID(RACING_ME_CHOICE);
		leadRaceMeChoice.setMenuName("Me");
		leadRaceMeChoice.setVrCommands(new Vector<String>(Arrays.asList("Me")));

		Choice leadRacelFriend1Choice = new Choice();
		leadRacelFriend1Choice.setChoiceID(RACING_FRIEND_1_CHOICE);
		leadRacelFriend1Choice.setMenuName("FastRocketChick");
		leadRacelFriend1Choice.setVrCommands(new Vector<String>(Arrays
				.asList("CaDanceMom1974")));

		Choice leadRaceFriend2Choice = new Choice();
		leadRaceFriend2Choice.setChoiceID(RACING_FRIEND_2_CHOICE);
		leadRaceFriend2Choice.setMenuName("FatSlogJeff");
		leadRaceFriend2Choice.setVrCommands(new Vector<String>(Arrays
				.asList("TwistTurny44")));

		Choice leadRaceFriend3Choice = new Choice();
		leadRaceFriend3Choice.setChoiceID(RACING_FRIEND_3_CHOICE);
		leadRaceFriend3Choice.setMenuName("Coolio7767");
		leadRaceFriend3Choice.setVrCommands(new Vector<String>(Arrays
				.asList("KevinMevinLevin")));

		try {
			proxy.createInteractionChoiceSet(
					new Vector<Choice>(Arrays.asList(leadNormalMeChoice,
							leadNormalFriend1Choice, leadNormalFriend2Choice,
							leadNormalFriend3Choice)), NORMAL_LEAD_CHOICESET,
					autoIncCorrId++);
			proxy.createInteractionChoiceSet(
					new Vector<Choice>(Arrays.asList(leadRaceMeChoice,
							leadRacelFriend1Choice, leadRaceFriend2Choice,
							leadRaceFriend3Choice)), RACING_LEAD_CHOICESET,
					autoIncCorrId++);
		} catch (SyncException e) {
			Log.e("SyncException", e.getMessage());
		}

	}

	/**
	 * Shows and speaks a welcome message
	 */
	private void welcomeMessage() {

		updateDisplay("", "LIVE Drive");

		setNormalModeButtons();

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void setNormalModeButtons() {
		Vector<SoftButton> currentSoftButtons = new Vector<SoftButton>();
		// choose the order softbuttons appear
		currentSoftButtons.add(showDriverScore);
		currentSoftButtons.add(showMPGScore);
		currentSoftButtons.add(showLeaderboard);

		Show msg = new Show();
		msg.setCorrelationID(autoIncCorrId++);
		msg.setSoftButtons(currentSoftButtons);

		try {
			proxy.sendRPCRequest(msg);
		} catch (SyncException e) {
			Log.e("SyncException",
					"sync exception" + e.getMessage()
							+ e.getSyncExceptionCause());
			e.printStackTrace();
		}
	}

	private void setRacingModeButtons() {
		Vector<SoftButton> currentSoftButtons = new Vector<SoftButton>();
		// choose the order softbuttons appear
		currentSoftButtons.add(showRacing);
		currentSoftButtons.add(showSplit);
		currentSoftButtons.add(showLeaderboard);

		Show msg = new Show();
		msg.setCorrelationID(autoIncCorrId++);
		msg.setSoftButtons(currentSoftButtons);

		try {
			proxy.sendRPCRequest(msg);
		} catch (SyncException e) {
			Log.e("SyncException",
					"sync exception" + e.getMessage()
							+ e.getSyncExceptionCause());
			e.printStackTrace();
		}
	}

	private void say(String tts) {
		try {
			// String sample =
			// "Your Driving Behavior in the past 5 minutes caused you to lose 17 point on a good driver score. Consider driving slower.";
			proxy.speak(tts, autoIncCorrId++);
		} catch (SyncException e) {
			e.printStackTrace();
		}
	}

	private void updateDisplay(String s1, String s2) {
		try {
			proxy.show(s1, s2, TextAlignment.CENTERED, autoIncCorrId++);
		} catch (SyncException e) {
			e.printStackTrace();
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
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void onOnButtonPress(OnButtonPress notification) {

		String display = "";
		switch (notification.getButtonName()) {
		case CUSTOM_BUTTON:
			switch (notification.getCustomButtonName()) {
			case SHOW_DRIVER_ID:
				display = DriverScoreService.getInstance().getDriverScoreDisplay();
				
				updateDisplay("Your Driving Score is", display);
				say("Your Driving Score is " + display);
				break;
			case SHOW_MPG_ID:
				display = DriverScoreService.getInstance().getMPGScoreDisplay();
				updateDisplay("Your MPG Score is", display);
				say("Your MPG Score is " + display);
				break;
			// case SHOW_LEADERBOARD_ID:
			// display = DriverScoreService.getLeaderboard();
			// try {
			// int currDrivingMode = 0;
			// synchronized (blah) {
			// currDrivingMode = drivingMode;
			// }
			//
			// if(currDrivingMode != DRIVING_MODE_RACING)
			// proxy.performInteraction("Chose a friend to check Normal Leaderboard",
			// "Leaderboard", NORMAL_LEAD_CHOICESET, autoIncCorrId++);
			// else
			// proxy.performInteraction("Chose a friend to check Racing Leaderboard",
			// "Leaderboard", RACING_LEAD_CHOICESET, autoIncCorrId++);
			// } catch (SyncException e) {
			// e.printStackTrace();
			// DebugTool.logError("Failed to perform interaction", e);
			// }
			//
			// break;
			default:
				break;
			}
			break;
		case PRESET_1:
			synchronized (blah) {
				drivingMode = DRIVING_MODE_GOOD;
			}

			updateDisplay("Driving Mode", "Good Driver");
			break;
		case PRESET_2:
			synchronized (blah) {
				drivingMode = DRIVING_MODE_SPEEDING;
			}

			updateDisplay("Driving Mode", "Speeding");
			break;
		case PRESET_3:
			synchronized (blah) {
				drivingMode = DRIVING_MODE_RECKLESS;
			}

			updateDisplay("Driving Mode", "Reckless");
			break;
		case PRESET_4:
			synchronized (blah) {
				drivingMode = DRIVING_MODE_SLOW;
			}

			updateDisplay("Driving Mode", "Slow");
			break;

		case PRESET_5:
			GetVehicleData msg = new GetVehicleData();
			msg.setCorrelationID(autoIncCorrId++);

			// Location functional group
			msg.setSpeed(true);
			msg.setGps(true);

			// VechicleInfo functional group
			msg.setFuelLevel(true);
			msg.setFuelLevel_State(true);
			msg.setInstantFuelConsumption(true);
			msg.setExternalTemperature(true);
			msg.setTirePressure(true);
			msg.setOdometer(true);
			msg.setVin(true);

			// DrivingCharacteristics functional group
			msg.setBeltStatus(false);
			msg.setDriverBraking(false);
			msg.setPrndl(true);
			msg.setRpm(true);

			try {
				proxy.sendRPCRequest(msg);
			} catch (SyncException e) {
				Log.e("shit", "get Vehicle Data no woikie");
			}
			break;
		case PRESET_6: // fake GoodDriving score change
			fakeDrivingScore(77.8);
			break;
		case PRESET_7:
			fakeDrivingScore(65.5);
			break;
		case PRESET_8:
			fakeDrivingScore(42.3);
			break;
		case PRESET_9:
			break;
		case PRESET_0:
			setDrivingModeRacing();
			break;

		default:
			break;
		}
	}

	private void setDrivingModeRacing() {

		int currDrivingMode = 0;

		synchronized (blah) {
			currDrivingMode = drivingMode;
		}

		if (currDrivingMode == DRIVING_MODE_RACING) {
			setNormalModeButtons();

			drivingMode = DRIVING_MODE_GOOD;
			updateDisplay("Driving Mode", "Good");
		} else {
			setRacingModeButtons();

			drivingMode = DRIVING_MODE_RACING;
			updateDisplay("Driving Mode", "Racing");
		}
	}

	private void fakeDrivingScore(double score) {
		DriverScoreService.getInstance().fakeDriverScore(score);
		
		String scoreDisplay = DriverScoreService.getInstance().getDriverScoreDisplay();

		setDriverScoreDisplay(scoreDisplay);
	}

	@Override
	public void onPerformInteractionResponse(PerformInteractionResponse response) {
		if (response.getSuccess()) {
			Integer choiceID = response.getChoiceID();
			String display = "";
			switch (choiceID) {
			case NORMAL_ME_CHOICE:
				// display = DriverScoreService.getInstance().getLeaderboard();
				say("Leaderboard. " + display);
				break;
			case NORMAL_FRIEND_1_CHOICE:
				// display = DriverScoreService.getLeaderboard();
				say("Leaderboard. "
						+ "CaDanceMom1974 is 10 points ahead of you");
				break;
			case NORMAL_FRIEND_2_CHOICE:
				break;
			case NORMAL_FRIEND_3_CHOICE:
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
	public void onCreateInteractionChoiceSetResponse(
			CreateInteractionChoiceSetResponse response) {
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
	public void onDeleteInteractionChoiceSetResponse(
			DeleteInteractionChoiceSetResponse response) {
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
	public void onOnVehicleData(OnVehicleData vehicleData) {
		// updateDisplay("getVehicleData change",
		// notification.getSpeed().toString());
		// Log.i("onVehicleData", "Speed: " + vehicleData.getSpeed().toString()
		// + " rpm: " + vehicleData.getRpm().toString() );

		boolean stateChange = false;

		if (prevVehicleData != null) {
			if (vehicleData.getAirbagStatus() != prevVehicleData.getAirbagStatus()
					|| vehicleData.getBeltStatus() != prevVehicleData.getBeltStatus()
					//|| vehicleData.getBodyInformation() != prevVehicleData.getBodyInformation()
					|| vehicleData.getClusterModeStatus() != prevVehicleData.getClusterModeStatus()
					|| vehicleData.getDeviceStatus() != prevVehicleData.getDeviceStatus()
					|| vehicleData.getDriverBraking() != prevVehicleData.getDriverBraking()
					|| vehicleData.getECallInfo() != prevVehicleData.getECallInfo()
					|| vehicleData.getEmergencyEvent() != prevVehicleData.getEmergencyEvent()
					|| vehicleData.getFuelLevel_State() != prevVehicleData.getFuelLevel_State()
					|| vehicleData.getHeadLampStatus() != prevVehicleData.getHeadLampStatus()
					|| vehicleData.getPrndl() != prevVehicleData.getPrndl()
					|| vehicleData.getTirePressure() != prevVehicleData.getTirePressure()
					|| vehicleData.getWiperStatus() != prevVehicleData.getWiperStatus()) {

				
				onVehicleDataStateChange();
				stateChange = true;
			}

			if (vehicleData.getPrndl() == PRNDL.PARK
					&& prevVehicleData.getPrndl() != PRNDL.PARK){
			
				isMoving = false;
				onParked(vehicleData);
			}

			if (vehicleData.getPrndl() != PRNDL.PARK
					&& prevVehicleData.getPrndl() == PRNDL.PARK){
				
				isMoving = true;
				onStartDriving(vehicleData);
			}
		}

		prevVehicleData = vehicleData;

		if (stateChange
				|| (isMoving && (Math.abs(vehicleData.getGps().getUtcSeconds() - prevDataLogSeconds) >= LOG_INTERVAL))) {
			
			prevDataLogSeconds = vehicleData.getGps().getUtcSeconds();
			DriverScoreService.getInstance().addVehicleData(vehicleData);
			
		}

	}

	private void onVehicleDataStateChange() {
		// TODO Auto-generated method stub

	}

	private void onStartDriving(OnVehicleData vehicleData) {

		Intent intent = new Intent(ACTION_VEHICLE_DRIVING_CHANGED);
		intent.putExtra("drivingState", "DRIVING");
		intent.putExtra("odometer", vehicleData.getOdometer());
		sendBroadcast(intent);

		//DriverScoreService.getInstance().startTrip(vehicleData);

	}

	private void onParked(OnVehicleData vehicleData) {
		
		Intent intent = new Intent(ACTION_VEHICLE_DRIVING_CHANGED);
		intent.putExtra("drivingState", "PARKED");
		intent.putExtra("odometer", vehicleData.getOdometer());
		sendBroadcast(intent);
		
		//DriverScoreService.getInstance().endTrip(vehicleData);

	}

	@Override
	public void onResetGlobalPropertiesResponse(
			ResetGlobalPropertiesResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSetGlobalPropertiesResponse(
			SetGlobalPropertiesResponse response) {
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
	public void onSubscribeVehicleDataResponse(
			SubscribeVehicleDataResponse response) {
		Log.i("SubscribeVehicleData", response.getResultCode().toString()
				+ " - " + response.getInfo());
	}

	@Override
	public void onUnsubscribeVehicleDataResponse(
			UnsubscribeVehicleDataResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onGetVehicleDataResponse(GetVehicleDataResponse response) {
		// updateDisplay("getVehicleData callback",
		// response.getSpeed().toString());
		Log.i("GetVehicleData", response.getResultCode().toString() + " - "
				+ response.getInfo());
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
	public void onPerformAudioPassThruResponse(
			PerformAudioPassThruResponse response) {
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

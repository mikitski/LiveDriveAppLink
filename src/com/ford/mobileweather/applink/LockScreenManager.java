package com.ford.mobileweather.applink;

import android.content.Intent;

import com.ford.mobileweather.activity.ActivityBase;
import com.ford.mobileweather.activity.LockScreenActivity;
import com.ford.mobileweather.app.LiveDriveApplication;
import com.ford.syncV4.proxy.rpc.enums.DriverDistractionState;
import com.ford.syncV4.proxy.rpc.enums.HMILevel;

/**
 * This class manages the lockscreen for the app.
 * With AppLink SDK versions <= 2.2, this is required.
 * In later versions, a callback from the proxy will
 * notify the app when a lockscreen should be displayed.
 */
public class LockScreenManager {
	// Variable that keeps track of whether SYNC is sending driver distractions
	// (older versions of SYNC will not send this notification)
	private static Boolean driverDistrationStatus = null;
	private static HMILevel currentHMILevel = null;
	private static boolean userSelected = false;
	// variable to contain the current state of the lockscreen
	private static boolean lockScreenUp = false;

	public static synchronized void setHMILevelState(HMILevel state) {
		currentHMILevel = state;
	}

	public static synchronized void setDriverDistractionState(DriverDistractionState state) {
		if (state == DriverDistractionState.DD_OFF) {
			driverDistrationStatus = false;
		} else {
			driverDistrationStatus = true;
		}
	}

	private static synchronized LockScreenStatus checkLockScreen() {
		// Default to HIDDEN
		LockScreenStatus displayLockScreen = LockScreenStatus.OFF;

		// Abort if we don't know the hmi level
		if (currentHMILevel == null) {
			return displayLockScreen;
		}

		// Check if the user has selected the app in mobile apps menu
		if (currentHMILevel.equals(HMILevel.HMI_FULL) || currentHMILevel.equals(HMILevel.HMI_LIMITED)) {
			userSelected = true;
		}
		else if (currentHMILevel.equals(HMILevel.HMI_NONE)) {
			userSelected = false;
		}

		// Determine the lockscreen state
		if (currentHMILevel.equals(HMILevel.HMI_BACKGROUND) && userSelected) {
			if (driverDistrationStatus == null || driverDistrationStatus) {
				displayLockScreen = LockScreenStatus.REQUIRED;
			}
			else {
				displayLockScreen = LockScreenStatus.OPTIONAL;
			}
		}
		else if (currentHMILevel.equals(HMILevel.HMI_FULL) || currentHMILevel.equals(HMILevel.HMI_LIMITED)) {
			if (driverDistrationStatus == null || driverDistrationStatus) {
				displayLockScreen = LockScreenStatus.REQUIRED;
			}
			else {
				displayLockScreen = LockScreenStatus.OPTIONAL;
			}
		}

		return displayLockScreen;
	}

	public static synchronized void showLockScreen() {
		// only show the lockscreen if main activity is currently on top
		// else, wait until onResume() to show the lockscreen so it doesn't
		// pop-up while a user is using another app on the phone
		if (LiveDriveApplication.getCurrentActivity() != null) {
			if (((ActivityBase) LiveDriveApplication.getCurrentActivity()).isActivityonTop() == true) {
				Intent i = new Intent(LiveDriveApplication.getInstance(), LockScreenActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				LiveDriveApplication.getInstance().startActivity(i);
			}
		}
		lockScreenUp = true;
	}

	public static synchronized void clearLockScreen() {
		if (LockScreenActivity.getInstance() != null) {
			LockScreenActivity.getInstance().exit();
		}
		lockScreenUp = false;
	}

	public static synchronized boolean getLockScreenStatus() {
		return lockScreenUp;
	}

	public static synchronized void updateLockScreen() {
		LockScreenStatus displayLockScreen = checkLockScreen();
		// Show lockscreen in both REQUIRED and OPTIONAL
		if (displayLockScreen == LockScreenStatus.REQUIRED || displayLockScreen == LockScreenStatus.OPTIONAL) {
			showLockScreen();
		}
		else {
			clearLockScreen();
		}
	}
}

package com.ford.mobileweather.applink;

import com.ford.mobileweather.app.MobileWeatherApplication;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppLinkReceiver extends BroadcastReceiver {	
	public void onReceive(Context context, Intent intent) {
		// Start the AppLinkService on BT connection
		if (intent.getAction().compareTo(BluetoothDevice.ACTION_ACL_CONNECTED) == 0) {
			MobileWeatherApplication app = MobileWeatherApplication.getInstance();
			if (app != null) {
				app.startServices();
			}
		}
		// Stop the AppLinkService on BT disconnection
		else if (intent.getAction().compareTo(BluetoothDevice.ACTION_ACL_DISCONNECTED) == 0) {
			MobileWeatherApplication app = MobileWeatherApplication.getInstance();
			if (app != null) {
				AppLinkService als = AppLinkService.getInstance();
				if (als != null) {
					app.endSyncProxyService();
				}
			}
		}
	}
}

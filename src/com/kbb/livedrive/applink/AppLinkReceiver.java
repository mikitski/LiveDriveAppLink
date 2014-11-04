package com.kbb.livedrive.applink;

import com.kbb.livedrive.app.LiveDriveApplication;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppLinkReceiver extends BroadcastReceiver {	
	public void onReceive(Context context, Intent intent) {
		// Start the AppLinkService on BT connection
		if (intent.getAction().compareTo(BluetoothDevice.ACTION_ACL_CONNECTED) == 0) {
			LiveDriveApplication app = LiveDriveApplication.getInstance();
			if (app != null) {
				app.startServices();
			}
		}
		// Stop the AppLinkService on BT disconnection
		else if (intent.getAction().compareTo(BluetoothDevice.ACTION_ACL_DISCONNECTED) == 0) {
			LiveDriveApplication app = LiveDriveApplication.getInstance();
			if (app != null) {
				AppLinkService als = AppLinkService.getInstance();
				if (als != null) {
					app.endSyncProxyService();
				}
			}
		}
	}
}

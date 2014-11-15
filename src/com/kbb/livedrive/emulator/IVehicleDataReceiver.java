package com.kbb.livedrive.emulator;

import com.ford.syncV4.proxy.rpc.OnVehicleData;

public interface IVehicleDataReceiver {
	void onOnVehicleData(OnVehicleData vehicleData);
}

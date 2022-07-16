package de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class BLEServiceInstance extends Application {
    private static BLEService bleService;

    // Service Connection ----------------------------
    public static ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (bleService == null) BLEServiceInstance.setBLEService(((BLEService.LocalBinder) service).getService());
            Log.i("testbt", "SERVICE connected!");
            if (bleService != null) {
                // call functions on service to check connection and connect to devices
                if (!bleService.initialize()) {
                    Log.e("testbt", "Unable to initialize Bluetooth");
                    // todo brauchen wir the following statement
                    // finish();
                }
                // perform device connection
                for(int i = 0; i < Devices.getDeviceCount(); i++) {
                    String mac = Devices.getMacAsString(i);
                    if (bleService.getGattByMAC(mac) == null) {
                        if (bleService.connect(mac)) {
                            Log.i("testbt", mac + " connected!");
                        }
                    }
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("testbt", "SERVICE disconnected!");
            BLEServiceInstance.setBLEService(null);
        }
    };

    public static void setBLEService(BLEService service){
        bleService = service;
    }

    public static BLEService getBLEService(){
        return bleService;
    }

}

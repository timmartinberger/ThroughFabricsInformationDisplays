package de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

import de.uni_hannover.hci.informationalDisplaysControl.baseData.Symbol;

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

    public static void deleteService() {
        bleService = null;
    }

    public static void setControllerOptions(ArrayList<String> deviceMacList, String mode, boolean enableButton) {
        for(String device: deviceMacList) {
            if(mode != null && !mode.isEmpty()) {
                BLEServiceInstance.getBLEService().writeCharacteristic(device, BLEService.MODE_CHARACTERISTIC_UUID, mode);
            }
            BLEServiceInstance.getBLEService().setCharacteristicNotification(device, BLEService.BUTTON_CHARACTERISTIC_UUID, enableButton);
        }
    }

    public static void sendSymbolsToPlayers(ArrayList<ArrayList<Symbol>> playersSymbols, ArrayList<String> deviceMacList) {
        int playerNr = 0;
        for(ArrayList<Symbol> list : playersSymbols) {
            try {
                byte[] data = getSymbolBytes(list);
                BLEServiceInstance.getBLEService().writeCharacteristic(deviceMacList.get(playerNr), BLEService.DATA_CHARACTERISTIC_UUID, data);
            } catch (Exception e) {
                Log.i("testbt", "Failed sending data");
            }
            playerNr++;
        }
    }

    private static byte[] getSymbolBytes(ArrayList<Symbol> symbols) {
        byte[] data = new byte[symbols.size()];
        int counter = 0;
        for(Symbol symbol: symbols) {
            data[counter] = (byte)((symbol.getCode()+1) & 0xFF);
            counter++;
        }
        return data;
    }

    public static void sendTurnCodeToPlayers(int code, ArrayList<String> deviceMacList) {
        byte[] data = {(byte)((code+1) & 0xFF)};
        for(String address : deviceMacList) {
            try {
                BLEServiceInstance.getBLEService().writeCharacteristic(address, BLEService.DATA_CHARACTERISTIC_UUID, data);
            } catch (Exception e) {
                System.out.println("Failed sending!");
            }
        }
    }

    public static void sendPlayerPoints(String address, int points, int colorCode) {
        byte[] data = {(byte)((points+1) & 0xFF), (byte)((colorCode+1) & 0xFF)};
        BLEServiceInstance.getBLEService().writeCharacteristic(address, BLEService.MODE_CHARACTERISTIC_UUID, "7");
        try {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.i("testbt", "Interrupted while sleeping");
            }
            BLEServiceInstance.getBLEService().writeCharacteristic(address, BLEService.DATA_CHARACTERISTIC_UUID, data);
        } catch (Exception e) {
            Log.i("testbt", "Failed sending data");
        }
    }
}

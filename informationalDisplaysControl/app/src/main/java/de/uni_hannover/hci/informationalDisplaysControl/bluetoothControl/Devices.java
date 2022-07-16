package de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl;
import android.net.MacAddress;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;


public class Devices {
    private static ArrayList<String> deviceNames;
    private static ArrayList<MacAddress> deviceAddress;
    private static int countDevices = 0;




    public static void addDevice(String name, MacAddress address) throws Exception {
        if (deviceNames == null) {
            deviceNames = new ArrayList<String>();
            deviceAddress = new ArrayList<MacAddress>();
        }
        if(deviceAddress.contains(address)){
            throw new Exception("Device " + address.toString() + " is already in device list!");
        } else {
            deviceNames.add(name);
            deviceAddress.add(address);
            countDevices++;
        }
    }

    public static int getDeviceCount(){
        return countDevices;
    }

    public static String getMacAsString(int index){
        return deviceAddress.get(index).toString().toUpperCase();
    }

    public static void removeDevice(MacAddress address){
        int idx = deviceAddress.indexOf(address);
        deviceNames.remove(idx);
        deviceAddress.remove(idx);
        countDevices--;
    }
}

package de.uni_hannover.hci.informationalDisplaysControl;
import android.net.MacAddress;

import java.util.ArrayList;

import javax.crypto.Mac;

public class Devices {
    public static ArrayList<String> deviceNames;
    public static ArrayList<MacAddress> deviceAddress;

    public static void addDevice(String name, MacAddress address) throws Exception {
        if(deviceAddress.contains(address)){
            throw new Exception("Device " + address.toString() + " is aready in device list!");
        } else {
            deviceNames.add(name);
            deviceAddress.add(address);
        }
    }

    public static void removeDevice(MacAddress address){
        int idx = deviceAddress.indexOf(address);
        deviceNames.remove(idx);
        deviceAddress.remove(idx);

    }
}

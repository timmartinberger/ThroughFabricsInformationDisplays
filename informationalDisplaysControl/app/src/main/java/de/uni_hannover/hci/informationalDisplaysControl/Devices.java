package de.uni_hannover.hci.informationalDisplaysControl;
import android.net.MacAddress;
import java.util.ArrayList;


public class Devices {
    public static ArrayList<String> deviceNames;
    public static ArrayList<MacAddress> deviceAddress;

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
        }
    }

    public static String getMacAsString(int index){
        return deviceAddress.get(index).toString().toUpperCase();
    }

    public static void removeDevice(MacAddress address){
        int idx = deviceAddress.indexOf(address);
        deviceNames.remove(idx);
        deviceAddress.remove(idx);

    }
}

package de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BLEService extends Service {

    private Binder binder = new LocalBinder();
    private BluetoothAdapter bluetoothAdapter;

    public ArrayList<BluetoothGatt> bluetoothGatts;
    private ArrayList<byte[]> values;

    // connection state
    public final static String ACTION_GATT_CONNECTED = "GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "DATA_AVAILABLE";

    public final static String SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
    public final static String MODE_CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8";
    public final static String DATA_CHARACTERISTIC_UUID = "84e3a48f-7172-4952-8e59-64af6ce20583";
    public final static String BUTTON_CHARACTERISTIC_UUID = "07bf0001-7a36-490f-ba53-345b3642a694";


    // Callback, um Verbindung zu überwachen -------------------------------------------------------
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @RequiresApi(api = Build.VERSION_CODES.P)
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("testbt", "CONNECTION_STATE_CHANGE: state:"+ newState + " status:" + status);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                broadcastUpdate(ACTION_GATT_CONNECTED, gatt);
                // discover services after connection
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                broadcastUpdate(ACTION_GATT_DISCONNECTED, gatt);
                disconnect(gatt);
                close(gatt);
                Devices.removeDevice(gatt.getDevice().getAddress());
            }
        }

        // Send broadcast, when Services discovered
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, gatt);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                Log.i("testbt", "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, gatt, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, gatt, characteristic);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("testbt", "onCharacteristicWrite: " + Arrays.toString(characteristic.getValue()));

            byte[] last = values.get(bluetoothGatts.indexOf(gatt));
            if (!Arrays.equals(last, characteristic.getValue())) {
                writeCharacteristic(gatt.getDevice().getAddress(), characteristic.getUuid().toString(), last);
            }
        }

    };

    // Service initialisieren ----------------------------------------------------------------------
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public BLEService getService() {
            return BLEService.this;
        }
    }


    public boolean initialize() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e("testbt", "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    // Verbindungsaufbau ---------------------------------------------------------------------------
    @SuppressLint("MissingPermission")
    public boolean connect(final String address) {
        if (bluetoothAdapter == null || address == null) {
            Log.w("testbt", "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        try {
            Log.i("testbt", "Device address: " + address);
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            BluetoothGatt bluetoothGatt = device.connectGatt(this, true, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
            if (bluetoothGatts == null){
                bluetoothGatts = new ArrayList<BluetoothGatt>();
                values = new ArrayList<byte[]>();
            }
            bluetoothGatts.add(bluetoothGatt);
            byte[] b = {};
            values.add(b);
            return true;
        } catch (IllegalArgumentException exception) {
            Log.w("testbt", "Device not found with provided address.  Unable to connect.");
            return false;
        }
    }

    // BLE-Service - Services und Characteristics suchen und kommunizieren -------------------------
    @SuppressLint("MissingPermission")
    public List<BluetoothGattService> getSupportedGattServices(BluetoothGatt bluetoothGatt) {
        if (bluetoothGatt == null) return null;
        return bluetoothGatt.getServices();
    }


    // write string value
    @SuppressLint("MissingPermission")
    public void writeCharacteristic(String mac, String characteristicUUID, String data) {
        BluetoothGatt gatt = getGattByMAC(mac);
        if (bluetoothAdapter == null || gatt == null) {
            Log.w("testbt", "BluetoothAdapter not initialized");
            return;
        }

        byte[] value = data.getBytes(StandardCharsets.UTF_8);
        BluetoothGattCharacteristic characteristic = gatt.getService(UUID.fromString(SERVICE_UUID)).getCharacteristic(UUID.fromString(characteristicUUID));
        characteristic.setValue(value);

        values.set(bluetoothGatts.indexOf(gatt), value);
        gatt.writeCharacteristic(characteristic);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    public void writeCharacteristicToAll(String characteristicUUID, String data) {
        if (bluetoothGatts == null || bluetoothGatts.size() == 0){
            Log.w("testbt", "GATTs not initialized");
            return;
        }
        for (BluetoothGatt gatt: bluetoothGatts) {
            if (bluetoothAdapter == null || gatt == null) {
                Log.w("testbt", "BluetoothAdapter not initialized");
                return;
            }

            byte[] value = data.getBytes(StandardCharsets.UTF_8);
            values.set(bluetoothGatts.indexOf(gatt), value);
            BluetoothGattCharacteristic characteristic = gatt.getService(UUID.fromString(SERVICE_UUID)).getCharacteristic(UUID.fromString(characteristicUUID));
            characteristic.setValue(value);
            gatt.writeCharacteristic(characteristic);
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // write byte value
    @SuppressLint("MissingPermission")
    public void writeCharacteristic(String mac, String characteristicUUID, byte[] data) {
        BluetoothGatt gatt = getGattByMAC(mac);
        if (bluetoothAdapter == null || gatt == null) {
            Log.w("testbt", "BluetoothAdapter not initialized");
            return;
        }

        BluetoothGattCharacteristic characteristic = gatt.getService(UUID.fromString(SERVICE_UUID)).getCharacteristic(UUID.fromString(characteristicUUID));
        characteristic.setValue(data);
        Log.i("testbt", "writeByteArray: " + data.length + " " +  data.toString());

        values.set(bluetoothGatts.indexOf(gatt), data);
        gatt.writeCharacteristic(characteristic);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // setup notification
    @SuppressLint("MissingPermission")
    public void setCharacteristicNotification(String mac, String characteristicUUID, boolean enabled) {
        BluetoothGatt gatt = getGattByMAC(mac);
        if (gatt == null) {
            Log.w("testbt", "BluetoothGatt not initialized");
            return;
        }
        BluetoothGattCharacteristic characteristic = gatt.getService(UUID.fromString(SERVICE_UUID)).getCharacteristic(UUID.fromString(characteristicUUID));
        gatt.setCharacteristicNotification(characteristic, enabled);

//        // This is specific to Heart Rate Measurement.
//        if (MODE_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
//            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            gatt.writeDescriptor(descriptor);
//        }
    }

    // Service/BLE-Verbindung beenden --------------------------------------------------------------
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void closeAll(){
        if (bluetoothGatts == null) return;
        for(BluetoothGatt gatt: bluetoothGatts) disconnect(gatt);
        for(BluetoothGatt gatt: bluetoothGatts) close(gatt);
    }

    @SuppressLint("MissingPermission")
    public void disconnect(BluetoothGatt gatt) {
        if (bluetoothAdapter == null || gatt == null) {
            Log.w("testbt", "BluetoothAdapter not initialized");
            return;
        }
        gatt.disconnect();
    }

    @SuppressLint("MissingPermission")
    public void close(BluetoothGatt gatt) {
        if (gatt == null) {
            return;
        }
        gatt.close();
        values.remove(bluetoothGatts.indexOf(gatt));
        bluetoothGatts.remove(gatt);
    }

    // Activities benachrichtigen ------------------------------------------------------------------
    private void broadcastUpdate(final String action, BluetoothGatt gatt) {
        final Intent intent = new Intent(action);
        intent.putExtra("ADDRESS", gatt.getDevice().getAddress());
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        intent.putExtra("ADDRESS", gatt.getDevice().getAddress());
        // If data ist from mode characteristic
        if (MODE_CHARACTERISTIC_UUID.equals(characteristic.getUuid().toString())) {
            final String mode = characteristic.getStringValue(0);
            Log.i("testbt", String.format("MODE: %s", mode));
            intent.putExtra("MODE_DATA", mode);
        }
        // Data from data characteristic
        else if (DATA_CHARACTERISTIC_UUID.equals((characteristic.getUuid().toString()))){

        }
        // If data is from button characteristic
        else if (BUTTON_CHARACTERISTIC_UUID.equals(characteristic.getUuid().toString())) {
            final String data = characteristic.getStringValue(0);
            if (data.equals("1")) {
                intent.putExtra("BUTTON_DATA", "PRESSED");
                Log.i("testbt", "BUTTON PRESSED!");
            }
            else {
                intent.putExtra("BUTTON_DATA", "NOT_PRESSED");
                Log.i("testbt", "BUTTON NOT PRESSED!");
            }
        }
        sendBroadcast(intent);
    }

    public static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    // Getter for gatts
    public BluetoothGatt getGattByMAC(String mac){
        Log.i("testbt", mac);
        if (bluetoothGatts == null){
            return null;
        }
        for (BluetoothGatt gatt: bluetoothGatts){
            if (mac.equals(gatt.getDevice().getAddress())) return gatt;
        }
        return null;
    }

}

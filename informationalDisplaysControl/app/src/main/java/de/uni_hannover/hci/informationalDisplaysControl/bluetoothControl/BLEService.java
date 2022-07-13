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
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BLEService extends Service {

    private Binder binder = new LocalBinder();
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;

    // connection state
    public final static String ACTION_GATT_CONNECTED = "GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "DATA_AVAILABLE";

    private final static String SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
    private final static String MODE_CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8";
    private final static String BUTTON_CHARACTERISTIC_UUID = "07bf0001-7a36-490f-ba53-345b3642a694";

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 2;

    private int connectionState;



    // Callback, um Verbindung zu überwachen -------------------------------------------------------
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("testbt", "CONNECTION_STATE_CHANGE: "+ newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                connectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);
                // discover services after connection
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                connectionState = STATE_DISCONNECTED;
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
            }
        }

        // Send broadcast, when Services discovered
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                setCharacteristicNotification(bluetoothGatt.getService(UUID.fromString(SERVICE_UUID)).getCharacteristic(UUID.fromString(BUTTON_CHARACTERISTIC_UUID)), true);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                readCharacteristic();
            } else {
                Log.i("testbt", "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
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
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            bluetoothGatt = device.connectGatt(this, true, bluetoothGattCallback);
            return true;
        } catch (IllegalArgumentException exception) {
            Log.w("testbt", "Device not found with provided address.  Unable to connect.");
            return false;
        }
    }

    // BLE-Service - Services und Characteristics suchen und kommunizieren -------------------------
    @SuppressLint("MissingPermission")
    public List<BluetoothGattService> getSupportedGattServices() {
        if (bluetoothGatt == null) return null;
        return bluetoothGatt.getServices();
    }

    // read value
    @SuppressLint("MissingPermission")
    public void readCharacteristic() {
        if (bluetoothGatt == null) {
            Log.w("testbt", "BluetoothGatt not initialized");
            return;
        }
        BluetoothGattCharacteristic characteristic = bluetoothGatt.getService(UUID.fromString(SERVICE_UUID)).getCharacteristic(UUID.fromString(MODE_CHARACTERISTIC_UUID));
        bluetoothGatt.readCharacteristic(characteristic);
    }

    // write string value
    @SuppressLint("MissingPermission")
    public void writeCharacteristic(String data) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w("testbt", "BluetoothAdapter not initialized");
            return;
        }

        byte[] value = data.getBytes(StandardCharsets.UTF_8);
        BluetoothGattCharacteristic characteristic = bluetoothGatt.getService(UUID.fromString(SERVICE_UUID)).getCharacteristic(UUID.fromString(MODE_CHARACTERISTIC_UUID));
        characteristic.setValue(value);
        bluetoothGatt.writeCharacteristic(characteristic);
    }

    // write int value
    @SuppressLint("MissingPermission")
    public void writeCharacteristic(int data) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w("testbt", "BluetoothAdapter not initialized");
            return;
        }

        byte[] value = BigInteger.valueOf(data).toByteArray();
        BluetoothGattCharacteristic characteristic = bluetoothGatt.getService(UUID.fromString(SERVICE_UUID)).getCharacteristic(UUID.fromString(MODE_CHARACTERISTIC_UUID));
        characteristic.setValue(value);
        bluetoothGatt.writeCharacteristic(characteristic);
    }

    // setup notification
    @SuppressLint("MissingPermission")
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (bluetoothGatt == null) {
            Log.w("testbt", "BluetoothGatt not initialized");
            return;
        }
        bluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (MODE_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }
    }

    public static class SampleGattAttributes {
        private static HashMap<String, String> attributes = new HashMap();
        public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
        public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

        static {
            // Sample Services.
            attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
            // Sample Characteristics.
            attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        }
    }


    // Service/BLE-Verbindung beenden --------------------------------------------------------------
    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    @SuppressLint("MissingPermission")
    public void close() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.disconnect();
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    // Activities benachrichtigen ------------------------------------------------------------------
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        if (MODE_CHARACTERISTIC_UUID.equals(characteristic.getUuid().toString())) {
            final String testCharacteristic = characteristic.getStringValue(0);
            Log.i("testbt", String.format("Test characteristic: %s", testCharacteristic));
            intent.putExtra("CHAR_DATA", testCharacteristic);
        } else if (BUTTON_CHARACTERISTIC_UUID.equals(characteristic.getUuid().toString())) {
            final String data = characteristic.getStringValue(0);
            if (data.equals("1")) Log.i("testbt", "BUTTON PRESSED!");
            else Log.i("testbt", "BUTTON NOT PRESSED!");
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra("CHAR_DATA", new String(data) + "\n" + stringBuilder.toString());
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

}

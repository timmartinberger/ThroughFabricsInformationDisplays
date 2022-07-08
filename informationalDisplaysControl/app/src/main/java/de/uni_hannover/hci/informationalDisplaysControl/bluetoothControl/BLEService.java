package de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.List;

public class BLEService extends Service {

    private Binder binder = new LocalBinder();
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;

    // connection state
    public final static String ACTION_GATT_CONNECTED = "GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "GATT_SERVICES_DISCOVERED";


    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 2;

    private int connectionState;



    // Callback, um Verbindung zu überwachen -------------------------------------------------------
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("testbt", "CONNECTION_STATE_CHANGE: "+ newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                connectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                connectionState = STATE_DISCONNECTED;
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.i("testbt", "onServicesDiscovered received: " + status);
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
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            bluetoothGatt = device.connectGatt(this, true, bluetoothGattCallback);
            return true;
        } catch (IllegalArgumentException exception) {
            Log.w("testbt", "Device not found with provided address.  Unable to connect.");
            return false;
        }
    }

    // BLE-Service - suchen und verbinden ----------------------------------------------------------
    public List<BluetoothGattService> getSupportedGattServices() {
        if (bluetoothGatt == null) return null;
        return bluetoothGatt.getServices();
    }


    // Service/BLE-Verbindung beenden --------------------------------------------------------------
    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    @SuppressLint("MissingPermission")
    private void close() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    // Activities benachrichtigen ------------------------------------------------------------------
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    public static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        return intentFilter;
    }



}

package de.uni_hannover.hci.informationalDisplaysControl.GameLogic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.uni_hannover.hci.informationalDisplaysControl.R;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEService;

public class BLETestActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothGatt> bluetoothGatts;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning;

    private boolean connected;
    private Handler handler = new Handler();

    // addresses of ESPs
    private String ESP1Address = "C8:C9:A3:C6:77:72";


    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 2000;
    private ScanCallback leScanCallback;

    // UI elements
    Button scanButton;
    Button connectButton;
    TextView scanResultView;


    private BluetoothGattCallback gattCallback;
    private BLEService bleService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bletest);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothGatts = new ArrayList<>();

        scanButton = findViewById(R.id.scan);
        connectButton = findViewById(R.id.connect);
        scanResultView = findViewById(R.id.results);

        connectButton.setClickable(false);


        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanLeDevice();
            }
        });

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connect();
            }
        });

        // Scan-------------------------------------------------------------------------------------
        leScanCallback = new ScanCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                @SuppressLint("MissingPermission") String[] device = {result.getDevice().getName(), result.getDevice().getAddress()};
                String res = device[0] + ": " + device[1] + "\n";
                scanResultView.setText(scanResultView.getText() + res);
            }
        };

        // Callback --------------------------------------------------------------------------------
        gattCallback = new BluetoothGattCallback() {
            @Override
            public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                super.onPhyUpdate(gatt, txPhy, rxPhy, status);
                Log.i("Testbt", "onPhyUpdate");
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // successfully connected to the GATT Server
                    Log.i("testbt", "BLE connected!");
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // disconnected from the GATT Server
                    Log.i("testbt", "BLE disconnected!");
                }
            }

        };

    }

    // Scanning ------------------------------------------------------------------------------------
    @SuppressLint("MissingPermission")
    private void scanLeDevice() {
        if (!scanning) {
            scanResultView.setText("");
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            scanning = true;
            checkPermissions(this, this);
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    // Permissions ---------------------------------------------------------------------------------
    public static void checkPermissions(Activity activity, Context context) {
        int PERMISSION_ALL = 1;
        @SuppressLint("InlinedApi") String[] PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_PRIVILEGED,
        };

        if (!hasPermissions(context, PERMISSIONS)) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS, PERMISSION_ALL);
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    // Verbindungsaufbau ---------------------------------------------------------------------------
    public void connect() {
        BluetoothDevice esp1Device = bluetoothAdapter.getRemoteDevice(ESP1Address);
        Log.i("testbt", "connect called!");
        checkPermissions(this, this);
        BluetoothGatt gatt1 = esp1Device.connectGatt(this, false, gattCallback);
        bluetoothGatts.add(gatt1);
        Intent gattServiceIntent = new Intent(this, BLEService.class);
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    // Verbindungsaufbau
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bleService = ((BLEService.LocalBinder) service).getService();
            if (bleService != null) {
                // call functions on service to check connection and connect to devices
                if (!bleService.initialize()) {
                    Log.e("testbt", "Unable to initialize Bluetooth");
                    finish();
                }
                // perform device connection
                if (bleService.connect(ESP1Address)){
                    Log.i("testbt", "bleService connected!");
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bleService = null;
        }
    };

    // Benachrichtigungen über Verbindungsstatus vom BLEService empfangen --------------------------
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.i("testbt", "Broadcastreceiver got notification: " + action);
            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
                connected = true;
                Toast.makeText(getApplicationContext(), ESP1Address + " connected!", Toast.LENGTH_LONG).show();
            } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                connected = false;
                Toast.makeText(getApplicationContext(), ESP1Address + " disconnected!", Toast.LENGTH_LONG).show();
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver, BLEService.makeGattUpdateIntentFilter());
        if (bleService != null) {
            // Todo check if connect hier wirklich gecalled werden soll!
            final boolean result = bleService.connect(ESP1Address);
            Log.d("testbt", "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        Log.i("testbt", "onPause()");
        super.onPause();
        //unregisterReceiver(gattUpdateReceiver);
    }





}
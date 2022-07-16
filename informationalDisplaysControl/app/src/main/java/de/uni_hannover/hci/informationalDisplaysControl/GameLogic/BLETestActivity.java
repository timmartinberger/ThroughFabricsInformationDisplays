package de.uni_hannover.hci.informationalDisplaysControl.GameLogic;

import static android.graphics.Color.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import de.uni_hannover.hci.informationalDisplaysControl.R;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEService;

public class BLETestActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning;

    private Handler handler = new Handler();

    // addresses of ESPs
    private String ESP1Address = "C8:C9:A3:C6:77:72";


    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 2000;
    private ScanCallback leScanCallback;

    // UI elements
    Button readButton;
    Button connectButton;
    Button writeButton;
    EditText scanResultView;
    ImageView statusCircle;

    public static final int STATUS_RED = 0;
    public static final int STATUS_GREEN = 1;

    private BLEService bleService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bletest);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        connectButton = findViewById(R.id.connect);
        scanResultView = findViewById(R.id.results);
        ImageView imageView = new ImageView(this);
        imageView.setImageDrawable(getDrawable(R.drawable.bt_status_circle));
        statusCircle = findViewById(R.id.bleTestStatus);
        writeButton = findViewById(R.id.write);
        readButton = findViewById(R.id.read);
        readButton.setClickable(false);

        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] b = {0x00, 0x11};
                //bleService.writeCharacteristic(b);
            }
        });


        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanResultView.setText("");
                //bleService.readCharacteristic();
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
    private void connect() {
        if (bleService == null) {
            checkPermissions(this, this);
            Intent gattServiceIntent = new Intent(this, BLEService.class);
            getApplicationContext().bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            if(bleService.connect(ESP1Address)){
                Log.i("testbt", "BLE Gatt connected!");
            }
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bleService = ((BLEService.LocalBinder) service).getService();
            Log.i("testbt", "SERVICE connected!");
            if (bleService != null) {
                // call functions on service to check connection and connect to devices
                if (!bleService.initialize()) {
                    Log.e("testbt", "Unable to initialize Bluetooth");
                    finish();
                }
                // perform device connection
                if (bleService.connect(ESP1Address)){

                    Log.i("testbt","bleService connected!");
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("testbt", "SERVICE disconnected!");
            bleService = null;
        }
    };

    private void setStatusColor(int status){
        if (status == STATUS_RED) {
            statusCircle.setColorFilter(parseColor("#fc1c03"));
            readButton.setClickable(false);
        } else if (status == STATUS_GREEN) {
            statusCircle.setColorFilter(parseColor("#05a615"));
            readButton.setClickable(true);
        }
    }

    // Benachrichtigungen über Verbindungsstatus vom BLEService empfangen --------------------------
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.i("testbt", "Broadcastreceiver got notification: " + action);
            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext(), ESP1Address + " connected!", Toast.LENGTH_LONG).show();
                setStatusColor(STATUS_GREEN);
            } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext(), ESP1Address + " disconnected!", Toast.LENGTH_LONG).show();
                setStatusColor(STATUS_RED);
            } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                //displayGattServices(bleService.getSupportedGattServices());
                Log.i("testbt", "Services found:");
//                for(BluetoothGattService service: bleService.getSupportedGattServices()){
//                    Log.i("testbt", service.getUuid().toString());
//                }
            } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                String result = intent.getStringExtra("CHAR_DATA");
                Log.i("testbt", "Value: " + result);
                scanResultView.setText(result);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver, BLEService.makeGattUpdateIntentFilter());
//        if (bleService != null) {
//            final boolean result = bleService.connect(ESP1Address);
//            Log.i("testbt", "Connect request result=" + result);
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }


    @Override
    protected void onDestroy() {
        bleService.closeAll();
        super.onDestroy();
    }

}
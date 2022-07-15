package de.uni_hannover.hci.informationalDisplaysControl;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import de.uni_hannover.hci.informationalDisplaysControl.GameLogic.*;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEService;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.Devices;


public class GameMenu extends AppCompatActivity {

    private ArrayList<Game> gameList;
    private RecyclerView RVGames;

    // BLE stuff
    private BLEService bleService;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_menu);

        RVGames = findViewById(R.id.RVGames);

        // Create List of games
        // TODO: Create the games here
        gameList = new ArrayList<>();

        gameList.add(new Game(getString(R.string.who_am_i), "This is a guessing game where players use yes or no questions to guess the identity of a famous person or fictional character.", getDrawable(R.drawable.whoami_darker), WhoAmI.class));
        gameList.add(new Game(getString(R.string.hot_pixels), "", null, DobbleController.class));
        gameList.add(new Game(getString(R.string.drawing_guessing), "", null, null));
        gameList.add(new Game(getString(R.string.four_wins), "", null, null));
        gameList.add(new Game(getString(R.string.send_text), "Send a any text to the LED Matrices", null, SendText.class));
        gameList.add(new Game("BLETest", "", null, BLETestActivity.class));

        GameAdapter gameAdapter = new GameAdapter(this, gameList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RVGames.setLayoutManager(linearLayoutManager);
        RVGames.setAdapter(gameAdapter);

        // Connect service
        // todo change place of permission
        BLETestActivity.checkPermissions(this, this);
        Intent gattServiceIntent = new Intent(this, BLEService.class);
        getApplicationContext().bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

    }



    // Service Connection ----------------------------
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
                for(int i = 0; i < Devices.getDeviceCount(); i++) {
                    String mac = Devices.getMacAsString(i);
                    if (bleService.connect(mac)) {
                        Log.i("testbt", mac + " connected!");
                    }
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("testbt", "SERVICE disconnected!");
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
                Toast.makeText(getApplicationContext(), intent.getDataString() + " connected!", Toast.LENGTH_LONG).show();
            } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext(), intent.getDataString() + " disconnected!", Toast.LENGTH_LONG).show();
            } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                //displayGattServices(bleService.getSupportedGattServices());
                Log.i("testbt", "Services found:");
                String data = intent.getStringExtra("CHAR_DATA");
                Log.i("testbt", "DATA: " + data);
                for(BluetoothGattService service: bleService.getSupportedGattServices(bleService.getGattByMAC(data))){
                    Log.i("testbt", service.getUuid().toString());
                }
            } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                String result = intent.getStringExtra("CHAR_DATA");
                Log.i("testbt", "Value: " + result);

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
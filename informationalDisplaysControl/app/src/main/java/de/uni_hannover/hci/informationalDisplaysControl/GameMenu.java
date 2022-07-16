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
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.uni_hannover.hci.informationalDisplaysControl.GameLogic.*;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEService;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEServiceInstance;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.Devices;


public class GameMenu extends AppCompatActivity {

    private ArrayList<Game> gameList;
    private RecyclerView RVGames;

    private boolean serviceBound;


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
        gameList.add(new Game(getString(R.string.dobble), "", null, DobbleController.class));
        gameList.add(new Game(getString(R.string.drawing_guessing), "", null, MontagsMalerController.class));
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
        serviceBound = this.bindService(gattServiceIntent, BLEServiceInstance.serviceConnection, Context.BIND_AUTO_CREATE);

    }

    // Benachrichtigungen über Verbindungsstatus vom BLEService empfangen --------------------------
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.i("testbt", "Broadcastreceiver got notification: " + action);
            String address = intent.getStringExtra("ADDRESS");
            // todo remove?
            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {

            }
            else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext(), address + " disconnected!", Toast.LENGTH_LONG).show();
            }
            else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.i("testbt", "Services found:");

                List<BluetoothGattService> services = BLEServiceInstance.getBLEService().getSupportedGattServices(BLEServiceInstance.getBLEService().getGattByMAC(address));
                for (BluetoothGattService service: services){
                    if (service.getUuid().toString().equals(BLEService.SERVICE_UUID)){
                        Toast.makeText(getApplicationContext(), address + " connected!", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                Toast.makeText(getApplicationContext(), "Device with address " + address + " is not compatible!", Toast.LENGTH_LONG).show();
                finish();
            }
            else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    for (String key : bundle.keySet()) {
                        Log.e("testbt", key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
                    }
                }
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
        Log.i("testbt", "servConn: " + BLEServiceInstance.serviceConnection.toString());
        if (serviceBound){
            this.unbindService(BLEServiceInstance.serviceConnection);
            serviceBound = false;
        }
        super.onDestroy();
    }


}
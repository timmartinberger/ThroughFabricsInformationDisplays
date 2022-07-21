package de.uni_hannover.hci.informationalDisplaysControl;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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

    public static ArrayList<Game> games;
    private RecyclerView RVGames;

    private boolean serviceBound;


    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_menu);

        RVGames = findViewById(R.id.RVGames);

        // Create List of games
        games = new ArrayList<>();

        games.add(new Game(getString(R.string.dobble), "Each players matrix contains eight symbols. Be the first to find the one to occur on all matrices.", getDrawable(R.drawable.dobble), DobbleController.class));
        games.add(new Game(getString(R.string.who_am_i), "This is a guessing game where players use yes or no questions to guess the identity of a famous person or fictional character.", getDrawable(R.drawable.whoami_darker), WhoAmI.class));
        games.add(new Game(getString(R.string.drawing_guessing), "One player has to paint a something specific and the others have to guess what it is.", getDrawable(R.drawable.drawing_and_guessing), MontagsMalerController.class));
        games.add(new Game(getString(R.string.send_text), "Send a any text to the LED Matrices", getDrawable(R.drawable.send_text), SendText.class));

        GameAdapter gameAdapter = new GameAdapter(this, games);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RVGames.setLayoutManager(linearLayoutManager);
        RVGames.setAdapter(gameAdapter);

        // Connect service
        Utils.checkPermissions(this, this);
        Intent gattServiceIntent = new Intent(this, BLEService.class);
        serviceBound = this.bindService(gattServiceIntent, BLEServiceInstance.serviceConnection, Context.BIND_AUTO_CREATE);

        AlertDialog.Builder endGameMenu = new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_baseline_videogame_asset_24)
                .setTitle("Do you want to close the game menu?")
                .setMessage("Your bluetooth devices will be disconnected!")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        BLEServiceInstance.getBLEService().closeAll();
                        BLEServiceInstance.deleteService();
                        finish();
                    }
                })
                .setNegativeButton("No", null);

        // Add callback to close gameMenu
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                    endGameMenu.show();
            }
        };

        this.getOnBackPressedDispatcher().addCallback(this, callback);

    }

    // Benachrichtigungen über Verbindungsstatus vom BLEService empfangen --------------------------
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.i("testbt", "Broadcastreceiver got notification: " + action);
        String address = intent.getStringExtra("ADDRESS");
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
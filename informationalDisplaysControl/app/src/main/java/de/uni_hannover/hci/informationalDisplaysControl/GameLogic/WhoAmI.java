package de.uni_hannover.hci.informationalDisplaysControl.GameLogic;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import de.uni_hannover.hci.informationalDisplaysControl.Utils;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.*;
import de.uni_hannover.hci.informationalDisplaysControl.R;

import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class WhoAmI extends AppCompatActivity {

    List<String> allNames = new ArrayList<>();
    private boolean serviceBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_who_am_i);

        // Bind service
        Intent gattServiceIntent = new Intent(this, BLEService.class);
        serviceBound = this.bindService(gattServiceIntent, BLEServiceInstance.serviceConnection, Context.BIND_AUTO_CREATE);

        BLEServiceInstance.getBLEService().writeCharacteristicToAll(BLEService.MODE_CHARACTERISTIC_UUID, "2");
        OnBackPressedCallback endWhoAmICallback = Utils.endGameCallback(this);
        this.getOnBackPressedDispatcher().addCallback(this, endWhoAmICallback);

        initNameList();
    }

    public void startGame(View view) {
        System.out.println("Starting the game...");
        if(allNames.isEmpty()) {
            initNameList();
        }
        int numberOfPlayers = Devices.getDeviceCount();
        ArrayList<String> nameList = generateNameList(numberOfPlayers);
        if(nameList.isEmpty()) {
            msg("No devices connected!");
            System.out.println("No devices connected!");
            return;
        }
        //for each name in nameList send to a device
        for(String name: nameList) {
            System.out.println(name);

        }
        sendNamesToDevices(nameList);
        msg("The game has started!");
    }

    private ArrayList<String> generateNameList(int numberOfPlayers) {
        ArrayList<String> copyAllNames = new ArrayList<>(this.allNames);
        ArrayList<String> nameList = new ArrayList<>();
        Random random = new Random();
        for(int i = 0; i < numberOfPlayers; i ++) {
            String name = copyAllNames.get(random.nextInt(copyAllNames.size()));
            nameList.add(name);
            copyAllNames.remove(name);
        }
        return nameList;
    }

    private void initNameList() {
        try {
            final InputStream file = getAssets().open("famous_people.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(file));
            this.allNames = reader.lines().collect(Collectors.toList());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void sendNamesToDevices(ArrayList<String> nameList) {
        for(int i = 0; i < nameList.size(); i++){
            BLEServiceInstance.getBLEService().writeCharacteristic(Devices.getMacAsString(i), BLEService.DATA_CHARACTERISTIC_UUID, nameList.get(i));
        }
    }


    // Fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
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
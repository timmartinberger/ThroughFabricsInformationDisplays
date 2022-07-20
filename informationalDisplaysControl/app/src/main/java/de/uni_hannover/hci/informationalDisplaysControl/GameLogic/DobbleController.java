package de.uni_hannover.hci.informationalDisplaysControl.GameLogic;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import de.uni_hannover.hci.informationalDisplaysControl.R;
import de.uni_hannover.hci.informationalDisplaysControl.Utils;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEService;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEServiceInstance;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.Devices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DobbleController extends AppCompatActivity {

    private TextView roundsInput;
    private final int MIN_PLAYERS = 3;
    private boolean isGameRunning = false;
    private Dobble dobble;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dobble);

        this.roundsInput = findViewById(R.id.roundsInput);
        BLEServiceInstance.getBLEService().writeCharacteristicToAll(BLEService.MODE_CHARACTERISTIC_UUID, "5");
        OnBackPressedCallback endDobbleCallback = Utils.endGameCallback(this);
        this.getOnBackPressedDispatcher().addCallback(this, endDobbleCallback);
    }

    private int  getRounds() {
        int rounds = 5;
        if(!roundsInput.getText().toString().isEmpty()) {
            String s = roundsInput.getText().toString();
            rounds = Integer.parseInt(s);
        }
        return rounds;
    }

    public void startGame(View view) {
        int numberOfPlayers = Devices.getDeviceCount();
        if (numberOfPlayers < MIN_PLAYERS) {
            Toast msg = Toast.makeText(this, "Not enough players! 3 players are needed!", Toast.LENGTH_SHORT);
            msg.show();
        } else {
            Button stopGameButton = findViewById(R.id.stopGameButton);
            dobble = new Dobble(getRounds(), numberOfPlayers);
            Thread thread = new Thread(dobble);
            Thread waitGame = new Thread(new Runnable() {
                @Override
                public void run() {
                        try {
                            isGameRunning=true;
                            thread.start();
                            thread.join();
                            // game finished naturally
                            System.out.println("Game finished!");
                        } catch (InterruptedException e) {
                            thread.interrupt();
                        }
                        finally {
                            isGameRunning = false;
                        }
                }
            });

            stopGameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(isGameRunning) {
                        waitGame.interrupt();
                        thread.interrupt();
                        isGameRunning = false;
                    }
                }
            });

            if(!isGameRunning) {
                isGameRunning = true;
                waitGame.start();
            }
            else {
                //start button no use while game running
                Log.i("testbt", "Game is already running!");
            }
        }

    }

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.i("testbt", "Broadcastreceiver got notification: " + action);
            final String address = intent.getStringExtra("ADDRESS");

            if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                final String data = intent.getStringExtra("BUTTON_DATA");
                if (data.equals("PRESSED")){
                    BLEServiceInstance.setControllerOptions(dobble.deviceMacList, "", false);
                    dobble.whoPressed = address;
                    dobble.buttonPressed = true;
                    //disable button for some time
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException interruptedException) {
                        Log.i("testbt", "Interrupted during button timeout!");
                    }
                    BLEServiceInstance.setControllerOptions(dobble.deviceMacList, "", true);
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
}
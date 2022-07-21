package de.uni_hannover.hci.informationalDisplaysControl.GameLogic;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import de.uni_hannover.hci.informationalDisplaysControl.R;
import de.uni_hannover.hci.informationalDisplaysControl.Utils;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEService;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEServiceInstance;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.Devices;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DobbleController extends AppCompatActivity {

    private TextView roundsInput;
    private final int MIN_PLAYERS = 1;
    private boolean isGameRunning = false;
    private Dobble dobble;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dobble);

        TextView gameDes = findViewById(R.id.gameDescription);
        gameDes.setVisibility(View.VISIBLE);
        gameDes.setText(R.string.dobble_description);

        ImageView imageView = findViewById(R.id.gameImage);
        imageView.setImageDrawable(getDrawable(R.drawable.dobble));


        if (Devices.getDeviceCount() < 3){
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_baseline_videogame_asset_24)
                    .setTitle("You are not enough players!")
                    .setMessage("For Dobble there are three players required. Select another game...")
                    .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
            }).show();
        }

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
        System.out.println("number of rounds: " + rounds);
        return rounds;
    }

    public void startGame(View view) {
        int numberOfPlayers = Devices.getDeviceCount();
        //numberOfPlayers = 1;
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
                System.out.println("GAME IS ALREADY RUNNING!");
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
                        System.out.println("Interrupted while button disabled!");
                    }
                    BLEServiceInstance.setControllerOptions(dobble.deviceMacList, "", true);
                    //BLEServiceInstance.getBLEService().setCharacteristicNotification(device, BLEService.BUTTON_CHARACTERISTIC_UUID, enableButton);
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
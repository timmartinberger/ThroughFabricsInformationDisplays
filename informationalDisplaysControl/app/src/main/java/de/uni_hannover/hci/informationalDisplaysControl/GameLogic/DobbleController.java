package de.uni_hannover.hci.informationalDisplaysControl.GameLogic;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import de.uni_hannover.hci.informationalDisplaysControl.R;
import de.uni_hannover.hci.informationalDisplaysControl.Utils;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEService;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEServiceInstance;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.Devices;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DobbleController extends AppCompatActivity {

    private TextView roundsInput;
    private final int MIN_PLAYERS = 2;
    private boolean isGameRunning = false;
    private Thread gameThread;
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
            System.out.println("Starting the game!");
            Dobble dobble = new Dobble(this, getRounds(), numberOfPlayers);
            Thread thread = new Thread(dobble);
            Thread waitGame = new Thread(new Runnable() {
                @Override
                public void run() {
                        try {
                            isGameRunning=true;
                            thread.start();
                            thread.join();
                            System.out.println("Game finished!");
                        } catch (InterruptedException e) {
                            thread.interrupt();
                            System.out.println("game interrupted by stop button");
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
                        //thread.interrupt();
                        isGameRunning = false;
                        System.out.println("STOPPING THE GAME...");
                    }
                }
            });
            if(!isGameRunning) {
                waitGame.start();
            }
            else {
                System.out.println("GAME IS ALREADY RUNNING!");
            }
        }

    }

    public void stopGame(View view) {
    }
}
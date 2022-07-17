package de.uni_hannover.hci.informationalDisplaysControl.GameLogic;

import androidx.appcompat.app.AppCompatActivity;
import de.uni_hannover.hci.informationalDisplaysControl.R;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEService;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEServiceInstance;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.Devices;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class DobbleController extends AppCompatActivity {

    private TextView roundsInput;
    private final int MIN_PLAYERS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dobble);

        this.roundsInput = findViewById(R.id.roundsInput);

        BLEServiceInstance.getBLEService().writeCharacteristicToAll(BLEService.MODE_CHARACTERISTIC_UUID, "5");
    }

    private Dobble initGame(int numberOfPlayers) {
        int rounds = 5;
        if(!roundsInput.getText().toString().isEmpty()) {
            String s = roundsInput.getText().toString();
            rounds = Integer.parseInt(s);
        }
        System.out.println("number of rounds: " + rounds);
        return new Dobble(numberOfPlayers, rounds);
    }

    public void startGame(View view) {
        int numberOfPlayers = Devices.getDeviceCount();
        if(numberOfPlayers < MIN_PLAYERS) {
            Toast msg = Toast.makeText(this, "Not enough players! 3 players are needed!", Toast.LENGTH_SHORT);
            msg.show();
        }
        else {
            initGame(numberOfPlayers).startGame();
        }
    }
}
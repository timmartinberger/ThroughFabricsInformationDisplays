package de.uni_hannover.hci.informationalDisplaysControl.GameLogic;

import androidx.appcompat.app.AppCompatActivity;
import de.uni_hannover.hci.informationalDisplaysControl.R;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEService;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEServiceInstance;

import android.os.Bundle;
import android.view.View;

public class DobbleController extends AppCompatActivity {

    private Dobble dobble3;
    private Dobble dobble4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dobble);
        BLEServiceInstance.getBLEService().writeCharacteristicToAll(BLEService.MODE_CHARACTERISTIC_UUID, "5");
    }

    private Dobble initGame(int i) {
        return new Dobble(i);
    }

    public void startGame(View view) {
        initGame(3).startGame();
    }
}
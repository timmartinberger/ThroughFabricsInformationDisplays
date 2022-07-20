package de.uni_hannover.hci.informationalDisplaysControl.GameLogic;


import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import de.uni_hannover.hci.informationalDisplaysControl.Utils;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEService;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEServiceInstance;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.Devices;
import de.uni_hannover.hci.informationalDisplaysControl.R;


public class SendText extends AppCompatActivity {

    // Button btnOn, btnOff, btnDis;
    Button SendBtn;
    ListView devices;
    EditText textInput;
    ArrayList<String> addresses = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        //view of the ledControl
        setContentView(R.layout.activity_send_text);

        //call the widgets
        SendBtn = (Button) findViewById(R.id.sendbtn);
        textInput = (EditText) findViewById(R.id.toSend);
        devices = findViewById(R.id.dev);

        TextView gameDes = findViewById(R.id.gameDescription);
        gameDes.setVisibility(View.VISIBLE);
        gameDes.setText(R.string.send_text_description);

        ImageView imageView = findViewById(R.id.gameImage);
        imageView.setImageDrawable(getDrawable(R.drawable.send_text));

        // Send text to ALL esp when button clicked
        SendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(String address : addresses){
                    BLEServiceInstance.getBLEService().writeCharacteristic(address, BLEService.DATA_CHARACTERISTIC_UUID, textInput.getText().toString());
                }
            }
        });

        createList();

        BLEServiceInstance.getBLEService().writeCharacteristicToAll(BLEService.MODE_CHARACTERISTIC_UUID, "2");
        OnBackPressedCallback endSTCallback = Utils.endGameCallback(this);
        this.getOnBackPressedDispatcher().addCallback(this, endSTCallback);
    }


    private void createList() {
        ArrayList list = new ArrayList();

        for (int i = 0; i < Devices.getDeviceCount(); i++) {
            list.add(Devices.getName(i) + Devices.getMacAsString(i)); //Get the device's name and the address
        }
        if (list.size() == 0) {
            Toast.makeText(getApplicationContext(), "No devices found!", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, list);
        devices.setAdapter(adapter);
        devices.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            public void onItemClick(AdapterView<?> av, View v, int pos, long arg3) {
                // Get the device MAC address, the last 17 chars in the View
                String info = ((TextView) v).getText().toString();
                String address = info.substring(info.length() - 17);

                boolean selected = devices.isItemChecked(pos);
                try {
                    if (selected) {
                        addresses.add(address);
                    } else {
                        addresses.remove(address);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } //Method called when the device from the list is clicked

        });
    }

}
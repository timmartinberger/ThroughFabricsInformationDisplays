package de.uni_hannover.hci.informationalDisplaysControl.GameLogic;


import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import android.bluetooth.BluetoothSocket;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BluetoothConnection;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.Devices;
import de.uni_hannover.hci.informationalDisplaysControl.R;


public class SendText extends AppCompatActivity {

    // Button btnOn, btnOff, btnDis;
    Button SendBtn, Discnt;
    EditText textInput;
    ArrayList<String> addresses = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        for (int i = 0; i < Devices.getDeviceCount(); i++) {
            addresses.add(Devices.getMacAsString(i)); //receive the address of the bluetooth device
        }

        //view of the ledControl
        setContentView(R.layout.activity_led_control);

        //call the widgets
        SendBtn = (Button) findViewById(R.id.off_btn);
        Discnt = (Button) findViewById(R.id.dis_btn);
        textInput = (EditText) findViewById(R.id.textInput);

        // Send text to ALL esp when button clicked
        SendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTextToAll();
            }
        });

        // Close connections to ALL esps
        Discnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // Send text via bluetooth to ALL esp32
    private void sendTextToAll() {
        String text = textInput.getText().toString();
        for (String address : addresses) {
            BluetoothConnection btConnection = new BluetoothConnection();
            btConnection.sendText(address, text, this);
        }
      }
}
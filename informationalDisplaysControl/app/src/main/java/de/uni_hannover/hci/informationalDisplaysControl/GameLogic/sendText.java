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

import de.uni_hannover.hci.informationalDisplaysControl.Devices;
import de.uni_hannover.hci.informationalDisplaysControl.R;


public class sendText extends AppCompatActivity {

   // Button btnOn, btnOff, btnDis;
    Button SendBtn, Discnt;
    EditText textInput;
//    String address = null;
//    private ProgressDialog progress;
//    BluetoothAdapter myBluetooth = null;
//    BluetoothSocket btSocket = null;
//    private boolean isBtConnected = false;

    // Alternative code for multiple devices
    ArrayList<String> addresses = new ArrayList<String>();
    private ProgressDialog progress;
    BluetoothAdapter btAdapter = null;
    ArrayList<BluetoothSocket> btSockets = new ArrayList<BluetoothSocket>();



    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        for(int i = 0; i < Devices.getDeviceCount(); i++){
            Log.i("testbt", "i = " + Integer.toString(i));
            Log.i("testbt", "Dev.len = " + Integer.toString(Devices.getDeviceCount()));
            addresses.add(Devices.getMacAsString(i)); //receive the address of the bluetooth device
        }

        //view of the ledControl
        setContentView(R.layout.activity_led_control);

        //call the widgets
        SendBtn = (Button)findViewById(R.id.off_btn);
        Discnt = (Button)findViewById(R.id.dis_btn);
        textInput = (EditText)findViewById(R.id.textInput);

        for(String address: addresses){
            new ConnectBT(address).execute(); //Call the class to connect
        }


        // Send text to ALL esp when button clicked
        SendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i < btSockets.size(); i++) {
                    sendText(i);
                }
            }
        });

        // Close connections to ALL esps
        Discnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i < btSockets.size(); i++) {
                    Disconnect(i);
                }
            }
        });


    }

    // Send text via bluetooth to ALL esp32
    private void sendText(int index) {
        BluetoothSocket socket = btSockets.get(index);
        if (socket != null) {
            try {
                socket.getOutputStream().write(textInput.getText().toString().getBytes());
                Log.i("test", "Der Stream wurde gesendet!");
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    // Fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    // Disconnect a esp with a specific index in btSocket
    private void Disconnect(int index) {
        BluetoothSocket socket = btSockets.get(index);
        if (socket!=null) {
            try {
                socket.getOutputStream().write((addresses + " disconnected!").getBytes());
                socket.close(); //close connection
            } catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout

    }


    private class ConnectBT extends AsyncTask<Void, Void, Void>  {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected
        private BluetoothSocket btSocket;
        private String btAddress;
        private boolean isConnected;

        public ConnectBT(String address){
            this.btAddress = address;
            this.isConnected = false;
        }


        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(sendText.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @SuppressLint("MissingPermission")
        @Override
        protected Void doInBackground(Void... devices) { //while the progress dialog is shown, the connection is done in background
            try {
                if (btSocket == null || !isConnected) {
                    if (btAdapter == null) {
                        btAdapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    }
                    BluetoothDevice dispositivo = btAdapter.getRemoteDevice(btAddress);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                    btSockets.add(btSocket);
                }
            } catch (Exception e) {
                ConnectSuccess = false;//if the try failed, you can check the exception here
                Log.i("test", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected.");
                isConnected = true;
            }
            progress.dismiss();
        }
    }
}

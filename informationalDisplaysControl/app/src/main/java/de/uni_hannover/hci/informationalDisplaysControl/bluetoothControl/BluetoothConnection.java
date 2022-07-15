package de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;


public class BluetoothConnection extends AsyncTask<Void, Void, Void> {
    private boolean ConnectSuccess = true;
    private String btAddress;
    private Context context;
    private ProgressDialog progress;
    private String text;
    BluetoothSocket btSocket = null;
    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");



    public void sendText(String address, String text, Context context) {
        this.btAddress = address;
        this.context = context;
        this.text = text;
        this.execute();
        disconnect();
    }

    private void disconnect() {
        if (btSocket!=null) {
            try {
                btSocket.close();
                btSocket = null;
            } catch (IOException e)
            {
                Log.i("testbt", e.toString());
            }
        }
    }

    @Override
    protected void onPreExecute() {
        this.progress = ProgressDialog.show(context, "Connecting...", "Please wait!!!");  //show a progress dialog
    }

    @SuppressLint("MissingPermission")
    @Override
    protected Void doInBackground(Void... devices) { //while the progress dialog is shown, the connection is done in background
        try {
            if (btSocket == null || !btSocket.isConnected()) {
                BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                BluetoothDevice device = btAdapter.getRemoteDevice(btAddress);//connects to the device's address and checks if it's available
                btSocket = device.createRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                btSocket.connect();//start connection
            }
        } catch (Exception e) {
            try {
                // Fall back solution if the above is not working
                // https://stackoverflow.com/questions/18657427/ioexception-read-failed-socket-might-closed-bluetooth-on-android-4-3
                Class<?> clazz = btSocket.getRemoteDevice().getClass();
                Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};

                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[] {Integer.valueOf(1)};

                btSocket = (BluetoothSocket) m.invoke(btSocket.getRemoteDevice(), params);
                Thread.sleep(500);
                btSocket.connect();
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException | InterruptedException noSuchMethodException) {
                noSuchMethodException.printStackTrace();
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            Log.i("testbt", "Standard call btSocket.connect() failed:");
            Log.i("testbt", e.toString());

        }
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        progress.dismiss();
        if (!ConnectSuccess) {
            Log.i("testbt", "Error occured on set up connection!");
        } else {
            try {
                btSocket.getOutputStream().write(this.text.getBytes());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


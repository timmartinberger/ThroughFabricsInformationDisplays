package de.uni_hannover.hci.informationalDisplaysControl;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.MacAddress;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Set;

import pub.devrel.easypermissions.EasyPermissions;


public class DeviceList extends AppCompatActivity {
    //widgets
    Button btnPaired;
    Button btnGo;
    ListView devicelist;
    //Bluetooth
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;

    // For intents to forward data of devices to connect to
    public static final String EXTRA_NAMES = "NAMES";
    public static final String EXTRA_ADDRESSES = "ADRESSES";


    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        //Calling widgets
        btnPaired = (Button)findViewById(R.id.update);
        devicelist = (ListView)findViewById(R.id.listView);
        btnGo = (Button)findViewById(R.id.select);

        // Check for BT permissions
        ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Toast.makeText(this, "Bluetooth permissions are granted.", Toast.LENGTH_LONG);
            } else {
                Toast.makeText(this, "Bluetooth permissions are necessary for this app in order to work properly!", Toast.LENGTH_LONG);
            }
        });

        // BT permissions for Android 12 or higher
        String[] BT_PERMISSIONS =  {android.Manifest.permission.BLUETOOTH_CONNECT, android.Manifest.permission.BLUETOOTH_SCAN};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if(!EasyPermissions.hasPermissions(this, BT_PERMISSIONS)){
                EasyPermissions.requestPermissions(this, "Bluetooth permissions are required!", 0, BT_PERMISSIONS);
            }

        }

        // Get BT adapter for the phones device
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if(myBluetooth == null) {
            //Show a mensag. that the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();

            //finish apk
            finish();
        } else if(!myBluetooth.isEnabled()) {
                //Ask to the user turn the bluetooth on
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon,1);
        }

        // Handle click on "update devices" button
        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedDevicesList();
            }
        });

        // Handlo click on "select" button
        btnGo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                // Make an intent to start next activity.
                Intent i = new Intent(DeviceList.this, GameMenu.class);

                //Change the activity.
//                i.putExtra(EXTRA_ADDRESSES, Devices.deviceAddress); //this will be received at ledControl (class) Activity
                startActivity(i);
            }
        });

    }

    private void pairedDevicesList() {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0) {
            for(BluetoothDevice bt : pairedDevices) {
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_multiple_choice, list);
        devicelist.setAdapter(adapter);
        devicelist.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick (AdapterView<?> av, View v, int pos, long arg3) {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            info = info.replace(address, "");
            MacAddress mcaddress = MacAddress.fromString(address);

            boolean selected = devicelist.isItemChecked(pos);
            try {
                if (selected) {
                    Devices.addDevice(info, mcaddress);
                } else {
                    Devices.removeDevice(mcaddress);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}

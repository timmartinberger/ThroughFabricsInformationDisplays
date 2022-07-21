package de.uni_hannover.hci.informationalDisplaysControl;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.MacAddress;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.Devices;
import pub.devrel.easypermissions.EasyPermissions;


public class DeviceList extends AppCompatActivity {

    // Widgets
    private Button btnUpdate;
    private Button btnGo;
    private ListView devicelist;

    //Bluetooth
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> devices;

    // Scanning
    private BluetoothLeScanner bluetoothLeScanner;
    private static final long SCAN_PERIOD = 2000;
    private ScanCallback leScanCallback;
    private boolean scanning;
    private Handler scanHandler = new Handler();

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        // Widgets
        btnUpdate = (Button)findViewById(R.id.update);
        devicelist = (ListView)findViewById(R.id.listView);
        btnGo = (Button)findViewById(R.id.select);



        // Permissions ----------------------------------------------
        // Check for BT permissions
        ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Toast.makeText(this, "Bluetooth permissions are granted.", Toast.LENGTH_LONG);
            } else {
                Toast.makeText(this, "Bluetooth permissions are necessary for this app in order to work properly!", Toast.LENGTH_LONG);
            }
        });

        // BT permissions for Android 12 or higher
        String[] BT_PERMISSIONS = {android.Manifest.permission.BLUETOOTH_CONNECT, android.Manifest.permission.BLUETOOTH_SCAN};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if(!EasyPermissions.hasPermissions(this, BT_PERMISSIONS)){
                EasyPermissions.requestPermissions(this, "Bluetooth permissions are required!", 0, BT_PERMISSIONS);
            }

        }

        // Get BT adapter for the phones device ---------------------
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = myBluetooth.getBluetoothLeScanner();
        if(myBluetooth == null) {
            //Show a mensag. that the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth device not available!", Toast.LENGTH_LONG).show();

            //finish apk
            finish();
        } else if(!myBluetooth.isEnabled()) {
                //Ask to the user turn the bluetooth on
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon,1);
        }

        // Scan Callback --------------------------------------------
        leScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                devices.add(result.getDevice());
            }
        };

        // Handle click on "update devices" button
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanBLEDevices();
            }
        });

        AlertDialog.Builder noDevices = new AlertDialog.Builder(this)
                                        .setIcon(R.drawable.ic_baseline_videogame_asset_24)
                                        .setTitle("Please select at least one device!")
                                        .setNeutralButton("Okay", null);

        // Handlo click on "select" button
        btnGo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Make an intent to start next activity.
                if (Devices.getDeviceCount() < 1){
                    noDevices.show();
                    return;
                }
                Intent i = new Intent(DeviceList.this, GameMenu.class);
                startActivity(i);
            }
        });

        // Start scanning
        scanBLEDevices();
    }

    // Scanning ------------------------------------------------------------------------------------
    @SuppressLint("MissingPermission")
    private void scanBLEDevices() {
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            ProgressDialog progress = ProgressDialog.show(this, "Searching for devices...", "Please wait!");  //show a progress dialog
            devices = new HashSet<>();
            scanHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                    progress.dismiss();
                    updateList();
                }
            }, SCAN_PERIOD);

            scanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    private void updateList() {
        ArrayList list = new ArrayList();

        if (devices.size()>0) {
            for(BluetoothDevice bt : devices) {
                if (bt.getName() != null)
                    list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        } else {
            Toast.makeText(getApplicationContext(), "No devices found!", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_multiple_choice, list);
        devicelist.setAdapter(adapter);
        devicelist.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked

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
                    Devices.removeDevice(address);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}

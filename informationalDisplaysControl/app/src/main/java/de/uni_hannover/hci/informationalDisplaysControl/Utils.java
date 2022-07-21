package de.uni_hannover.hci.informationalDisplaysControl;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEService;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEServiceInstance;

public class Utils {
    // Permission check
    public static void checkPermissions(Activity activity, Context context) {
        int PERMISSION_ALL = 1;
        @SuppressLint("InlinedApi") String[] PERMISSIONS = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_PRIVILEGED,
        };

        if (!hasPermissions(context, PERMISSIONS)) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS, PERMISSION_ALL);
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    // Abort game callback
    public static OnBackPressedCallback endGameCallback(AppCompatActivity context) {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                endGameDialog(context).show();
            }
        };
        return callback;
    }

    private static AlertDialog.Builder endGameDialog(AppCompatActivity context) {
         return new AlertDialog.Builder(context)
        .setIcon(R.drawable.ic_baseline_videogame_asset_24)
        .setTitle("Do you want to end this game?")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                BLEServiceInstance.getBLEService().writeCharacteristicToAll(BLEService.MODE_CHARACTERISTIC_UUID, "1");
                context.finish();
            }
        })
        .setNegativeButton("No", null);
    }
}

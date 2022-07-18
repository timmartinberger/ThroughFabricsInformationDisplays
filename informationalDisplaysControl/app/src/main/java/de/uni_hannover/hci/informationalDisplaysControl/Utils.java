package de.uni_hannover.hci.informationalDisplaysControl;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEService;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEServiceInstance;

public class Utils {

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
        .setIcon(android.R.drawable.ic_dialog_alert)
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

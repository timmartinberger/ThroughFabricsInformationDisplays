package de.uni_hannover.hci.informationalDisplaysControl.GameLogic;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import de.uni_hannover.hci.informationalDisplaysControl.Devices;
import de.uni_hannover.hci.informationalDisplaysControl.R;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WhoAmI extends AppCompatActivity {

    List<String> allNames = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_who_am_i);
        initNameList();
    }

    public void startGame(View view) {
        System.out.println("Starting the game...");
        if(allNames.isEmpty()) {
            initNameList();
        }
        int numberOfPlayers = Devices.getDeviceCount();
        ArrayList<String> nameList = generateNameList(numberOfPlayers);
        if(nameList.isEmpty()) {
            msg("No devices connected!");
            System.out.println("No devices connected!");
            return;
        }
        //for each name in nameList send to a device
        for(String name: nameList) {
            System.out.println(name);

        }
        sendNamesToDevices(nameList);
        msg("The game has started!");
    }

    private ArrayList<String> generateNameList(int numberOfPlayers) {
        ArrayList<String> nameList = new ArrayList<>();
        Random random = new Random();
        for(int i = 0; i < numberOfPlayers; i ++) {
            nameList.add(this.allNames.get(random.nextInt(this.allNames.size())));
        }
        return nameList;
    }

    private void initNameList() {
        try {
            final InputStream file = getAssets().open("famous_people.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(file));
            this.allNames = reader.lines().collect(Collectors.toList());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendNamesToDevices(ArrayList<String> nameList) {
        //TODO implement sending info to devices
    }

    // Fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }
}
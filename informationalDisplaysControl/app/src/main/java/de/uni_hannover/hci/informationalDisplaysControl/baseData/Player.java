package de.uni_hannover.hci.informationalDisplaysControl.baseData;

import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEServiceInstance;

public class Player {

    private final String address;
    private int points;

    public Player(String address) {
        this.address = address;
        this.points = 0;
    }

    public String getAddress() {
        return address;
    }

    public void updatePoints(boolean isCorrect) {
        if(isCorrect) {
            this.points++;
        }
        else {
            this.points--;
        }
    }

    public int getPoints() {
        return Math.max(points, 0);
    }

    public void showPoints(int colorCode) {
        BLEServiceInstance.sendPlayerPoints(this.address, getPoints(), colorCode);
    }
}

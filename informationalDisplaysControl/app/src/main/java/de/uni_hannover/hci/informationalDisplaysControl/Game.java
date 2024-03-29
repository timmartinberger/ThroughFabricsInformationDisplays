package de.uni_hannover.hci.informationalDisplaysControl;
import android.graphics.drawable.Drawable;

import androidx.appcompat.app.AppCompatActivity;


public class Game {
    private final String gameName;
    private final String gameDescription;
    private final Drawable gameImage;
    private final Class app;

    public Game(String gameName, String gameDescription, Drawable image, Class app) {
        this.gameName = gameName;
        this.gameDescription = gameDescription;
        this.gameImage = image;
        this.app = app;
    }

    public String getGameName() {
        return this.gameName;
    }


    public String getGameDescription() {
        return this.gameDescription;
    }


    public Drawable getGameImage() {
        return this.gameImage;
    }


    public Class getApp(){ return this.app; }

}

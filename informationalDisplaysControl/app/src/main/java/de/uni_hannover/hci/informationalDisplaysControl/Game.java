package de.uni_hannover.hci.informationalDisplaysControl;
import android.graphics.drawable.Drawable;


public class Game {
    private String gameName;
    private String gameDescription;
    private Drawable gameImage;

    public Game(String gameName, String gameDescription, Drawable image) {
        this.gameName = gameName;
        this.gameDescription = gameDescription;
        this.gameImage = image;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameDescription() {
        return gameDescription;
    }

    public void setGameDescription(String gameDescription) {
        this.gameDescription = gameDescription;
    }

    public Drawable getGameImage() {
        return gameImage;
    }

    public void setGameImage(Drawable gameImage) {
        this.gameImage = gameImage;
    }
}

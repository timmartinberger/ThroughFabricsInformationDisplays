package de.uni_hannover.hci.informationalDisplaysControl;

import android.media.Image;

public class Game {
    private String gameName;
    private String gameDescription;
    private Image gameImage;

    public Game(String gameName, String gameDescription, Image image) {
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

    public Image getGameImage() {
        return gameImage;
    }

    public void setGameImage(Image gameImage) {
        this.gameImage = gameImage;
    }
}

package de.uni_hannover.hci.informationalDisplaysControl.GameLogic;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;

import de.uni_hannover.hci.informationalDisplaysControl.baseData.DrawingColor;
import de.uni_hannover.hci.informationalDisplaysControl.baseData.Player;
import de.uni_hannover.hci.informationalDisplaysControl.baseData.Symbol;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEServiceInstance;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.Devices;

public class Dobble extends Thread {

    private final int numberOfPlayers;
    private final int MAX_SYMBOLS = 8;
    private final int CIRCLE_CODE = 33;
    private final int CROSS_Code = 34;
    private final int currentMaxSymbols;
    private final int rounds;
    final private ArrayList<Symbol> symbolList = new ArrayList<>();
    final private Random random = new Random();
    public volatile boolean buttonPressed = false;
    public ArrayList<String> deviceMacList = new ArrayList<>();
    public volatile String whoPressed = "";
    private String whoPressedForPoints;


    public Dobble(int rounds, int numberOfPlayers) {
        this.rounds = rounds;
        this.numberOfPlayers = numberOfPlayers;
        this.symbolList.addAll(Arrays.stream(Symbol.values()).collect(Collectors.toList()));
        this.currentMaxSymbols = generateCurrentMaxSymbols();
    }

    private int generateCurrentMaxSymbols() {
        int currentMaxSymbols = (this.symbolList.size()-1) / numberOfPlayers;
        return (currentMaxSymbols >= MAX_SYMBOLS) ?  7 : currentMaxSymbols;
    }

    /*
        Ablauf:
        1. doppeltes Symbol bestimmen
        2. Listen der Player mit max 7 Symbolen befüllen, in keiner Liste darf das gleiche vorkommen!
        3. jeder Liste das doppelte Symbol geben
        4. Symbole anzeigen lassen
        5. Durch Knopfdruck wird "gestoppt": auslösender Spieler wird durch Display hevorgehoben
        6. durch erneuten Knopfdruck erfolgt Prüfung - das richtige Symbol wird bei allen angezeigt
        7. Drückt Spieler den Knopf, so gibt es einen Punkt
        8. langes Drücken oder andere Spieler: Minuspunkt
        9. nächste Runde...
     */

    public void run() {
        ArrayList<ArrayList<Symbol>> playersSymbols;
        Symbol currentSymbol;

        initDeviceMacList(deviceMacList);
        ArrayList<Player> players = generatePlayerList();
        BLEServiceInstance.setControllerOptions(deviceMacList, "6", true);
        for(int i = 0; i < rounds; i++) {
            currentSymbol = getRoundSymbol();
            //init player display
            playersSymbols = getPlayersSymbols(currentSymbol);
            //show icons on display
            BLEServiceInstance.sendSymbolsToPlayers(playersSymbols, deviceMacList);
            try {
                waitForInput(300);
            } catch (InterruptedException e) {
                endGame();
                return;
            }
            decideWhoPressedFirst();
            try {
                waitForInput(300);
            } catch (InterruptedException e) {
                endGame();
                return;
            }
            BLEServiceInstance.sendSymbolsToPlayers(singleSymbolList(currentSymbol), deviceMacList);
            try {
                waitForInput(300);
            } catch (InterruptedException e) {
                endGame();
                return;
            }
            setPlayerPoints(players);
        }
        analyzePlayerPoints(players);
    }

    private void setPlayerPoints(ArrayList<Player> players) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException interruptedException) {
            Log.i("testbt", "Game stopped!");
        }
        Player player = null;
        for(Player p: players) {
            if(p.getAddress().equals(whoPressedForPoints)) {
                player = p;
            }
        }
        try {
            player.updatePoints(!whoPressed.equals(whoPressedForPoints));
        } catch (Exception e) {
            endGame();
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException interruptedException) {
            Log.i("testbt", "Game stopped!");
        }
    }

    private void analyzePlayerPoints(ArrayList<Player> players) {
        ArrayList<Player> highestPlayers = new ArrayList<>();
        int highscore = 0;
        for(Player player : players) {
            if(player.getPoints() > highscore) {
                highscore = player.getPoints();
            }
        }
        for(Player player : players) {
            if(player.getPoints() >= highscore) {
                highestPlayers.add(player);
            }
        }
        int highscoreColor;
        if(highestPlayers.size() > 1) {
            highscoreColor = DrawingColor.YELLOW.getColorCode();
        } else {
            highscoreColor = DrawingColor.GREEN.getColorCode();
        }
        for(Player player : players) {
            if(highestPlayers.contains(player)) {
                player.showPoints(highscoreColor);
            } else {
                player.showPoints(DrawingColor.RED.getColorCode());
            }
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException interruptedException) {
            Log.i("testbt", "Game stopped!");
        }
        endGame();
    }

    private ArrayList<Player> generatePlayerList() {
        ArrayList<Player> players = new ArrayList<>();
        for(String address: deviceMacList) {
            players.add(new Player(address));
        }
        return players;
    }

    private Symbol getRoundSymbol() {
        return symbolList.get(random.nextInt(symbolList.size()));
    }

    private void endGame() {
        Log.i("testbt", "Game stopped!");
        BLEServiceInstance.setControllerOptions(deviceMacList, "5", false);
    }

    private ArrayList<ArrayList<Symbol>> getPlayersSymbols(Symbol currentSymbol) {
        ArrayList<ArrayList<Symbol>>  playersSymbols = new ArrayList<>();
        ArrayList<Symbol> copySymbols = new ArrayList<>(symbolList);
        copySymbols.remove(currentSymbol);
        Symbol sym;
        for(int players = 0; players < numberOfPlayers; players++) {
            ArrayList<Symbol> symbols = new ArrayList<>();
            for (int i = 0; i < currentMaxSymbols; i++) {
                sym = copySymbols.get(random.nextInt(copySymbols.size()));
                copySymbols.remove(sym);
                symbols.add(sym);
            }
            symbols.add(currentSymbol);
            Collections.shuffle(symbols);
            playersSymbols.add(symbols);
        }
        return playersSymbols;
    }

    private void waitForInput(int waitDuration) throws InterruptedException {
        for(int i = 0; i < waitDuration; i++) {
            if(buttonPressed) {
                buttonPressed = false;
                return;
            }
            else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new InterruptedException();
                }
            }
        }
    }

    private ArrayList<ArrayList<Symbol>> singleSymbolList(Symbol symbol) {
        ArrayList<ArrayList<Symbol>>  playersSymbols = new ArrayList<>();
        for(int players = 0; players < numberOfPlayers; players++) {
            ArrayList<Symbol> symbols = new ArrayList<>();
            symbols.add(symbol);
            playersSymbols.add(symbols);
        }
        return playersSymbols;
    }

    private void initDeviceMacList(ArrayList<String> deviceMacList) {
        for(int deviceNr = 0; deviceNr < Devices.getDeviceCount(); deviceNr++) {
            deviceMacList.add(Devices.getMacAsString(deviceNr));
        }
    }

    private void decideWhoPressedFirst() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException interruptedException) {
            Log.i("testbt", "Game stopped!");
        }
        ArrayList<String> lastPressed = new ArrayList<>(deviceMacList);
        lastPressed.remove(whoPressed);
        ArrayList<String> firstPressed = new ArrayList<>();
        firstPressed.add(whoPressed);
        BLEServiceInstance.sendTurnCodeToPlayers(CIRCLE_CODE, firstPressed);
        whoPressedForPoints = whoPressed;
        whoPressed = "";
        BLEServiceInstance.sendTurnCodeToPlayers(CROSS_Code, lastPressed);
        try {
            Thread.sleep(500);
        } catch (InterruptedException interruptedException) {
            Log.i("testbt", "Game stopped!");
        }
    }
}

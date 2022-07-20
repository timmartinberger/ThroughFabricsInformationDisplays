package de.uni_hannover.hci.informationalDisplaysControl.GameLogic;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;

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
        BLEServiceInstance.setControllerOptions(deviceMacList, "6", true);
        //round loop
        for(int i = 0; i < rounds; i++) {
            currentSymbol = getRoundSymbol();
            //init player display
            playersSymbols = getPlayersSymbols(currentSymbol);
            //show icons on display
            BLEServiceInstance.sendSymbolsToPlayers(playersSymbols, deviceMacList);
            try {
                waitForInput(300);
                //Thread.sleep(10000);
            } catch (InterruptedException e) {
                endGame(deviceMacList);
                return;
            }
            //decide who pressed first
            decideWhoPressedFirst();
            try {
                waitForInput(300);
            } catch (InterruptedException e) {
                endGame(deviceMacList);
                return;
            }

            //send currentSymbol to all players
            BLEServiceInstance.sendSymbolsToPlayers(singleSymbolList(currentSymbol), deviceMacList);
            //get signal
            try {
                //Thread.sleep(10000);
                waitForInput(300);
            } catch (InterruptedException e) {
                endGame(deviceMacList);
                return;
                // button pressed, continue
            }
            //repeat game
        }
    }

    private Symbol getRoundSymbol() {
        return symbolList.get(random.nextInt(symbolList.size()));
    }

    private void endGame(ArrayList<String> deviceMacList) {
        System.out.println("Game stopped!!!");
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
                System.out.println("Button Pressed!");
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
    /*
    private void sendToPlayers(ArrayList<ArrayList<Symbol>> playersSymbols, ArrayList<String> deviceMacList) {
        int playerNr = 0;
        for(ArrayList<Symbol> list : playersSymbols) {
            //printSymbolArray(list, playerNr);
            try {
                byte[] data = getSymbolBytes(playersSymbols.get(playerNr));
                BLEServiceInstance.getBLEService().writeCharacteristic(deviceMacList.get(playerNr), BLEService.DATA_CHARACTERISTIC_UUID, data);
            } catch (Exception e) {
                System.out.println("Failed sending!");
            }
            playerNr++;
        }
    }*/
    /*
    private byte[] getSymbolBytes(ArrayList<Symbol> symbols) {
        byte[] data = new byte[symbols.size()];
        int counter = 0;
        for(Symbol symbol: symbols) {
            data[counter] = (byte)((symbol.getCode()+1) & 0xFF);
            counter++;
        }
        return data;
    }
    */

    /*
    private void setBTMode(ArrayList<String> deviceMacList, String mode, boolean enableButton) {
        for(String device: deviceMacList) {
            BLEServiceInstance.getBLEService().writeCharacteristic(device, BLEService.MODE_CHARACTERISTIC_UUID, mode);
            BLEServiceInstance.getBLEService().setCharacteristicNotification(device, BLEService.BUTTON_CHARACTERISTIC_UUID, enableButton);
        }
    }*/

    private void initDeviceMacList(ArrayList<String> deviceMacList) {
        for(int deviceNr = 0; deviceNr < Devices.getDeviceCount(); deviceNr++) {
            deviceMacList.add(Devices.getMacAsString(deviceNr));
        }
    }

    private void decideWhoPressedFirst() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException interruptedException) {
            System.out.println("Stopped!");
        }
        ArrayList<String> lastPressed = new ArrayList<>(deviceMacList);
        lastPressed.remove(whoPressed);
        ArrayList<String> firstPressed = new ArrayList<>();
        firstPressed.add(whoPressed);
        BLEServiceInstance.sendTurnCodeToPlayers(CIRCLE_CODE, firstPressed);
        whoPressed = "";
        BLEServiceInstance.sendTurnCodeToPlayers(CROSS_Code, lastPressed);
        try {
            Thread.sleep(500);
        } catch (InterruptedException interruptedException) {
            System.out.println("Stopped!");
        }
    }
}

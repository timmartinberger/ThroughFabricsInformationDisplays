package de.uni_hannover.hci.informationalDisplaysControl.GameLogic;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;

import de.uni_hannover.hci.informationalDisplaysControl.baseData.Symbol;

public class Dobble {

    private int numberOfPlayers;
    private final int MAX_SYMBOLS = 8;
    private int currentMaxSymbols;
    final private ArrayList<Symbol> symbolList = new ArrayList<>();
    final private Random random = new Random();


    public Dobble(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
        this.symbolList.addAll(Arrays.stream(Symbol.values()).collect(Collectors.toList()));
        this.currentMaxSymbols = generateCurrentMaxSymbols();
    }

    private int generateCurrentMaxSymbols() {
        int currentMaxSymbols = (this.symbolList.size()-1) / numberOfPlayers;
        return (currentMaxSymbols >= MAX_SYMBOLS) ?  7 : currentMaxSymbols;
    }

    public void startGame() {
        int rounds = 3;
        System.out.println("Number of symbols: " + symbolList.size());
        System.out.println("----------------------------");
        playingMatch(rounds);
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

    private void playingMatch(int rounds) {
        Symbol curentSymbol;
        ArrayList<ArrayList<Symbol>> playersSymbols;
        //ArrayList<Symbol> copySymbols;
        for(int i = 0; i < rounds; i++) {
            curentSymbol = getRoundSymbol();
            playersSymbols = getPlayersSymbols(curentSymbol);
            int player = 0;
            System.out.println("Round " + i + ":");
            for(ArrayList<Symbol> list : playersSymbols) {
                printSymbolArray(list, player);
                player++;
            }
            System.out.println("+++++++++++++++++++");

        }
    }

    private Symbol getRoundSymbol() {
        return symbolList.get(random.nextInt(symbolList.size()));
    }

    private ArrayList<ArrayList<Symbol>> getPlayersSymbols(Symbol curentSymbol) {

        ArrayList<ArrayList<Symbol>>  playersSymbols = new ArrayList<>();
        ArrayList<Symbol> copySymbols = new ArrayList<>(symbolList);
        copySymbols.remove(curentSymbol);
        Symbol sym;
        for(int players = 0; players < numberOfPlayers; players++) {
            ArrayList<Symbol> symbols = new ArrayList<>();
            for (int i = 0; i < currentMaxSymbols; i++) {
                sym = copySymbols.get(random.nextInt(copySymbols.size()));
                copySymbols.remove(sym);
                symbols.add(sym);
            }
            symbols.add(curentSymbol);
            Collections.shuffle(symbols);
            playersSymbols.add(symbols);
        }
        return playersSymbols;
    }

    private void printSymbolArray(ArrayList<Symbol> list, int player) {
        System.out.print("Player " + player + ": ");
        for(Symbol s: list) {
            System.out.print(s.name() + " ");
        }
        System.out.println();
    }
}

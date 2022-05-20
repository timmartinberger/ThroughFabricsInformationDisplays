package de.uni_hannover.hci.informationalDisplaysControl;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import de.uni_hannover.hci.informationalDisplaysControl.databinding.ActivityGameMenuBinding;

public class GameMenu extends AppCompatActivity {

    private ArrayList<Game> gameList;
    private RecyclerView RVGames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_menu);

        RVGames = findViewById(R.id.RVGames);

        // Create List of games
        gameList = new ArrayList<>();
        gameList.add(new Game("Who am I?", "This is a guessing game where players use yes or no questions to guess the identity of a famous person or fictional character.", null));
        gameList.add(new Game("Hot Potatoe", "", null));
        gameList.add(new Game("Tetris", "", null));

        GameAdapter gameAdapter = new GameAdapter(this, gameList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RVGames.setLayoutManager(linearLayoutManager);
        RVGames.setAdapter(gameAdapter);
        // Get cart of 'Who am I?' to set Listener on it
        RecyclerView.ViewHolder whoAmI = RVGames.findViewHolderForLayoutPosition(0);
         

    }
}
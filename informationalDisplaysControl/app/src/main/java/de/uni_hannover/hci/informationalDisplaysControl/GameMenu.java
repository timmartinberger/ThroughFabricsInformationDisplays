package de.uni_hannover.hci.informationalDisplaysControl;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class GameMenu extends AppCompatActivity {

    private ArrayList<Game> gameList;
    private RecyclerView RVGames;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_menu);

        RVGames = findViewById(R.id.RVGames);

        // Create List of games
        // TODO: Create the games here
        gameList = new ArrayList<>();
        // For API versions >= 21 with background image
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            gameList.add(new Game(getString(R.string.who_am_i), "This is a guessing game where players use yes or no questions to guess the identity of a famous person or fictional character.", getDrawable(R.drawable.whoami_darker)));
            gameList.add(new Game(getString(R.string.hot_potatoe), "", null));
            gameList.add(new Game("Tetris", "", null));
        }
        // For API versions < 21 WITHOUT background image
        else {
            gameList.add(new Game(getString(R.string.who_am_i), "This is a guessing game where players use yes or no questions to guess the identity of a famous person or fictional character.", null));
            gameList.add(new Game(getString(R.string.hot_potatoe), "", null));
            gameList.add(new Game("Tetris", "", null));
        }

        GameAdapter gameAdapter = new GameAdapter(this, gameList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RVGames.setLayoutManager(linearLayoutManager);
        RVGames.setAdapter(gameAdapter);

    }
}
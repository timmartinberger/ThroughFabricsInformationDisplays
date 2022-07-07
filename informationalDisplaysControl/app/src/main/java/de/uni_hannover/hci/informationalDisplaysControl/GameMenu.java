package de.uni_hannover.hci.informationalDisplaysControl;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import de.uni_hannover.hci.informationalDisplaysControl.GameLogic.*;


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

        gameList.add(new Game(getString(R.string.who_am_i), "This is a guessing game where players use yes or no questions to guess the identity of a famous person or fictional character.", getDrawable(R.drawable.whoami_darker), WhoAmI.class));
        gameList.add(new Game(getString(R.string.hot_pixels), "", null, DobbleController.class));
        gameList.add(new Game(getString(R.string.drawing_guessing), "", null, null));
        gameList.add(new Game(getString(R.string.four_wins), "", null, null));
        gameList.add(new Game(getString(R.string.send_text), "Send a any text to the LED Matrices", null, SendText.class));
        gameList.add(new Game("BLETest", "", null, BLETestActivity.class));

        GameAdapter gameAdapter = new GameAdapter(this, gameList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        RVGames.setLayoutManager(linearLayoutManager);
        RVGames.setAdapter(gameAdapter);

    }
}
package de.uni_hannover.hci.informationalDisplaysControl;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.Devices;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.Viewholder> {

    private Context context;
    private ArrayList<Game> gameList;

    // Constructor
    public GameAdapter(Context context, ArrayList<Game> courseModelArrayList) {
        this.context = context;
        this.gameList = courseModelArrayList;
    }


    @NonNull
    @Override
    public GameAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // to inflate the layout for each item of recycler view.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_game_item, parent, false);
        return new Viewholder(view);
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull GameAdapter.Viewholder holder, int position) {
        // to set data to textview and imageview of each card layout
        Game model = gameList.get(position);
        holder.gameTitle.setText(model.getGameName());
        holder.gameImage.setImageDrawable(model.getGameImage());
    }

    @Override
    public int getItemCount() {
        // this method is used for showing number
        // of card items in recycler view.
        return gameList.size();
    }

    // View holder class for initializing of
    // your views such as TextView and Imageview.
    public class Viewholder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView gameImage;
        private TextView gameTitle, gameDescription;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            gameTitle = itemView.findViewById(R.id.gameTitle);
            gameImage = itemView.findViewById(R.id.gameImage);
            gameDescription = itemView.findViewById(R.id.gameDescription);
            gameDescription.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onClick(View view) {
            int pos = this.getAdapterPosition();
            Intent intent = new Intent(context, gameList.get(pos).getApp());
            context.startActivity(intent);
        }
    }
}

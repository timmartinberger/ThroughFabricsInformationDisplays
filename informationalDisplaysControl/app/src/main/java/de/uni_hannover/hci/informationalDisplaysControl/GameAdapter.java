package de.uni_hannover.hci.informationalDisplaysControl;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

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
        holder.gameDescription.setText(model.getGameDescription());
        holder.gameImage.setImageResource(0);
        holder.gameDescription.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
    }

    @Override
    public int getItemCount() {
        // this method is used for showing number
        // of card items in recycler view.
        return gameList.size();
    }

    // View holder class for initializing of
    // your views such as TextView and Imageview.
    public class Viewholder extends RecyclerView.ViewHolder {
        private ImageView gameImage;
        private TextView gameTitle, gameDescription;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            gameTitle = itemView.findViewById(R.id.gameTitle);
            gameDescription = itemView.findViewById(R.id.gameDescription);
            gameImage = itemView.findViewById(R.id.gameImage);
        }
    }
}

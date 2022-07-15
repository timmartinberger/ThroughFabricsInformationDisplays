package de.uni_hannover.hci.informationalDisplaysControl.GameLogic;

import androidx.appcompat.app.AppCompatActivity;
import de.uni_hannover.hci.informationalDisplaysControl.R;


import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;

public class MontagsMalerController extends AppCompatActivity {

    private LinearLayout colorBar;
    private TableLayout table;
    private ArrayList<Integer> colorList = new ArrayList<>();
    private int selectedColor = Color.WHITE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_montags_maler_controller);
        colorBar = findViewById(R.id.colorBar);
        table = findViewById(R.id.table);

        initColorList();

        initTable();
        initColors();

        final ImageView colorObjectBlue = generateColorPickerElement(Color.BLUE);
        //colorBar.addView(colorObjectBlue);


    }



    private ImageView generateColorPickerElement(int color) {
        ImageView colorObject = new ImageView(this);
        colorObject.setImageDrawable(getDrawable(R.drawable.color_object));
        colorObject.setColorFilter(color);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(16,16,16,16);
        colorObject.setLayoutParams(params);
        colorObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorObject.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), android.R.anim.fade_in));
                selectedColor = color;
            }
        });
        return colorObject;
    }

    private ImageView generatePixelElement() {
        ImageView pixelObject = new ImageView(this);
        pixelObject.setImageDrawable(getDrawable(R.drawable.pixel_object));
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(4,4,4,4);
        pixelObject.setLayoutParams(params);
        pixelObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pixelObject.setColorFilter(selectedColor);
            }
        });
        return pixelObject;
    }

    private void initTable() {
        for(int i = 0; i < 10; i++) {
            TableRow row = new TableRow(this);
            for(int j = 0; j < 10; j++) {
                row.addView(generatePixelElement());
            }
            table.addView(row);
        }
    }

    private void initColors() {
        ArrayList<Integer> copyColors = new ArrayList<>(this.colorList);
        for(int i = 0; i < 2; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            row.setOrientation(LinearLayout.HORIZONTAL);
            for(int j = 0; j < 5; j++) {
                row.addView(generateColorPickerElement(copyColors.get(0)));
                copyColors.remove(0);
            }
            colorBar.addView(row);
        }
    }

    private void initColorList() {
        this.colorList.add(Color.BLUE);
        this.colorList.add(Color.RED);
        this.colorList.add(Color.GREEN);
        this.colorList.add(Color.GRAY);
        this.colorList.add(Color.YELLOW);
        this.colorList.add(Color.MAGENTA);
        this.colorList.add(Color.CYAN);
        this.colorList.add(Color.WHITE);
        this.colorList.add(Color.WHITE);
        this.colorList.add(Color.WHITE);
        this.colorList.add(Color.WHITE);
        this.colorList.add(Color.WHITE);
    }

}
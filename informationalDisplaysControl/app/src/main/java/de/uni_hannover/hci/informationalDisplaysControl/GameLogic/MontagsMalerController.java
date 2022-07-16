package de.uni_hannover.hci.informationalDisplaysControl.GameLogic;

import androidx.appcompat.app.AppCompatActivity;
import de.uni_hannover.hci.informationalDisplaysControl.R;
import de.uni_hannover.hci.informationalDisplaysControl.baseData.DrawingColor;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEService;
import de.uni_hannover.hci.informationalDisplaysControl.bluetoothControl.BLEServiceInstance;


import android.graphics.Color;
import android.graphics.ColorFilter;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

public class MontagsMalerController extends AppCompatActivity {

    private LinearLayout colorBar;
    private TableLayout table;
    private ImageButton fillButton;
    private final ArrayList<DrawingColor> colorList = new ArrayList<>();
    private DrawingColor selectedColor = DrawingColor.WHITE;
    private TextView toBeDrawn;
    private final ArrayList<DrawAction> actionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_montags_maler_controller);
        colorBar = findViewById(R.id.colorBar);
        table = findViewById(R.id.table);
        fillButton = findViewById(R.id.fillBoardButton);
        toBeDrawn = findViewById(R.id.toDrawTextView);

        initColorList();

        initTable();
        initColors();

        BLEServiceInstance.getBLEService().writeCharacteristicToAll(BLEService.MODE_CHARACTERISTIC_UUID, "4");

        toBeDrawn.setText("Pepe");
        toBeDrawn.setTextSize(24);
    }



    private ImageView generateColorPickerElement(DrawingColor color) {
        ImageView colorObject = new ImageView(this);
        colorObject.setImageDrawable(getDrawable(R.drawable.color_object));
        colorObject.setColorFilter(color.getColorValue());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(16,16,16,16);
        colorObject.setLayoutParams(params);
        colorObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorObject.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), android.R.anim.fade_in));
                selectedColor = color;
                fillButton.setColorFilter(selectedColor.getColorValue());
            }
        });
        return colorObject;
    }

    private ImageView generatePixelElement(DrawAction action) {
        ImageView pixelObject = new ImageView(this);
        pixelObject.setImageDrawable(getDrawable(R.drawable.pixel_object));
        pixelObject.setPadding(4, 4, 4, 4);
        pixelObject.setTag(action);
        pixelObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionList.add(action);
                pixelObject.setColorFilter(selectedColor.getColorValue());
                notifyBT(action);
            }
        });
        return pixelObject;
    }

    private void initTable() {
        for(int i = 0; i < 10; i++) {
            TableRow row = new TableRow(this);
            for(int j = 0; j < 10; j++) {
                row.addView(generatePixelElement(new DrawAction(new Pair<Integer, Integer>(i,j), null, DrawingColor.WHITE)));
            }
            table.addView(row);
        }
    }

    private void initColors() {
        ArrayList<DrawingColor> copyColors = new ArrayList<>(this.colorList);
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
        this.colorList.add(DrawingColor.WHITE);
        this.colorList.add(DrawingColor.LTBLUE);
        this.colorList.add(DrawingColor.RED);
        this.colorList.add(DrawingColor.ORANGE);
        this.colorList.add(DrawingColor.YELLOW);

        this.colorList.add(DrawingColor.DKGRAY);
        this.colorList.add(DrawingColor.GREEN);
        this.colorList.add(DrawingColor.BLUE);
        this.colorList.add(DrawingColor.MAGENTA);
        this.colorList.add(DrawingColor.BROWN);

    }

    public void clearBoard(View view) {
        fillBoardWithColor(Color.WHITE);
    }

    public void revertAction(View view) {
        int last = actionList.size()-1;
        if(last < 0) {
            return;
        }
        DrawAction action = actionList.get(last);
        actionList.remove(last);
        if(action.position == null && action.boardState.length == 100) {
            resetBoardState(action.boardState);
        }
        else {
            TableRow row = (TableRow) table.getChildAt(action.position.first);
            ImageView pixel = (ImageView) row.getChildAt(action.position.second);
            pixel.setColorFilter(action.color.getColorValue());
        }
        notifyBT(action);
    }

    private void resetBoardState(DrawingColor[] boardState) {
        for(int i = 0; i < table.getChildCount(); i++) {
            TableRow row = (TableRow) table.getChildAt(i);
            for(int j = 0; j < row.getChildCount(); j++) {
                ImageView pixel = (ImageView) row.getChildAt(j);
                pixel.setColorFilter(boardState[10*i +j].getColorValue());
            }
        }
    }

    public void fillBoard(View view) {
        actionList.add(new DrawAction(null, getBoardState(), null));
        fillBoardWithColor(selectedColor.getColorValue());
    }

    private void fillBoardWithColor(int color) {
        for(int i = 0; i < table.getChildCount(); i++) {
            TableRow row = (TableRow) table.getChildAt(i);
            for(int j = 0; j < row.getChildCount(); j++) {
                ImageView pixel = (ImageView) row.getChildAt(j);
                pixel.setColorFilter(color);
            }
        }
    }

    private DrawingColor[] getBoardState() {
        DrawingColor[] boardState = new DrawingColor[100];
        for(int i = 0; i < table.getChildCount(); i++) {
            TableRow row = (TableRow) table.getChildAt(i);
            for(int j = 0; j < row.getChildCount(); j++) {
                ImageView pixel = (ImageView) row.getChildAt(j);
                DrawAction action = (DrawAction)pixel.getTag();
                boardState[i*10 + j] = action.color;
            }
        }
        return boardState;
    }

    public static class DrawAction {
        public Pair<Integer, Integer> position;
        public DrawingColor color;
        public DrawingColor[] boardState;
        public DrawAction(Pair<Integer, Integer> position, DrawingColor[] boardState, DrawingColor color) {
            this.position = position;
            this.boardState = boardState;
            this.color = color;
        }
    }

    private void notifyPixel(DrawAction action) {
        byte bytePosX = (byte)(action.position.first & 0xFF);
        byte bytePosy = (byte)(action.position.first & 0xFF);
        byte byteColor = (byte)(action.color.getColorCode() & 0xFF);
    }

    private void notifyBoard() {

    }

    private void notifyBT(DrawAction action) {
        if(action.boardState != null && action.boardState.length == 100) {
            notifyBoard();
        }
        else {
            notifyPixel(action);
        }
    }
}
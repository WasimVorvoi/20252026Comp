package com.example.connect67;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.Color;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    int cols = 6;
    int rows = 7;
    TextView[] textViews = new TextView[cols];
    Button[][] buttons = new Button[rows][cols];
    int col = 0;
    boolean redTurn = true;
    boolean over = false;
    TextView turn;
    Button restart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for (int i = 0; i < textViews.length; i++) {
            String id = "textView" + (i + 16);
            int res = getResources().getIdentifier(id, "id", getPackageName());
            textViews[i] = findViewById(res);
        }
        turn = findViewById(R.id.textView3);
        restart = findViewById(R.id.buttonRestart);
        restart.setEnabled(false);
        restart.setAlpha(0.4f);
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetBoard();
            }
        });
        int num = 4;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                String id = "button" + num;
                int res = getResources().getIdentifier(id, "id", getPackageName());
                buttons[r][c] = findViewById(res);
                num++;
            }
        }
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Button b = buttons[r][c];
                b.setTag("empty");
                b.setBackgroundColor(Color.WHITE);
            }
        }
        updateIndicator();
        Button left = findViewById(R.id.buttonLeft);
        Button right = findViewById(R.id.buttonRight);
        Button drop = findViewById(R.id.buttonDrop);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (over) return;
                try {
                    if (col > 0) {
                        col--;
                        updateIndicator();
                        if (redTurn) {
                            turn.setText("Turn: Player 2");
                        } else {
                            turn.setText("Turn: Player 1");
                        }
                    } else {
                        if(redTurn){
                            Toast.makeText(MainActivity.this, "Player 2: Out of bounds", Toast.LENGTH_SHORT).show();
                            turn.setText("Player 2: Out of bounds");
                        }
                        else{
                            Toast.makeText(MainActivity.this, "Player 1: Out of bounds", Toast.LENGTH_SHORT).show();
                            turn.setText("Player 1: Out of bounds");
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    turn.setText("Player turn: Exception");
                }
            }
        });
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (over) return;
                try {
                    if (col < cols - 1) {
                        col++;
                        updateIndicator();
                        if (redTurn) {
                            turn.setText("Turn: Player 2");
                        } else{
                            turn.setText("Turn: Player 1");
                        }
                    }
                    else {
                        if(redTurn){
                            Toast.makeText(MainActivity.this, "Player 2: Out of bounds", Toast.LENGTH_SHORT).show();
                            turn.setText("Player 2: Out of bounds");
                        }
                        else{
                            Toast.makeText(MainActivity.this, "Player 1: Out of bounds", Toast.LENGTH_SHORT).show();
                            turn.setText("Player 1: Out of bounds");
                        }
                    }
                } catch(ArrayIndexOutOfBoundsException e) {
                    turn.setText("Player turn: Exception");
                }
            }
        });
        drop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (over) return;
                try {
                    boolean placed = false;
                    int placedRow = -1;
                    String tagPlaced = null;
                    for (int r = rows - 1; r >= 0; r--) {
                        Button b = buttons[r][col];
                        Object tag = b.getTag();
                        if ("empty".equals(tag)) {
                            if (redTurn) {
                                b.setBackgroundColor(Color.RED);
                                b.setTag("red");
                                turn.setText("Turn: Player 2");
                                tagPlaced = "red";
                            } else {
                                b.setBackgroundColor(Color.YELLOW);
                                b.setTag("yellow");
                                turn.setText("Turn: Player 1");
                                tagPlaced = "yellow";
                            }
                            redTurn = !redTurn;
                            placed = true;
                            placedRow = r;
                            break;
                        }
                    }
                    if (!placed) {
                        Toast.makeText(MainActivity.this, "Column is full", Toast.LENGTH_SHORT).show();
                        if (redTurn) {
                            turn.setText("Column full! Player 1");
                        } else {
                            turn.setText("Column full! Player 2");
                        }
                    }
                    else{
                        String winner = checkWinner();
                        if(winner.equals("red") || winner.equals("yellow")){
                            over = true;
                            String name = "";
                            if(winner.equals("red")){
                                name = "Player 1";
                            }
                            else{
                                name = "Player 2";
                            }
                            turn.setText("Winner " + name);
                            Toast.makeText(MainActivity.this, name + " wins", Toast.LENGTH_LONG).show();
                            Button left = findViewById(R.id.buttonLeft);
                            Button right = findViewById(R.id.buttonRight);
                            Button drop = findViewById(R.id.buttonDrop);
                            left.setEnabled(false);
                            right.setEnabled(false);
                            drop.setEnabled(false);
                            restart.setEnabled(true);
                            restart.setAlpha(1f);
                        }
                        else if(winner.equals("tie")){
                            over = true;
                            String name = "";
                            if(winner.equals("red")){
                                name = "Player 1";
                            }
                            else{
                                name = "Player 2";
                            }
                            turn.setText("Winner " + name);
                            Toast.makeText(MainActivity.this, name + " wins", Toast.LENGTH_LONG).show();
                            Button left = findViewById(R.id.buttonLeft);
                            Button right = findViewById(R.id.buttonRight);
                            Button drop = findViewById(R.id.buttonDrop);
                            left.setEnabled(false);
                            right.setEnabled(false);
                            drop.setEnabled(false);
                            restart.setEnabled(true);
                            restart.setAlpha(1f);
                        }
                    }
                    updateIndicator();
                } catch (Exception e) {
                    turn.setText("Player turn: Exception");
                }
            }
        });
    }
    private String checkWinner(){
        boolean full = true;
        for(int r = 0; r<rows; r++){
            for(int c = 0; c<cols; c++){
                Button b = buttons[r][c];
                String tag;
                if(b.getTag() != null) {
                    tag = b.getTag().toString();
                }
                else{
                    tag = "empty";
                }
                if(tag.equals("empty")){
                    full = false;
                }
                if(tag.equals("red") || tag.equals("yellow")){
                    if(c+3 < cols){
                        String t1 = val(r, c+1);
                        String t2 = val(r, c+2);
                        String t3 = val(r, c+3);
                        if(tag.equals(t1) && tag.equals(t2) && tag.equals(t3)){
                            return tag;
                        }
                    }
                    if(r+3 < cols){
                        String t1 = val(r+1,c);
                        String t2 = val(r+2,c);
                        String t3 = val(r+3,c);
                        if(tag.equals(t1) && tag.equals(t2) && tag.equals(t3)){
                            return tag;
                        }
                    }
                    if((c+3 < cols) && (r+3 <rows)){
                        String t1 = val(r+1, c+1);
                        String t2 = val(r+2, c+2);
                        String t3 = val(r+3, c+3);
                        if(tag.equals(t1) && tag.equals(t2) && tag.equals(t3)){
                            return tag;
                        }
                    }
                    if((r+3 <rows) && (c-3 >=0)){
                        String t1 = val(r+1,c-1);
                        String t2 = val(r+2,c-2);
                        String t3 = val(r+3,c-3);
                        if(tag.equals(t1) && tag.equals(t2) && tag.equals(t3)){
                            return tag;
                        }
                    }
                }
            }
        }
        if(full){
            return "tie";
        }
        return "none";
    }
    private void updateIndicator() {
        for (int i = 0; i < textViews.length; i++) {
            TextView t = textViews[i];
                if (!over && i == col) {
                    t.setText("v");
                } else {
                    t.setText("");
                }
        }
    }
    public void onButtonClick(View v) {
        try {
            if (over) return;
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (buttons[r][c] == v) {
                        col = c;
                        updateIndicator();
                        Button drop = findViewById(R.id.buttonDrop);
                        drop.performClick();
                        return;
                    }
                }
            }
        } catch (Exception e) {
            turn.setText("Player turn: Exception");
        }
    }

    

    private String val(int r, int c) {
        Button b = buttons[r][c];
        return b.getTag().toString();
    }

    private void resetBoard() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Button b = buttons[r][c];
                b.setBackgroundColor(Color.WHITE);
                b.setTag("empty");
            }
        }
        over = false;
        redTurn = true;
        col = 0;
        updateIndicator();
        Button left = findViewById(R.id.buttonLeft);
        Button right = findViewById(R.id.buttonRight);
        Button drop = findViewById(R.id.buttonDrop);
        left.setEnabled(true); left.setAlpha(1f);
        right.setEnabled(true); right.setAlpha(1f);
        drop.setEnabled(true); drop.setAlpha(1f);
        restart.setEnabled(false);
        restart.setAlpha(0.4f);
        turn.setText("Turn: Player 1");
    }
}   
package com.example.miltimedia;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<Card> cardList;
    CardAdapter adapter;
    ListView listView;
    View detailsContainer;
    ImageView selectedCardImage;
    TextView selectedCardName;
    TextView selectedCardInfo;
    TextView selectedCardElixir;
    TextView selectedCardTarget;
    TextView selectedCardSpeed;
    RatingBar selectedCardRating;
    TextView emptyMessage;
    int selectedPosition = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.cardListView);
        detailsContainer = findViewById(R.id.detailsScrollView);
        selectedCardImage = findViewById(R.id.selectedCardImage);
        selectedCardName = findViewById(R.id.selectedCardName);
        selectedCardElixir = findViewById(R.id.selectedCardElixir);
        selectedCardTarget = findViewById(R.id.selectedCardTarget);
        selectedCardSpeed = findViewById(R.id.selectedCardSpeed);
        selectedCardRating = findViewById(R.id.selectedCardRating);
        emptyMessage = findViewById(R.id.emptyMessage);
        cardList = new ArrayList<>();
        if (savedInstanceState != null && savedInstanceState.containsKey("cardNames")) {
            String[] savedCardNames = savedInstanceState.getStringArray("cardNames");
            int[] savedCardElixir = savedInstanceState.getIntArray("cardElixir");
            String[] savedCardTarget = savedInstanceState.getStringArray("cardTarget");
            String[] savedCardSpeed = savedInstanceState.getStringArray("cardSpeed");
            float[] savedCardRating = savedInstanceState.getFloatArray("cardRating");
            int[] savedCardImages = savedInstanceState.getIntArray("cardImages");
            if (savedCardNames != null && savedCardElixir != null && savedCardTarget != null &&
                savedCardSpeed != null && savedCardRating != null && savedCardImages != null) {
                for (int i = 0; i < savedCardNames.length; i++) {
                    cardList.add(new Card(savedCardNames[i], savedCardElixir[i], savedCardTarget[i], savedCardSpeed[i], savedCardRating[i], savedCardImages[i]));
                }
            }
        } else {
            cardList.add(new Card("Ice Spirit", 1, "Ground & Air", "Very Fast", 4.0f, R.drawable.ice));
            cardList.add(new Card("Fire Spirit", 1, "Ground & Air", "Very Fast", 4.0f, R.drawable.fire));
            cardList.add(new Card("Knight", 3, "Ground", "Medium", 4.5f, R.drawable.knight));
            cardList.add(new Card("Wall Breakers", 2, "Buildings", "Very Fast", 3.5f, R.drawable.wall));
            cardList.add(new Card("Skele Barrel", 3, "Buildings", "Medium", 3.5f, R.drawable.skelebarr));
            cardList.add(new Card("Hog Rider", 4, "Buildings", "Very Fast", 5.0f, R.drawable.hogrider));
            cardList.add(new Card("Mini P.E.K.K.A", 4, "Ground", "Fast", 4.5f, R.drawable.minipekka));
            cardList.add(new Card("Royal Ghost", 3, "Ground", "Medium", 4.0f, R.drawable.royal));
            cardList.add(new Card("P.E.K.K.A", 7, "Ground", "Slow", 4.5f, R.drawable.pekka));
            cardList.add(new Card("Mega Knight", 7, "Ground", "Medium", 4.0f, R.drawable.megaknight));
        }
        adapter = new CardAdapter(this, cardList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPosition = position;
                displayCardDetails(position);
            }
        });
        if (savedInstanceState != null) {
            selectedPosition = savedInstanceState.getInt("selectedPosition", -1);
            if (selectedPosition != -1 && selectedPosition < cardList.size()) {
                displayCardDetails(selectedPosition);
            }
        }
    }
    public void displayCardDetails(int position) {
        if (position < 0 || position >= cardList.size()) {
            return;
        }
        try {
            Card card = cardList.get(position);
            if (emptyMessage != null) {
                emptyMessage.setVisibility(View.GONE);
            }
            if (detailsContainer != null) {
                detailsContainer.setVisibility(View.VISIBLE);
            }
            if (selectedCardImage != null) {
                selectedCardImage.setImageResource(card.getImageResourceId());
            }
            if (selectedCardName != null) {
                selectedCardName.setText(card.getName());
            }
            if (selectedCardElixir != null) {
                selectedCardElixir.setText(String.valueOf(card.getElixir()));
            }
            if (selectedCardTarget != null) {
                selectedCardTarget.setText(card.getTarget());
            }
            if (selectedCardSpeed != null) {
                selectedCardSpeed.setText(card.getSpeed());
            }
            if (selectedCardRating != null) {
                selectedCardRating.setRating(card.getRating());
            }
        } catch (Exception e) {
        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectedPosition", selectedPosition);
        String[] cardNames = new String[cardList.size()];
        int[] cardElixir = new int[cardList.size()];
        String[] cardTarget = new String[cardList.size()];
        String[] cardSpeed = new String[cardList.size()];
        float[] cardRating = new float[cardList.size()];
        int[] cardImages = new int[cardList.size()];
        for (int i = 0; i < cardList.size(); i++) {
            Card card = cardList.get(i);
            cardNames[i] = card.getName();
            cardElixir[i] = card.getElixir();
            cardTarget[i] = card.getTarget();
            cardSpeed[i] = card.getSpeed();
            cardRating[i] = card.getRating();
            cardImages[i] = card.getImageResourceId();
        }
        outState.putStringArray("cardNames", cardNames);
        outState.putIntArray("cardElixir", cardElixir);
        outState.putStringArray("cardTarget", cardTarget);
        outState.putStringArray("cardSpeed", cardSpeed);
        outState.putFloatArray("cardRating", cardRating);
        outState.putIntArray("cardImages", cardImages);
    }
}
package com.example.miltimedia;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

public class CardAdapter extends ArrayAdapter<Card> {
    Context context;
    ArrayList<Card> cards;
    public CardAdapter(Context context, ArrayList<Card> cards) {
        super(context, 0, cards);
        this.context = context;
        this.cards = cards;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;

        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.list_item_card, parent, false);
        }
        Card currentCard = getItem(position);
        ImageView cardImage = listItemView.findViewById(R.id.cardImage);
        TextView cardName = listItemView.findViewById(R.id.cardName);
        TextView cardElixir = listItemView.findViewById(R.id.cardElixir);
        RatingBar cardRating = listItemView.findViewById(R.id.cardRating);
        Button removeButton = listItemView.findViewById(R.id.removeButton);
        cardImage.setImageResource(currentCard.getImageResourceId());
        cardName.setText(currentCard.getName());
        cardElixir.setText("Elixir: " + currentCard.getElixir());
        cardRating.setRating(currentCard.getRating());

        cardRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser) {
                    currentCard.setRating(rating);
                }
            }
        });
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cards.remove(position);
                notifyDataSetChanged();
            }
        });
        return listItemView;
    }
}

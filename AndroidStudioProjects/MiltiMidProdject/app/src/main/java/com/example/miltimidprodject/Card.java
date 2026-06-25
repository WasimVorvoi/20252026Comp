package com.example.miltimidprodject;


public class Card {
    private String name;
    private int elixir;
    private String target;
    private String speed;
    private float rating;
    private int imageResourceId;

    public Card(String name, int elixir, String target, String speed, float rating, int imageResourceId) {
        this.name = name;
        this.elixir = elixir;
        this.target = target;
        this.speed = speed;
        this.rating = rating;
        this.imageResourceId = imageResourceId;
    }

    public String getName() {
        return name;
    }

    public int getElixir() {
        return elixir;
    }

    public String getTarget() {
        return target;
    }

    public String getSpeed() {
        return speed;
    }

    public float getRating() {
        return rating;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
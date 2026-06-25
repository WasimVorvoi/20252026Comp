package com.example.miltimedia;

public class Card {
    String name;
    int elixir;
    String target;
    String speed;
    float rating;
    int imageResourceId;

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

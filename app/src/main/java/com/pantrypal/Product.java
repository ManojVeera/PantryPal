package com.pantrypal; // Use your package name

import com.google.gson.annotations.SerializedName;

public class Product {
    // Maps the "title" field from the API to our "name" variable
    @SerializedName("title")
    private String name;

    // The API uses "description", which matches our variable name
    @SerializedName("description")
    private String description;

    // The API uses "price", which matches our variable name
    @SerializedName("price")
    private double price; // Changed to double to handle decimal prices from API

    // Maps the "image" field from the API to our "imageUrl" variable
    @SerializedName("image")
    private String imageUrl;

    @SerializedName("category")
    private String category;

    // Note: A constructor is not strictly needed when using Gson,
    // but it can be useful for testing.

    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getCategory() { return category; }
}
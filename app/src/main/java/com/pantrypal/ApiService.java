package com.pantrypal; // Use your package name

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    // This defines the GET request to the 'products' endpoint
    @GET("products")
    Call<List<Product>> getProducts();
}
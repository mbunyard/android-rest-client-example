package com.mbunyard.rest_client_example.rest.service;

import com.mbunyard.rest_client_example.rest.model.StoryListingResponse;

import retrofit.Call;
import retrofit.http.GET;

/**
 * Defines reddit Listings REST API as a Java interface.
 */
public interface ListingsService {

    // Gets a list of stories from network.
    @GET("r/all/new.json")
    Call<StoryListingResponse> getStories();
}
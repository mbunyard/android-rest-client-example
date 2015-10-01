package com.mbunyard.rest_client_example.rest.service;

import com.mbunyard.rest_client_example.rest.model.StoryListingResponse;

import retrofit.http.GET;

/**
 * Defines reddit Listings REST API as a Java interface.
 */
public interface ListingsService {

    /**
     * Gets a list of reddit stories tagged "Funny".
     */
    @GET("/r/funny.json")
    StoryListingResponse listFunnyStories();
}
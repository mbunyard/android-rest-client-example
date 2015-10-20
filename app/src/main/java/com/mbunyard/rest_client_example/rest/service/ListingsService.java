package com.mbunyard.rest_client_example.rest.service;

import com.mbunyard.rest_client_example.rest.model.StoryListingResponse;

import retrofit.Callback;
import retrofit.http.GET;

/**
 * Defines reddit Listings REST API as a Java interface.
 */
public interface ListingsService {

    String URL = "/r/funny.json";

    // ----- Synchronous methods -----

    /**
     * Makes network request on calling/main thread and returns a list of stories.
     */
    @GET(URL)
    StoryListingResponse getStories();

    // ----- Asynchronous methods -----

    /**
     * Makes network request on background thread and returns a list of stories.
     */
    @GET(URL)
    void getStories(Callback<StoryListingResponse> callback);

}
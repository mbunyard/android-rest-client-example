package com.mbunyard.rest_client_example.rest;

import com.mbunyard.rest_client_example.rest.service.ListingsService;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Provides interface for efficient use of Retrofit library and defined reddit web services.
 */
public class RedditRestAdapter {

    /**
     * reddit web service endpoint URL.
     */
    private static final String ENDPOINT_URL = "https://www.reddit.com";

    /**
     * Create single Retrofit RestAdapter instance for efficient reuse.
     */
    private static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(ENDPOINT_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    /**
     * Create single ListingsService instance for efficient reuse.
     */
    private static final ListingsService LISTINGS_SERVICE = retrofit.create(ListingsService.class);

    /**
     * Private constructor to prevent external instantiation.
     */
    private RedditRestAdapter() {
    }

    /**
     * Returns instance of ListingsService for use in obtaining resources/stories from network.
     */
    public static ListingsService getListingsService() {
        return LISTINGS_SERVICE;
    }
}
package com.mbunyard.rest_client_example.rest.model;

import com.google.gson.annotations.SerializedName;

/**
 * Network response model.
 */
public class Story {

    @SerializedName("data")
    private StoryDetails storyDetails;

    public StoryDetails getStoryDetails() {
        return storyDetails;
    }

    public void setStoryDetails(StoryDetails storyDetails) {
        this.storyDetails = storyDetails;
    }
}
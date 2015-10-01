package com.mbunyard.rest_client_example.rest.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Network response model.
 */
public class StoryListingData {

    @SerializedName("children")
    private List<Story> stories;

    public List<Story> getStories() {
        return stories;
    }

    public void setStories(List<Story> stories) {
        this.stories = stories;
    }
}

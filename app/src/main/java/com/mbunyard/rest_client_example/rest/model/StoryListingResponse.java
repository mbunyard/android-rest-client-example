package com.mbunyard.rest_client_example.rest.model;

import android.content.ContentValues;

import com.mbunyard.rest_client_example.provider.StoryContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Network response model.
 */
public class StoryListingResponse {

    private StoryListingData data;

    public StoryListingData getData() {
        return data;
    }

    public void setData(StoryListingData data) {
        this.data = data;
    }

    /**
     * Utility method to transform collection of Stories to array of ContentValues.
     */
    public ContentValues[] getStoryContentValues() {
        List<ContentValues> contentValues = new ArrayList<ContentValues>();
        for (Story story : getData().getStories()) {
            ContentValues storyValues = new ContentValues();
            storyValues.put(StoryContract.Story.ID, story.getStoryDetails().getId());
            storyValues.put(StoryContract.Story.TITLE, story.getStoryDetails().getTitle());
            storyValues.put(StoryContract.Story.AUTHOR, story.getStoryDetails().getAuthor());
            storyValues.put(StoryContract.Story.URL, story.getStoryDetails().getUrl());
            storyValues.put(StoryContract.Story.PERMALINK, story.getStoryDetails().getPermalink());
            storyValues.put(StoryContract.Story.THUMBNAIL, story.getStoryDetails().getThumbnail());
            storyValues.put(StoryContract.Story.SCORE, story.getStoryDetails().getScore());
            storyValues.put(StoryContract.Story.PUBLISHED, story.getStoryDetails().getPublished());
            contentValues.add(storyValues);
        }
        return contentValues.toArray(new ContentValues[contentValues.size()]);
    }
}
package com.mbunyard.rest_client_example.service;

import android.app.IntentService;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mbunyard.rest_client_example.event.Event;
import com.mbunyard.rest_client_example.provider.StoryContract;
import com.mbunyard.rest_client_example.rest.RedditRestAdapter;
import com.mbunyard.rest_client_example.rest.model.StoryListingResponse;

import de.greenrobot.event.EventBus;

public class NetworkService extends IntentService {

    private static final String TAG = NetworkService.class.getSimpleName();
    private static ContentProvider contentProviderTest;

    public NetworkService() {
        super(TAG);
    }

    public static void getStories(Context context, ContentProvider contentProvider) {
        Log.d(TAG, "***** getStories()");
        contentProviderTest = contentProvider;
        context.startService(new Intent(context, NetworkService.class));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO: remove.
        String intentData = intent.getDataString();
        Log.d(TAG, "***** onHandleIntent - received intent: " + intent + " | intentData: " + intentData);

        // TODO: make network request.
        try {
            StoryListingResponse storiesResponse = RedditRestAdapter.getListingsService().getStories();
            if (storiesResponse != null) {
                Log.d(TAG, "***** Attempt to insert/update stories: " + storiesResponse.getData().getStories().size());

                // TODO - review : bulk insert/replace stories.
                //getContentResolver().bulkInsert(StoryContract.Story.CONTENT_URI, storiesResponse.getStoryContentValues());

                // Attempt to insert stories one-by-one.
                for (ContentValues contentValues : storiesResponse.getStoryContentValues()) {
                    contentProviderTest.insert(StoryContract.Story.CONTENT_URI, contentValues);
                }
            } else {
                Log.d(TAG, "***** No stories returned from web service");
            }
        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
        } finally {
            contentProviderTest = null;

            // Inform UI/main thread that query is complete
            EventBus.getDefault().post(new Event.QueryCompleteEvent());
        }
    }
}

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
        contentProviderTest = contentProvider;
        context.startService(new Intent(context, NetworkService.class));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // Make HTTP request on current, intent service background thread, to obtain stories.
            StoryListingResponse storiesResponse = RedditRestAdapter.getListingsService().getStories();
            if (storiesResponse != null) {
                // Attempt to bulk insert stories.
                //contentProviderTest.bulkInsert(
                //        StoryContract.Story.CONTENT_URI, storiesResponse.getStoryContentValues());

                // Attempt to insert stories one-by-one.
                for (ContentValues contentValues : storiesResponse.getStoryContentValues()) {
                    contentProviderTest.insert(StoryContract.Story.CONTENT_URI, contentValues);
                }

                // Inform UI/main thread that query is complete.
                EventBus.getDefault().post(new Event.QueryCompleteEvent());
            }
        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));

            // Inform UI/main thread that query is complete and there was an error.
            EventBus.getDefault().post(new Event.QueryCompleteEvent());
            EventBus.getDefault().post(new Event.QueryServiceError(
                    "unable to complete network request"));
        } finally {
            contentProviderTest = null;
        }
    }
}

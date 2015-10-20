package com.mbunyard.rest_client_example.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mbunyard.rest_client_example.provider.StoryContract;
import com.mbunyard.rest_client_example.rest.RedditRestAdapter;
import com.mbunyard.rest_client_example.rest.model.StoryListingResponse;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class NetworkService extends IntentService {

    private static final String TAG = NetworkService.class.getSimpleName();

    public NetworkService() {
        super(TAG);
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, NetworkService.class);
    }

    public static void getStories(Context context) {
        Log.d(TAG, "***** getStories()");
        Intent intent = newIntent(context);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String intentData = intent.getDataString();
        Log.d(TAG, "***** onHandleIntent - received intent: " + intent + " | intentData: " + intentData);

        // TODO: make network request.
        try {
            StoryListingResponse storiesResponse = RedditRestAdapter.getListingsService().getStories();
            if (storiesResponse != null) {
                Log.d(TAG, "***** Attempt to insert/update stories: " + storiesResponse.getData().getStories().size());
                //bulkInsert(StoryContract.Story.CONTENT_URI, storiesResponse.getStoryContentValues());
                // TODO: bulk insert via content provider
                //getContentResolver().bulkInsert(StoryContract.Story.CONTENT_URI, storiesResponse.getStoryContentValues());

                for (ContentValues contentValues : storiesResponse.getStoryContentValues()) {
                    getContentResolver().insert(StoryContract.Story.CONTENT_URI, contentValues);
                }

            } else {
                Log.d(TAG, "***** No stories returned from web service");
            }
        } catch (Exception e) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Log.d(TAG, "***** intent service - finalize()");
    }
}

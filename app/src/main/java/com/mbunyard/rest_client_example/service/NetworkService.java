package com.mbunyard.rest_client_example.service;

import android.app.IntentService;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mbunyard.rest_client_example.NetworkUtil;
import com.mbunyard.rest_client_example.event.Event;
import com.mbunyard.rest_client_example.provider.StoryContract;
import com.mbunyard.rest_client_example.rest.RedditRestAdapter;
import com.mbunyard.rest_client_example.rest.model.StoryListingResponse;

import java.net.UnknownHostException;

import de.greenrobot.event.EventBus;
import retrofit.Call;
import retrofit.Response;

public class NetworkService extends IntentService {

    private static final String TAG = NetworkService.class.getSimpleName();
    private static ContentProvider contentProvider;

    public NetworkService() {
        super(TAG);
    }

    public static void getStories(Context context, ContentProvider contentProvider) {
        NetworkService.contentProvider = contentProvider;
        context.startService(new Intent(context, NetworkService.class));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Call<StoryListingResponse> call = RedditRestAdapter.getListingsService().getStories();
        try {
            Response<StoryListingResponse> response = call.execute();
            if (response.isSuccess()) {     // response code is 2xx
                // Attempt to bulk insert stories.
                //contentProvider.bulkInsert(
                //        StoryContract.Story.CONTENT_URI, response.body().getStoryContentValues());

                // Attempt to insert stories one-by-one.
                for (ContentValues contentValues : response.body().getStoryContentValues()) {
                    contentProvider.insert(StoryContract.Story.CONTENT_URI, contentValues);
                }

                // Inform UI/main thread that query is complete.
                EventBus.getDefault().post(new Event.QueryCompleteEvent());
            } else {
                // Handle request errors.
                //int statusCode = response.code();
                //ResponseBody errorBody = response.errorBody();

                // Inform UI/main thread that query is complete and there was an error.
                EventBus.getDefault().post(new Event.QueryCompleteEvent());
                EventBus.getDefault().post(new Event.QueryServiceError(
                        "Network request error: " + response.code() + " - " + response.message()));
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());

            // Inform UI/main thread that query is complete and there was an error.
            EventBus.getDefault().post(new Event.QueryCompleteEvent());
            if (e instanceof UnknownHostException
                    && !NetworkUtil.isNetworkAvailableAndConnected(getApplicationContext())) {
                EventBus.getDefault().post(new Event.NoConnectivityEvent());
            } else {
                EventBus.getDefault().post(new Event.QueryServiceError(
                        "Network request error: " + e.getMessage()));
            }
        } finally {
            contentProvider = null;
        }
    }
}

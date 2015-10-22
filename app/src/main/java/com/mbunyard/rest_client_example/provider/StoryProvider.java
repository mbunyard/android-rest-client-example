package com.mbunyard.rest_client_example.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.mbunyard.rest_client_example.NetworkUtil;
import com.mbunyard.rest_client_example.R;
import com.mbunyard.rest_client_example.database.StoryDatabaseHelper;
import com.mbunyard.rest_client_example.event.Event;
import com.mbunyard.rest_client_example.rest.RedditRestAdapter;
import com.mbunyard.rest_client_example.rest.model.StoryListingResponse;
import com.mbunyard.rest_client_example.service.NetworkService;

import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.greenrobot.event.EventBus;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Stories content provider. The contract between this provider and applications
 * is defined in {@link StoryContract}.
 */
public class StoryProvider extends ContentProvider {
    public static final String TAG = StoryProvider.class.getSimpleName();

    /**
     * URI ID for route: /stories
     */
    public static final int ROUTE_STORIES = 1;

    /**
     * URI ID for route: /stories/{ID}
     */
    public static final int ROUTE_STORIES_ID = 2;

    /**
     * Provides access to backing database/datastore.
     */
    private StoryDatabaseHelper storyDatabaseHelper;

    /**
     * Use to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(StoryContract.AUTHORITY, "stories", ROUTE_STORIES);
        sUriMatcher.addURI(StoryContract.AUTHORITY, "stories/*", ROUTE_STORIES_ID);
    }

    /**
     * Initialize content provider on startup. This method is called for all registered content
     * providers on the application main thread at application launch time.
     */
    @Override
    public boolean onCreate() {
        storyDatabaseHelper = new StoryDatabaseHelper(getContext());
        return true;
    }

    /**
     * Handles requests for the MIME type of the data at the given URI.
     */
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case ROUTE_STORIES:
                return StoryContract.Story.CONTENT_TYPE;
            case ROUTE_STORIES_ID:
                return StoryContract.Story.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    /**
     * Handles query requests from clients.
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case ROUTE_STORIES:
                // Query database for all stories.

                // If no sort order specified/passed, use the default.
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = StoryContract.Story.DEFAULT_SORT_ORDER;
                }

                // Quickly return cached data from database query, notifying URI observers.
                queryBuilder.setTables(StoryContract.Story.TABLE_NAME);
                cursor = queryBuilder.query(
                        getDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
                if (null != getContext()) {
                    cursor.setNotificationUri(getContext().getContentResolver(), uri);
                }

                /**
                 * Always try to update results with the latest data from the network.
                 *
                 * Spawning an asynchronous load task thread, guarantees that the load has no
                 * chance to block any content provider method, and therefore no chance to block
                 * the UI thread.
                 *
                 * While the request loads, we return the cursor with existing data to the client.
                 *
                 * If the existing cursor is empty, the UI will render no content until it
                 * receives URI notification.
                 *
                 * Content updates that arrive when the asynchronous network request completes will
                 * appear in the already returned cursor, since that cursor query will match that of
                 * newly arrived items.
                 */
                if (isNetworkRequestAllowed(StoryContract.Story.TABLE_NAME, StoryContract.Story.CREATED)) {
                    // Use Retrofit to make request on background thread.
                    //getStoriesFromNetwork();

                    // Use IntentService to make network request on background thread.
                    NetworkService.getStories(getContext(), this);
                }
                return cursor;

            case ROUTE_STORIES_ID:
                // TODO: Query database for and return single story.

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        switch (sUriMatcher.match(uri)) {
            case ROUTE_STORIES:
                // Determine if record already exists and if not, insert new record.
                String storyId = (String) values.get(StoryContract.Story.ID);
                Long rowID = storyExists(storyId);
                if (rowID == null) {
                    long rowId = getDatabase().insert(StoryContract.Story.TABLE_NAME, null, values);
                    if (rowId >= 0) {
                        Uri insertUri = ContentUris.withAppendedId(StoryContract.Story.CONTENT_URI, rowId);
                        getContext().getContentResolver().notifyChange(insertUri, null);
                        return insertUri;
                    }
                    throw new IllegalStateException("Could not insert content values: " + values);
                }
                return ContentUris.withAppendedId(StoryContract.Story.CONTENT_URI, rowID);

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    /**
     * STUB - Functionality not yet implemented.
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * STUB - Functionality not yet implemented.
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * Handles requests to insert a set of rows.
     */
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        int insertCount;
        switch (sUriMatcher.match(uri)) {
            case ROUTE_STORIES:
                // TODO: swap replace for insert.
                insertCount = bulkReplaceRecords(StoryContract.Story.TABLE_NAME, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If rows inserted, notify registered observers.
        if (insertCount > 0 && null != getContext()) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return insertCount;
    }

    // --------------- Internal ---------------

    /**
     * Utility method to create and/or open database.
     *
     * @return readable, open database connection object
     */
    private SQLiteDatabase getDatabase() {
        return storyDatabaseHelper.getReadableDatabase();
    }

    /**
     * Utility method to insert/replace records in a single batch transaction.
     *
     * @param tableName database table to for story insert/replace
     * @param values    array of stories to insert/replace
     * @return number of stories inserted/replaced
     */
    private int bulkReplaceRecords(String tableName, ContentValues[] values) {
        int recordCount = 0;
        try {
            getDatabase().beginTransaction();
            for (ContentValues value : values) {
                long id = getDatabase().replace(tableName, tableName, value);
                if (id > -1) {
                    recordCount++;
                }
            }
            getDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error replacing records via bulkReplaceRecords(): ", e);
            return 0;
        } finally {
            getDatabase().endTransaction();
        }
        return recordCount;
    }

    /**
     * Requests recent stories from network (on background thread)
     * and inserts/replaces records in ContentProvider/database.
     */
    private void getStoriesFromNetwork() {
        Call<StoryListingResponse> call = RedditRestAdapter.getListingsService().getStories();
        call.enqueue(new Callback<StoryListingResponse>() {
            @Override
            public void onResponse(Response<StoryListingResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {     // response code is 2xx
                    StoryListingResponse storyList = response.body();

                    // Attempt to bulk insert stories.
                    //bulkInsert(
                    //        StoryContract.Story.CONTENT_URI, storyListingResponse.getStoryContentValues());

                    // Attempt to insert stories one-by-one.
                    for (ContentValues contentValues : storyList.getStoryContentValues()) {
                        insert(StoryContract.Story.CONTENT_URI, contentValues);
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
                            getContext().getString(R.string.request_error) + " | "
                                    + response.code() + " - " + response.message()));
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e(TAG, throwable.toString());

                // Inform UI/main thread that query is complete and there was an error.
                EventBus.getDefault().post(new Event.QueryCompleteEvent());
                if (throwable instanceof UnknownHostException
                        && !NetworkUtil.isNetworkAvailableAndConnected(getContext())) {
                    EventBus.getDefault().post(new Event.NoConnectivityEvent());
                } else {
                    EventBus.getDefault().post(new Event.QueryServiceError(
                            getContext().getString(R.string.request_error)));    // e.getMessage()}
                }
            }
        });
    }

    /**
     * Determines if a given story ID already exists within the database.
     *
     * @param storyId   network story ID
     * @return          the internal story ID if story already exists, null otherwise
     */
    private Long storyExists(String storyId) {
        Cursor cursor = null;
        Long rowID = null;
        try {
            cursor = getDatabase().query(
                    StoryContract.Story.TABLE_NAME,
                    new String[]{StoryContract.Story._ID},
                    StoryContract.Story.ID + " = ?",
                    new String[]{storyId},
                    null, null, null);
            if (cursor.moveToFirst()) {
                rowID = cursor.getLong(cursor.getColumnIndex(StoryContract.Story._ID));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return rowID;
    }

    /**
     * Determines if another network request should be completed based on the age of the most recent
     * latest cached record.
     *
     * @param tableName         database table name to query for most recent cache date
     * @param cacheDateColumn   database date column to use in determining cache age
     * @return                  true if cache is old enough to allow a new network request, false otherwise
     */
    private boolean isNetworkRequestAllowed(String tableName, String cacheDateColumn) {
        final long ALLOWED_REQUEST_AGE = 2;  // Seconds
        Cursor cursor = null;
        boolean isRequestAllowed = true;

        try {
            // Query database for single most recent update date/time to use as a reference point
            // for determining when last cache update was completed.
            String mostRecentUpdateDate = null;
            cursor = getDatabase().query(
                    tableName,
                    new String[]{cacheDateColumn},
                    null, null, null, null,
                    cacheDateColumn + " DESC",
                    "1");
            while (cursor.moveToNext()) {
                mostRecentUpdateDate = cursor.getString(cursor.getColumnIndex(cacheDateColumn));
            }
            cursor.close();

            // Determine if mostRecentUpdate is older than ALLOWED_REQUEST_AGE and another
            // network request/cache refresh can be started.
            if (!TextUtils.isEmpty(mostRecentUpdateDate)) {
                try {
                    final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date lastUpdateDate = format.parse(mostRecentUpdateDate);
                    Date currentDate = format.parse(format.format(new Date()));
                    long timeDiff = currentDate.getTime() - lastUpdateDate.getTime();
                    /*
                    Log.d(TAG, "***** lastUpdateDate: " + lastUpdateDate
                    + " | currentDate: " + currentDate + " | diff - ms: " + timeDiff
                    + " | sec: " + (timeDiff / 1000) + " | min: " + (timeDiff / 1000 / 60));
                    */
                    if ((timeDiff / 1000) < ALLOWED_REQUEST_AGE) {
                        isRequestAllowed = false;
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "Date Format/Parse Exception", e);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return isRequestAllowed;
    }
}
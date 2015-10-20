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

import com.mbunyard.rest_client_example.database.StoryDatabaseHelper;
import com.mbunyard.rest_client_example.rest.RedditRestAdapter;
import com.mbunyard.rest_client_example.rest.model.StoryListingResponse;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

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
        Log.d(TAG, "***** onCreate()");
        storyDatabaseHelper = new StoryDatabaseHelper(getContext());
        return true;
    }

    /**
     * Handles requests for the MIME type of the data at the given URI.
     */
    @Override
    public String getType(@NonNull Uri uri) {
        Log.d(TAG, "***** getType()");
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
                Log.d(TAG, "***** query() - stories");
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

                // TODO: remove - make network request on main thread.
                //getStoriesFromNetworkMainThread();

                // TODO: remove - make network request on background thread.
                getStoriesFromNetworkBackgroundThread();

                // TODO: *****************************************
                // TODO: leverage timestamp gate b4 making request
                // TODO: *****************************************

                // Leverage IntentService to make network request on background thread.
                //NetworkService.getStories(getContext());

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

                // TODO: investigate db.insertWithOnConflict(blah, blah, blah, SQLiteDatabase.CONFLICT_REPLACE)

                Log.d(TAG, "***** insert() called");

                // Insert the values into a new database row
                String storyId = (String) values.get(StoryContract.Story.ID);

                Long rowID = storyExists(storyId);
                if (rowID == null) {
                    // TODO: REMOVE
                    Log.d(TAG, "***** story (" + storyId + ") does not exist - insert");

                    //long time = System.currentTimeMillis();
                    //values.put(StoryContract.Story.CREATED, time);
                    long rowId = getDatabase().insert(StoryContract.Story.TABLE_NAME, null, values);
                    Log.d(TAG, "***** inserted into DB rowId: " + rowId + " | storyId: " + values.getAsString(StoryContract.Story.ID));
                    if (rowId >= 0) {
                        Uri insertUri = ContentUris.withAppendedId(StoryContract.Story.CONTENT_URI, rowId);
                        Log.d(TAG, "***** contentResolver.notifyChange: " + insertUri);
                        getContext().getContentResolver().notifyChange(insertUri, null);
                        return insertUri;
                    }

                    throw new IllegalStateException("Could not insert content values: " + values);
                }

                Log.d(TAG, "***** no insert - storyId: " + storyId + " | rowId: " + rowID + " | return uri: " + ContentUris.withAppendedId(StoryContract.Story.CONTENT_URI, rowID));
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
                insertCount = bulkReplaceRecords(StoryContract.Story.TABLE_NAME, values);
                Log.d(TAG, "***** # records inserted: " + insertCount); // TODO: remove
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If rows inserted, notify registered observers that row(s) was inserted/updated.
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
     * Requests recent stories from network and inserts/replaces records in ContentProvider/database.
     */
    private void getStoriesFromNetworkBackgroundThread() {
        Log.d(TAG, "***** getStoriesFromNetworkBackgroundThread");

        RedditRestAdapter.getListingsService().getStories(new Callback<StoryListingResponse>() {
            @Override
            public void success(StoryListingResponse storyListingResponse, Response response) {
                Log.d(TAG, "***** http response returned - attempt to insert/update stories: " + storyListingResponse.getData().getStories().size());

                // TODO - review : bulk insert/replace stories.
                //bulkInsert(StoryContract.Story.CONTENT_URI, storyListingResponse.getStoryContentValues());

                // Attempt to insert stories one-by-one.
                for (ContentValues contentValues : storyListingResponse.getStoryContentValues()) {
                    insert(StoryContract.Story.CONTENT_URI, contentValues);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "***** No stories returned from web service");
            }
        });
    }

    /**
     * Determines if a given story ID already exists within the database.
     *
     * @param storyId network story ID
     * @return the internal story ID if story already exists, null otherwise
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
                Log.d(TAG, "***** storyExists() - rowId: " + rowID + " | storyId: " + storyId);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return rowID;
    }
}
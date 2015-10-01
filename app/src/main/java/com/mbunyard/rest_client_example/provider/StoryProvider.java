package com.mbunyard.rest_client_example.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.mbunyard.rest_client_example.database.StoryDatabaseHelper;

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
    private StoryDatabaseHelper mStoryDatabaseHelper;

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
        mStoryDatabaseHelper = new StoryDatabaseHelper(getContext());
        return true;
    }

    /**
     * Handles requests for the MIME type of the data at the given URI.
     */
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case ROUTE_STORIES:
                return StoryContract.Story.CONTENT_TYPE;
            case ROUTE_STORIES_ID:
                return StoryContract.Story.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }

    /**
     * Handles query requests from clients.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case ROUTE_STORIES_ID:
                // Query database for single story.
                String id = uri.getLastPathSegment();
                // TODO: evaluate better/safer method of building query where clause.
                if (TextUtils.isEmpty(selection)) {
                    selection = StoryContract.Story._ID + "=" + id;
                } else {
                    selection += " AND " + StoryContract.Story._ID + "=" + id;
                }
            case ROUTE_STORIES:
                // Query database for all stories.

                // If no sort order specified/passed, use the default.
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = StoryContract.Story.DEFAULT_SORT_ORDER;
                }

                // Perform query and notify URI observers.
                queryBuilder.setTables(StoryContract.Story.TABLE_NAME);
                cursor = queryBuilder.query(getDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }

    /**
     * STUB - Functionality not yet implemented.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    /**
     * STUB - Functionality not yet implemented.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * STUB - Functionality not yet implemented.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * Handles requests to insert a set of rows.
     */
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int insertCount;
        switch (sUriMatcher.match(uri)) {
            case ROUTE_STORIES:
                insertCount = bulkReplaceRecords(StoryContract.Story.TABLE_NAME, values);
                Log.d(TAG, "***** number of records inserted: " + insertCount);// TODO: remove
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI " + uri);
        }

        // If rows inserted, notify registered observers that row(s) was inserted/updated.
        if (insertCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return insertCount;
    }

    /**
     * Utility method to create and/or open database.
     */
    private SQLiteDatabase getDatabase() {
        return mStoryDatabaseHelper.getReadableDatabase();
    }

    /**
     * Utility method to insert/update(replace) records in a single bulk transaction.
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
}
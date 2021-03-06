package com.mbunyard.rest_client_example.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mbunyard.rest_client_example.provider.StoryContract;

/**
 * SQLite datastore for @{link StoryProvider}.
 * <p/>
 * Provides access to an disk-backed, SQLite datastore, which is utilized by StoryProvider. This
 * database should never be directly accessed by other parts of the application.
 */
public class StoryDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = StoryDatabaseHelper.class.getSimpleName();

    /**
     * Schema version.
     */
    public static final int DATABASE_VERSION = 1;

    /**
     * Filename for SQLite file.
     */
    public static final String DATABASE_NAME = "story.db";

    /**
     * SQL DDL statement to create "story" table.
     */
    private static final String SQL_CREATE_STORIES =
            "CREATE TABLE " + StoryContract.Story.TABLE_NAME + " (" +
                    StoryContract.Story._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    StoryContract.Story.ID + " TEXT UNIQUE," +
                    StoryContract.Story.TITLE + " TEXT," +
                    StoryContract.Story.AUTHOR + " TEXT," +
                    StoryContract.Story.URL + " TEXT," +
                    StoryContract.Story.PERMALINK + " TEXT," +
                    StoryContract.Story.THUMBNAIL + " TEXT," +
                    StoryContract.Story.SCORE + " INTEGER," +
                    StoryContract.Story.PUBLISHED + " INTEGER," +
                    StoryContract.Story.CREATED + " DATE DEFAULT (datetime('now','localtime')))";

    /**
     * SQL DDL statement to drop "story" table.
     */
    private static final String SQL_DELETE_STORIES =
            "DROP TABLE IF EXISTS " + StoryContract.Story.TABLE_NAME;

    public StoryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_STORIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over.
        db.execSQL(SQL_DELETE_STORIES);
        onCreate(db);
    }
}
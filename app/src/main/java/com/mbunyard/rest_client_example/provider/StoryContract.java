package com.mbunyard.rest_client_example.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Public API for the {@link com.mbunyard.rest_client_example.provider.StoryProvider}.
 * <p/>
 * The public API for a content provider should only contain information that should be referenced
 * by content provider clients. Implementation details such as constants only used by a content
 * provider subclass should not appear in the provider API.
 */
public class StoryContract {

    /**
     * Private constructor to prevent external instantiation.
     */
    private StoryContract() {
    }

    /**
     * Content provider authority.
     */
    public static final String AUTHORITY = "com.mbunyard.rest_client_example";

    /**
     * The content:// style URL for this provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * Columns supported by "story" records.
     */
    public static class Story implements BaseColumns {

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of stories.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.rest_client_example.story";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single story.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.rest_client_example.story";

        /**
         * The content:// style URL for this table.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath("stories").build();

        /**
         * Table name where resources are stored.
         */
        public static final String TABLE_NAME = "story";

        /**
         * Default story sort order.
         */
        public static final String DEFAULT_SORT_ORDER = Story.PUBLISHED + " ASC";

        /**
         * Story ID.
         */
        public static final String ID = "story_id";

        /**
         * Story ID as an alias to allow ListView/Adapter binding.
         */
        public static final String ID_ALIAS = ID + " as _id";

        /**
         * Story title.
         */
        public static final String TITLE = "title";

        /**
         * Story author.
         */
        public static final String AUTHOR = "author";

        /**
         * Story URL.
         */
        public static final String URL = "url";

        /**
         * Story permalink.
         */
        public static final String PERMALINK = "permalink";

        /**
         * Story thumbnail URL.
         */
        public static final String THUMBNAIL = "thumbnail";

        /**
         * Story score.
         */
        public static final String SCORE = "score";

        /**
         * Date story was published/created remotely.
         */
        public static final String PUBLISHED = "published";

        /**
         * Date story was created within local cache/datastore.
         */
        public static final String CREATED = "created";
    }
}
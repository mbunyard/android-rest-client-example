package com.mbunyard.rest_client_example.ui.fragment;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.mbunyard.rest_client_example.R;
import com.mbunyard.rest_client_example.provider.StoryContract;
import com.mbunyard.rest_client_example.ui.adapter.StoryCursorAdapter;

/**
 * A list fragment representing a list of Stories.
 */
public class StoryListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = StoryListFragment.class.getSimpleName();
    private static final int LOADER_STORIES = 1;

    // Adapter to expose location data to list view.
    private StoryCursorAdapter adapter;

    private ListView listView;

    //private RecyclerView recyclerView;
    //private RecyclerView.Adapter adapter;
    //private RecyclerView.LayoutManager layoutManager;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment
     * (e.g. upon screen orientation changes).
     */
    public StoryListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_story_list, container, false);

        listView = (ListView) rootView.findViewById(R.id.story_list);

        /*
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.story_list);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        // TODO: replace with real data.
        String[] dataset = {"Story01", "Story02", "Story03", "Story04", "Story05"};

        adapter = new StoryAdapter(dataset);
        recyclerView.setAdapter(adapter);
        */

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Initialize data adapter and fetch/load story data.
        initStoryAdapter();

        // Request list of stories from content provider.
        getStories();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_STORIES) {
            // Specify Story data fields to get from content provider.
            String[] projection = new String[]{
                    StoryContract.Story.ID_ALIAS,
                    StoryContract.Story.TITLE
            };

            // Query content provide for locations
            return new CursorLoader(
                    getActivity(),
                    StoryContract.Story.CONTENT_URI,
                    projection,
                    "",
                    null,
                    StoryContract.Story.DEFAULT_SORT_ORDER + " LIMIT 30");
        } else {
            throw new IllegalArgumentException("No loader found by ID: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Swap in the new cursor. Framework will take care of closing the old cursor once method returns.
        adapter.swapCursor(cursor);

        // TODO: remove after testing
        /*
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            String id = cursor.getString(cursor.getColumnIndex(StoryContract.Story.ID));
            String title = cursor.getString(cursor.getColumnIndex(StoryContract.Story.TITLE));
            Log.d(TAG, "***** Story record: " + id + " - " + title);
        }
        */
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Ensure app is no longer referencing a cursor
        adapter.swapCursor(null);
    }

    // ---------- Internal API ----------

    /**
     * Starts or restarts existing loader to get story data from content provider (and web service).
     */
    private void getStories() {
        if (getLoaderManager().getLoader(LOADER_STORIES) != null) {
            getLoaderManager().restartLoader(LOADER_STORIES, null, this);
        } else {
            getLoaderManager().initLoader(LOADER_STORIES, null, this);
        }
    }

    private void initStoryAdapter() {
        adapter = new StoryCursorAdapter(
                getActivity().getApplicationContext(),
                R.layout.item_story,
                null,
                0);
        listView.setAdapter(adapter);
    }
}

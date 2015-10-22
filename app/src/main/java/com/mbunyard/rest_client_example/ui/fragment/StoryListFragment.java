package com.mbunyard.rest_client_example.ui.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.mbunyard.rest_client_example.R;
import com.mbunyard.rest_client_example.event.Event;
import com.mbunyard.rest_client_example.provider.StoryContract;
import com.mbunyard.rest_client_example.ui.adapter.StoryCursorAdapter;

import de.greenrobot.event.EventBus;

/**
 * A list fragment representing a list of stories.
 */
public class StoryListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = StoryListFragment.class.getSimpleName();
    private static final int LOADER_STORIES = 1;

    // Adapter to expose location data to list view.
    private StoryCursorAdapter adapter;

    // View to display list of stories.
    private ListView listView;

    // Refresh indicator view.
    SwipeRefreshLayout swipeRefreshLayout;

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

        listView = (ListView) rootView.findViewById(R.id.list);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_to_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        // Initialize adapter and attach to listview.
        adapter = new StoryCursorAdapter(
                getActivity().getApplicationContext(),
                R.layout.item_story,
                null,
                0);
        listView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register fragment as event bus subscriber.
        EventBus.getDefault().register(this);

        // Get stories from content provider.
        getStories(false);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Unregister fragment as event bus subscriber.
        EventBus.getDefault().unregister(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_STORIES) {
            // Specify story data fields to get from content provider.
            String[] projection = new String[]{
                    StoryContract.Story._ID,
                    StoryContract.Story.TITLE
            };

            // Query content provider for stories.
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
        // Swap in new cursor. Framework will take care of closing the old cursor once method returns.
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Ensure app is no longer referencing a cursor.
        adapter.swapCursor(null);
    }

    /**
     * SwipeRefresh callback.
     */
    @Override
    public void onRefresh() {
        // Get stories from content provider.
        getStories(true);
    }

    /**
     * Event bus event handler.
     */
    public void onEventMainThread(Object event) {
        if (event instanceof Event.QueryCompleteEvent) {
            // Stop pull-to-refresh UI indicator if displayed.
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        } else if (event instanceof Event.NoConnectivityEvent) {
            // TODO: swap toast with snackbar.
            Toast.makeText(getActivity().getApplicationContext(),
                    "No connection",
                    Toast.LENGTH_LONG).show();
        } else if (event instanceof Event.QueryServiceError) {
            // TODO: swap toast with snackbar.
            Toast.makeText(getActivity().getApplicationContext(),
                    ((Event.QueryServiceError) event).getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    // --------------- Internal ---------------

    /**
     * Initialize or reuse existing loader to get story data from content provider (and web service).
     */
    private void getStories(boolean updateCache) {
        if (updateCache) {
            if (getLoaderManager().getLoader(LOADER_STORIES) != null) {
                getLoaderManager().restartLoader(LOADER_STORIES, null, this);
            } else {
                getLoaderManager().initLoader(LOADER_STORIES, null, this);
            }
        } else {
            getLoaderManager().initLoader(LOADER_STORIES, null, this);
        }
    }
}

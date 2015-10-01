package com.mbunyard.rest_client_example.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.mbunyard.rest_client_example.R;
import com.mbunyard.rest_client_example.provider.StoryContract;

public class StoryCursorAdapter extends ResourceCursorAdapter {
    private static final String TAG = StoryCursorAdapter.class.getSimpleName();

    public StoryCursorAdapter(Context context, int layout, Cursor c, int flags) {
        super(context, layout, c, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int columnIndex;

        // Set story title.
        columnIndex = cursor.getColumnIndex(StoryContract.Story.TITLE);
        if (columnIndex > -1) {
            ((TextView) view.findViewById(R.id.story_title)).setText(cursor.getString(columnIndex));
        }
    }
}

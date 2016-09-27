package com.example.android.redditreader.UI;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.redditreader.PostCursorAdapter;
import com.example.android.redditreader.R;
import com.example.android.redditreader.data.RedditContract;

public class MainFragment extends Fragment {
    private static final String TAG = MainFragment.class.getSimpleName();
    private static final String SUBREDDIT_KEY = "subreddit_key";

    private String mSubreddit;
    private PostCursorAdapter mPostCursorAdapter;
    private ListView mListView;

    public interface Callback {
        void onItemSelected(String postPermaLink);
    }


    public static MainFragment newInstance(String subreddit) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(SUBREDDIT_KEY, subreddit);
        fragment.setArguments(args);
        return fragment;
    }

    public void setCursor(Cursor cursor) {
        if (cursor == null) {
            return;
        }
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
        }
        mPostCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSubreddit = getArguments().getString(SUBREDDIT_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_main, container, false);
        mListView = (ListView) rootview.findViewById(R.id.post_listview);
        Log.d(TAG, "set adapter" + mSubreddit);
        mListView.setAdapter(mPostCursorAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id){
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if(cursor != null) {
                    String postPermaLink = cursor.getString(cursor.getColumnIndex(RedditContract.PostEntry.COLUMN_PERMLINK));
                    ((Callback)getActivity()).onItemSelected(postPermaLink);
                }
            }
        });

        return rootview;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mPostCursorAdapter = new PostCursorAdapter(getActivity(), null, 0);
    }
}

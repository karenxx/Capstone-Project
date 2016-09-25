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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnActiveSubredditChangeListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {
    private static final String TAG = "MainFragment";
    private static final String SUBREDDIT_KEY = "subreddit_key";

    private String mSubreddit;
    private PostCursorAdapter mPostCursorAdapter;
    private ListView mListView;

    private OnActiveSubredditChangeListener mListener;

    public MainFragment() {
        Log.d(TAG, "new adapter " + mSubreddit);
    }

    public interface Callback {
        public void onItemSelected(String postPermaLink);
    }


    public static MainFragment newInstance(String subreddit) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(SUBREDDIT_KEY, subreddit);
        fragment.setArguments(args);
        return fragment;
    }

    public void setCursor(Cursor cursor) {
        Log.d(TAG, "fragment get cursor size" + cursor.getCount() + mSubreddit);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            Log.d(TAG, "subreddit in cursor" + cursor.getString(cursor.getColumnIndex(RedditContract.PostEntry.COLUMN_SUBREDDIT_NAME)));
        }
        mPostCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSubreddit = getArguments().getString(SUBREDDIT_KEY);
            Log.d(TAG, "fragment on create " + mSubreddit);
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
        if (context instanceof OnActiveSubredditChangeListener) {
            mListener = (OnActiveSubredditChangeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnActiveSubredditChangeListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            if (mListener != null)
                mListener.onActiveSubredditChange(mSubreddit, this);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnActiveSubredditChangeListener {
        // TODO: Update argument type and name
        void onActiveSubredditChange(String subreddit, MainFragment fragment);
    }
}

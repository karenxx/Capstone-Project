package com.example.android.redditreader;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.redditreader.data.RedditContract.PostEntry;
import com.squareup.picasso.Picasso;

public class PostCursorAdapter extends CursorAdapter {
    public static final String LOG_TAG = PostCursorAdapter.class.getSimpleName();
    public Context mContext;
    ImageView mThumbnailView;

    public static class ViewHolder {
        public final TextView titleView;
        public final TextView authorView;
        public final TextView commentCountView;
        public final TextView scoreView;
        public final ImageView thumbnailView;

        public ViewHolder(View view) {
            titleView = (TextView) view.findViewById(R.id.post_title);
            authorView = (TextView) view.findViewById(R.id.post_author);
            commentCountView = (TextView) view.findViewById(R.id.post_comment_count);
            scoreView = (TextView) view.findViewById(R.id.post_score);
            thumbnailView = (ImageView) view.findViewById(R.id.post_thumbnail);
        }
    }

    public PostCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.d(LOG_TAG, "new view");
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_post, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        String title = cursor.getString(cursor.getColumnIndex(PostEntry.COLUMN_TITLE));
        Log.d(LOG_TAG, "title: " + title);
        viewHolder.titleView.setText(title);

        String author = cursor.getString(cursor.getColumnIndex(PostEntry.COLUMN_AUTHOR));
        viewHolder.authorView.setText(author);

        int commentCount = cursor.getInt(cursor.getColumnIndex(PostEntry.COLUMN_COMMENT_COUNT));
        viewHolder.commentCountView.setText(commentCount +"");

        int score = cursor.getInt(cursor.getColumnIndex(PostEntry.COLUMN_SCORE));
        viewHolder.scoreView.setText(score + "");

        Picasso.with(mContext)
                .load(cursor.getString(cursor.getColumnIndex(PostEntry.COLUMN_THUMBNAIL)))
                .into(viewHolder.thumbnailView);
    }
}

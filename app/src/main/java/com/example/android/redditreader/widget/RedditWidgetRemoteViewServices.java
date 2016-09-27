package com.example.android.redditreader.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.redditreader.R;
import com.example.android.redditreader.data.RedditContract;

public class RedditWidgetRemoteViewServices extends RemoteViewsService {

    public final String LOG_TAG = RedditWidgetRemoteViewServices.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;
            @Override
            public void onCreate() {
                Log.d(LOG_TAG, "widget create");
            }

            @Override
            public void onDataSetChanged() {
                Log.d(LOG_TAG, "widget DATA change");
                if(data != null)
                    data.close();

                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query( RedditContract.PostEntry.buildPostBySubreddit("FrontPage"),null, null,null,null);
                Binder.restoreCallingIdentity(identityToken);

            }

            @Override
            public void onDestroy() {
                if(data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                Log.d(LOG_TAG, "getviewAt" + position);
                if(position == AdapterView.INVALID_POSITION || data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.list_item_widget);
                String title = data.getString(data.getColumnIndex(RedditContract.PostEntry.COLUMN_TITLE));
                String author = data.getString(data.getColumnIndex(RedditContract.PostEntry.COLUMN_AUTHOR));
                String subreddit = data.getString(data.getColumnIndex(RedditContract.PostEntry.COLUMN_SUBREDDIT_NAME));

                views.setTextViewText(R.id.post_title, title);
                views.setTextViewText(R.id.post_subredditname, "r/"+ subreddit + "  ");
                views.setTextViewText(R.id.post_author, author);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                Log.d(LOG_TAG, "ITEM ID " + position);
               if(data != null && data.moveToPosition(position))
                   return data.getLong(data.getColumnIndex(RedditContract.PostEntry._ID));
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}

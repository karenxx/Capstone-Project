package com.example.android.redditreader.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the reddit database.
 */
public class RedditContract {
    public static final String CONTENT_AUTHORITY = "com.example.android.redditreader";
    public static final Uri BASE_CONTETN_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final class PostEntry implements BaseColumns {
        public static final String TABLE_NAME = "post";
        public static final String COLUMN_SUBREDDIT_NAME = "subreddit_name";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_PERMLINK = "permlink";

        public static Uri buildPostBySubreddit(String subredditName) {
            return BASE_CONTETN_URI.buildUpon().appendEncodedPath(subredditName).build();
        }
    }
}

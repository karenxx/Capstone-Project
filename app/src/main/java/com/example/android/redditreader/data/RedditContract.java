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

    //public static final String PATH_SUBREDDIT = "subreddit";
    public static final String PATH_POST = "post";
    public static final String PATH_COMMENT = "comment";

//    public static final class SubredditEntry implements BaseColumns {
//        public static final Uri CONTENT_URI =
//                BASE_CONTETN_URI.buildUpon().appendPath(PATH_SUBREDDIT).build();
//        public static final String CONTENT_TYPE =
//                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUBREDDIT;
//        public static final String CONTENT_ITEM_TYPE =
//                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUBREDDIT;
//
//        public static final String TABLE_NAME = "subreddit";
//        public static final String COLUMN_NAME = "name";
////        public static final String COLUMN_SUBREDDITID = "subredditid";
//        public static final String COLUMN_DESCRIPTION = "description";
//        public static final String COLUMN_SELECTED = "selected";
//
//        public static Uri buildSubredditUri(long id) {
//            return ContentUris.withAppendedId(CONTENT_URI, id);
//        }
//    }

    public static final class PostEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTETN_URI.buildUpon().appendPath(PATH_POST).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POST;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POST;

        public static final String TABLE_NAME = "post";
        public static final String COLUMN_SUBREDDIT_ID = "subredditId";
        public static final String COLUMN_SUBREDDIT_NAME = "subreddit_name";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_DATE = "date";
        //link's net score
        public static final String COLUMN_SCORE = "score";
        //the way the logged user's vote
        public static final String COLUMN_VOTE = "vote";
        public static final String COLUMN_COMMENT_COUNT = "comment_count";

        public static Uri buildPostUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildPostBySubreddit(String subredditId) {
            return CONTENT_URI.buildUpon().appendEncodedPath(subredditId).build();
        }

        public static String getSubredditIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static final class CommentEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTETN_URI.buildUpon().appendPath(PATH_COMMENT).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMMENT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMMENT;

        public static final String TABLE_NAME = "comment";
        public static final String COLUMN_POST_ID = "post_id";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_BODY = "body";
        public static final String COLUMN_SCORE = "score";
        public static final String COLUMN_VOTE = "vote";
        public static final String COLUMN_PARENT_ID = "parent_id";

        public static Uri buildCommentUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}

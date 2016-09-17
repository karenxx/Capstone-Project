package com.example.android.redditreader.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.redditreader.data.RedditContract.PostEntry;

public class RedditProvider extends ContentProvider {
    private static final String LOG_TAG = RedditProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private RedditDbHelper mDbHelper;

    static final int POST = 100;
    static final int POST_BY_SUBREDDIT = 101;
    static final int COMMENT = 200;

    //    private static final String sSubredditIdSelection = RedditContract.SubredditEntry.TABLE_NAME+
//            "." + RedditContract.SubredditEntry._ID + " = ? ";
    private static final String sPostBySubreddit = PostEntry.TABLE_NAME + "." +
            PostEntry.COLUMN_SUBREDDIT_ID + " = ? ";

    private Cursor getPostBySubredditSetting(Uri uri, String[] projection, String sorOrder) {
        String subredditID = PostEntry.getSubredditIdFromUri(uri);
        selction
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = RedditContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, RedditContract.PATH_POST, POST);
        matcher.addURI(authority, RedditContract.PATH_POST + "/*", POST_BY_SUBREDDIT);
        matcher.addURI(authority, RedditContract.PATH_COMMENT, COMMENT);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new RedditDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case POST: {
                retCursor = mDbHelper.getReadableDatabase().query(
                        RedditContract.PostEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case POST_BY_SUBREDDIT: {

            }
            case COMMENT: {
                String subredditId = uri.getLastPathSegment();
                retCursor = mDbHelper.getReadableDatabase().query(
                        RedditContract.CommentEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null
                );
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case POST:
                return RedditContract.SubredditEntry.CONTENT_TYPE;
            case SUBREDDIT:
                return RedditContract.SubredditEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(LOG_TAG, "insert");
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnuri;
        switch (match) {
            case SUBREDDIT: {
                long _id = db.insert(RedditContract.SubredditEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnuri = RedditContract.SubredditEntry.buildSubredditUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnuri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(LOG_TAG, "delete");
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowDeleted;
        switch (match) {
            case SUBREDDIT_LIST:
                if (selection == null) {
                    selection = "1";
                }
                rowDeleted = db.delete(RedditContract.SubredditEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SUBREDDIT:
                String subredditId = uri.getLastPathSegment();
                rowDeleted = db.delete(
                        RedditContract.SubredditEntry.TABLE_NAME, sSubredditIdSelection, new String[]{subredditId});
                break;
            default:
                throw new UnsupportedOperationException("Unknow uri: " + uri);
        }
        if (rowDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case SUBREDDIT_LIST:
                rowsUpdated = db.update(RedditContract.SubredditEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case SUBREDDIT:
                String subredditId = uri.getLastPathSegment();
                rowsUpdated = db.update(RedditContract.SubredditEntry.TABLE_NAME, values, sSubredditIdSelection, new String[]{subredditId});
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mDbHelper.close();
        super.shutdown();
    }

}

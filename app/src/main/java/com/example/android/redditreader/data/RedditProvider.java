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
    private static final String SUBREDDIT_SELECTION = PostEntry.COLUMN_SUBREDDIT_NAME + "=?";
    private RedditDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new RedditDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.e(LOG_TAG, "query");
        String subreddit = uri.getEncodedPath();
        if (subreddit.length() < 2) {
            Log.e(LOG_TAG, "invalid uri for query");
        }
        subreddit = subreddit.substring(1); //remove "/";

        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor retCursor = null;
        try {
            retCursor= db.query(
                    RedditContract.PostEntry.TABLE_NAME,
                    null,
                    SUBREDDIT_SELECTION,
                    new String[] {subreddit},
                    null,
                    null,
                    sortOrder
            );

            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
            Log.d(LOG_TAG, "cursor count" + retCursor.getCount());
        } catch (Exception e) {
            Log.d(LOG_TAG, "query db error: " + e);
        }
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("insert not supported!");
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] initialValues) {
        // Remove all old submission in provider.
        delete(null, null, null);
        Log.d(LOG_TAG, "bulkInsert size " + initialValues.length);
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowInserted = 0;
        try {
            for (ContentValues contentValues : initialValues) {
                if (db.insert(RedditContract.PostEntry.TABLE_NAME, null, contentValues) != -1) {
                    ++rowInserted;
                }
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "Insert db error: " + e);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowInserted;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowDeleted = -1;
        try {
                rowDeleted = db.delete(RedditContract.PostEntry.TABLE_NAME, selection, selectionArgs);

        } catch (Exception e) {
            Log.d(LOG_TAG, "Delete db error: " + e);
        }
        Log.d(LOG_TAG, "delete " + rowDeleted);
        return rowDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("update not supported!");
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        mDbHelper.close();
        super.shutdown();
    }

}

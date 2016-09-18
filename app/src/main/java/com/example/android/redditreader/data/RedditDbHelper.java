package com.example.android.redditreader.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.redditreader.data.RedditContract.PostEntry;

public class RedditDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "reader.db";

    public RedditDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_POST_TABLE = "CREATE TABLE " + PostEntry.TABLE_NAME + " (" +
                PostEntry._ID + " INTEGER PRIMARY KEY," +
                PostEntry.COLUMN_SUBREDDIT_NAME + " TEXT NOT NULL," +
                PostEntry.COLUMN_TITLE + " TEXT NOT NULL," +
                PostEntry.COLUMN_AUTHOR + " TEXT NOT NULL," +
                PostEntry.COLUMN_PERMLINK + " TEXT NOT NULL" +
                ");";
        db.execSQL(SQL_CREATE_POST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PostEntry.TABLE_NAME);
        onCreate(db);
    }
}

package com.example.android.redditreader.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.redditreader.data.RedditContract.CommentEntry;
import com.example.android.redditreader.data.RedditContract.PostEntry;

public class RedditDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    static final String DATABASE_NAME = "reader.db";

    public RedditDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        final String SQL_CREATE_SUBREDDIT_TABLE =  "CREATE TABLE " + SubredditEntry.TABLE_NAME + "("
//                + SubredditEntry._ID + " INTEGER PRIMARY KEY, "
//                + SubredditEntry.COLUMN_NAME + " TEXT NOT NULL,"
//                //+ SubredditEntry.COLUMN_SUBREDDITID + " TEXT,"
//                + SubredditEntry.COLUMN_DESCRIPTION + " TEXT,"
//                + SubredditEntry.COLUMN_SELECTED + " INTEGER NOT NULL DEFAULT 0);";
//        db.execSQL(SQL_CREATE_SUBREDDIT_TABLE);

        final String SQL_CREATE_POST_TABLE = "CREATE TABLE " + PostEntry.TABLE_NAME + " (" +
                PostEntry._ID + " INTEGER PRIMARY KEY," +
                PostEntry.COLUMN_SUBREDDIT_ID + " TEXT NOT NULL," +
                PostEntry.COLUMN_SUBREDDIT_NAME + " TEXT NOT NULL," +
                PostEntry.COLUMN_TYPE + " INTEGER NOT NULL," +
                PostEntry.COLUMN_TITLE + " TEXT NOT NULL," +
                PostEntry.COLUMN_AUTHOR + " TEXT NOT NULL," +
                PostEntry.COLUMN_URL + " TEXT NOT NULL," +
                PostEntry.COLUMN_DATE + " TEXT," +
                PostEntry.COLUMN_SCORE + " INTEGER" +
                PostEntry.COLUMN_VOTE + " TEXT," +
                PostEntry.COLUMN_COMMENT_COUNT + " INTEGER" +
                ");";
        db.execSQL(SQL_CREATE_POST_TABLE);

        final String SQL_CREATE_COMMENT_TABLE = "CREATE TABLE " + CommentEntry.TABLE_NAME + " (" +
                CommentEntry._ID + " INTEGER PRIMARY KEY," +
                CommentEntry.COLUMN_POST_ID + " INTEGER NOT NULL," +
                CommentEntry.COLUMN_AUTHOR + " TEXT," +
                CommentEntry.COLUMN_BODY + " TEXT NOT NULL," +
                CommentEntry.COLUMN_SCORE + " INTEGER," +
                CommentEntry.COLUMN_VOTE + " INTEGER," +
                CommentEntry.COLUMN_PARENT_ID + " INTEGER," +
                ");";
        db.execSQL(SQL_CREATE_COMMENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PostEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CommentEntry.TABLE_NAME);
        onCreate(db);
    }
}

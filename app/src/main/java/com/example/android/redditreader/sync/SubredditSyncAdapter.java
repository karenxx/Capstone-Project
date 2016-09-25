package com.example.android.redditreader.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.android.redditreader.R;
import com.example.android.redditreader.data.RedditContract;
import com.example.android.redditreader.data.RedditContract.PostEntry;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.ArrayList;
import java.util.List;


public class SubredditSyncAdapter extends AbstractThreadedSyncAdapter {
    private final String LOG_TAG = getClass().getSimpleName();
    public static final int SYNC_INTERVAL = 60 * 60 * 24;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 24;
    public static final String PREF_SEPARATOR = "#";
    private static final int POST_LIMIT_COUNT = 100;
    ContentResolver mContentResolver;
    final Context mContext;

    public SubredditSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
        mContext = context;
    }

    public SubredditSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");

        RedditClient redditClient = AuthenticationManager.get().getRedditClient();
        UserSubredditsPaginator userSubredditsPaginator = new UserSubredditsPaginator(redditClient, "subscriber");
        String subredditPref = "";
        List<String> subredditList = new ArrayList<>();
        try {
            while (userSubredditsPaginator.hasNext()) {
                Listing<Subreddit> subreddits = userSubredditsPaginator.next();
                for (Subreddit subreddit : subreddits) {
                    if (!subreddit.isNsfw()) {
                        Log.d(LOG_TAG, "subreddit ID: " + subreddit.getId() + subreddit.getDisplayName());
                        subredditPref += subreddit.getDisplayName() + PREF_SEPARATOR;
                        subredditList.add(subreddit.getDisplayName());
                    }
                }
            }
            SharedPreferences.Editor editor = mContext.getSharedPreferences(mContext.getString(R.string.subreddit_file_key), Context.MODE_PRIVATE).edit();
            editor.putString(mContext.getString(R.string.saved_subreddit_key), subredditPref);
            Log.d(LOG_TAG, "write to sharedpref");
            editor.apply();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error message: ", e);
        }

        SubredditPaginator subredditPaginator = new SubredditPaginator(redditClient);
        subredditPaginator.setLimit(POST_LIMIT_COUNT);
        if (subredditList.size() == 0) {
            Log.e(LOG_TAG, "No subreddit");
            return;
        }
        if (subredditList.size() == 1) {
            subredditPaginator.setSubreddit(subredditList.get(0));
        } else {
            subredditPaginator.setSubreddit(subredditList.get(0),
                    subredditList.subList(1, subredditList.size()).toArray(new String[0]));
        }
        List<ContentValues> result = new ArrayList<>();
        try {
            while (subredditPaginator.hasNext()) {
                Listing<Submission> submissions = subredditPaginator.next();
                for (Submission submission : submissions) {
                    if (!submission.isNsfw()) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(PostEntry.COLUMN_AUTHOR, submission.getAuthor());
                        contentValues.put(PostEntry.COLUMN_TITLE, submission.getTitle());
                        contentValues.put(PostEntry.COLUMN_PERMLINK, submission.getPermalink());
                        contentValues.put(PostEntry.COLUMN_SUBREDDIT_NAME, submission.getSubredditName());
                        contentValues.put(PostEntry.COLUMN_THUMBNAIL, submission.getThumbnail());
                        contentValues.put(PostEntry.COLUMN_SCORE, submission.getScore());
                        contentValues.put(PostEntry.COLUMN_COMMENT_COUNT, submission.getCommentCount());
                        result.add(contentValues);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error message: ", e);
        }

        try {
            Log.d(LOG_TAG, mContentResolver.bulkInsert(RedditContract.BASE_CONTETN_URI, result.toArray(new ContentValues[0])) + " inserted");
        } catch (Exception e) {
            Log.e(LOG_TAG, "ERROR: ", e);
        }
    }


    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));
        if (null == accountManager.getPassword(newAccount)) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        SubredditSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        // Without calling setSyncAutomatically, our periodic sync will not be enabled.
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        // Finally, let's do a sync to get things started
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}

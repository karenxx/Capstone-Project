package com.example.android.redditreader.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.android.redditreader.R;
import com.example.android.redditreader.data.RedditContract.SubredditEntry;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SubredditSyncAdapter extends AbstractThreadedSyncAdapter {
    private final String LOG_TAG = getClass().getSimpleName();
    public static final int SYNC_INTERVAL = 60 * 60 * 24;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 24;
    ContentResolver mContentResolver;

    public SubredditSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    public SubredditSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");

        RedditClient redditClient = AuthenticationManager.get().getRedditClient();
        UserSubredditsPaginator paginator = new UserSubredditsPaginator(redditClient, "subscriber");
        HashMap<String, Subreddit> userSubreddit = new HashMap<>();
        try {
            while (paginator.hasNext()) {
                Listing<Subreddit> subreddits = paginator.next();
                for (Subreddit subreddit : subreddits) {
                    if (!subreddit.isNsfw()) {
                        Log.d(LOG_TAG, "subreddit ID: " + subreddit.getId() + subreddit.getDisplayName());
                        userSubreddit.put(subreddit.getId(), subreddit);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error message: ", e);
        }


        List<ContentValues> result = new ArrayList<>();
        for (Subreddit subreddit : userSubreddit.values()) {
            ContentValues subredditValues = new ContentValues();
            String id = subreddit.getId();
            subredditValues.put(SubredditEntry._ID, id);
            subredditValues.put(SubredditEntry.COLUMN_NAME, subreddit.getDisplayName());
            subredditValues.put(SubredditEntry.COLUMN_DESCRIPTION, subreddit.getPublicDescription());
            result.add(subredditValues);
        }
        Uri uri = SubredditEntry.CONTENT_URI;
        try {
            for (ContentValues cv : result) {
                String subredditId = cv.getAsString(SubredditEntry._ID);
                mContentResolver.delete(uri, SubredditEntry._ID + "=?", new String[]{subredditId});
                mContentResolver.insert(uri, cv);
            }
            Log.d(LOG_TAG, "sync complete" + result.size() + " inserted");
        }catch (Exception e) {
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

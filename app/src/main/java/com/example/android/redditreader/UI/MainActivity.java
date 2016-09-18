package com.example.android.redditreader.UI;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.android.redditreader.R;
import com.example.android.redditreader.UserInfoActivity;
import com.example.android.redditreader.data.RedditContract;
import com.example.android.redditreader.handler.AuthenHandler;
import com.example.android.redditreader.sync.SubredditSyncAdapter;

import net.dean.jraw.auth.AuthenticationManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";


    public static final String AUTHORITY = "com.example.android.redditreader";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "redditreader.example.com";
    // The account name
    public static final String ACCOUNT = "dummyaccount";
    // Instance fields
    Account mAccount;

    SharedPreferences.OnSharedPreferenceChangeListener mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.d(TAG, "saved subreddit key changed " + key);
            if (key.equals(getString(R.string.saved_subreddit_key))) {
                Log.d(TAG, "saved subreddit key changed");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerSubredditObserver();
        mAccount = createSyncAccount(this);
    }

    private void registerSubredditObserver() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.subreddit_file_key), Context.MODE_PRIVATE);
        prefs.registerOnSharedPreferenceChangeListener(mListener);
    }

    public void userInfo(View view) {
        Cursor cursor = getContentResolver().query(
                RedditContract.PostEntry.buildPostBySubreddit("funny"),   // The content URI of the words table
                null,                        // The columns to return for each row
                null,                    // Selection criteria
                null,                     // Selection criteria
                null);                        // The sort order for the returned rows
        if (cursor != null) {
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                String sub = cursor.getString(cursor.getColumnIndex(RedditContract.PostEntry.COLUMN_SUBREDDIT_NAME));
                String title = cursor.getString(cursor.getColumnIndex(RedditContract.PostEntry.COLUMN_TITLE));
                Log.d(TAG, sub + " " + title);
            }
            cursor.close();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onResume() {
        super.onResume();
        checkAuthenState();
    }


    public void checkAuthenState() {
        AuthenHandler authenHandler = AuthenHandler.get(this);
        String authenState = AuthenticationManager.get().checkAuthState().toString();
        switch (authenState) {
            case "READY":
                Log.d(TAG, "ready");
                syncData();
                break;
            case "NONE":
                Log.d(TAG, "none");
                startActivity(new Intent(this, LoginActivity.class));
                break;
            case "NEED_REFRESH":
                Log.d(TAG, "refresh");
                authenHandler.refreshAuthenTokenAsync();
                break;
        }
    }

    private void syncData() {
        Log.d(TAG, "start sync");
        // Pass the settings flags by inserting them in a bundle
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        /*
         * Request the sync for the default account, authority, and
         * manual sync settings
         */
        ContentResolver.requestSync(mAccount, AUTHORITY, settingsBundle);
    }

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account createSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
        } else {
            Log.e(TAG, "createSyncAccount() error");
        }
        return newAccount;
    }
}

package com.example.android.redditreader.UI;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.redditreader.R;
import com.example.android.redditreader.data.RedditContract;
import com.example.android.redditreader.handler.AuthenHandler;
import com.example.android.redditreader.sync.SubredditSyncAdapter;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.firebase.analytics.FirebaseAnalytics;

import net.dean.jraw.auth.AuthenticationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, MainFragment.Callback{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int POST_LOADER = 0;

    Map<String, MainFragment> mSavedFragment = new HashMap<>();

    TabLayout mTabLayout;
    TabPagerAdapter mTabPagerAdapter;
    ViewPager mViewpager;
    private FirebaseAnalytics mFirebaseAnalytics;

    //public static final String AUTHORITY = "com.example.android.redditreader";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "redditreader.example.com";
    // The account name
    public static final String ACCOUNT = "dummyaccount";
    // Instance fields
    Account mAccount;

    private List<String> mSubredditList = new ArrayList<>();
    private String mActiveSubreddit;
    private MainFragment mActiveFragment;
    private OnSubredditChangeListener mSubredditChangeListener = new OnSubredditChangeListener();

    private class OnSubredditChangeListener extends ViewPager.SimpleOnPageChangeListener {
        Boolean first = true;

        public void setFirst(boolean isFirst) {
            first = isFirst;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (first){
                onPageSelected(position);
                first = false;
            }
        }

        @Override
        public void onPageSelected(int position) {
            mActiveSubreddit = mSubredditList.get(position);
            mActiveFragment = mSavedFragment.get(mSubredditList.get(position));
            Log.d(TAG, "onPageSelected" + mActiveSubreddit + mActiveFragment);
            getLoaderManager().restartLoader(POST_LOADER, null, MainActivity.this);
        }
    }

    SharedPreferences.OnSharedPreferenceChangeListener mSubredditPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.saved_subreddit_key))) {
                updateSubreddits();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(getApplicationContext(), getString(R.string.app_id));
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewpager = (ViewPager) findViewById(R.id.pager);
        mTabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
        updateSubreddits();
        mViewpager.setAdapter(mTabPagerAdapter);
        //set tablayout with viewpager
        mTabLayout.setupWithViewPager(mViewpager);

        // adding functionality to tab and viewpager to manage each other when a page is changed or when a tab is selected
        mViewpager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mViewpager.addOnPageChangeListener(mSubredditChangeListener);

        registerSubredditObserver();
        mAccount = createSyncAccount(this);
        getLoaderManager().initLoader(POST_LOADER, null, this);
    }

    private void registerSubredditObserver() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.subreddit_file_key), Context.MODE_PRIVATE);
        prefs.registerOnSharedPreferenceChangeListener(mSubredditPrefListener);
    }

    private void updateSubreddits() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.subreddit_file_key), Context.MODE_PRIVATE);
        String subreddits = prefs.getString(getString(R.string.saved_subreddit_key), null);
        List<String> updatedSubredditList = new ArrayList<>();
        if (subreddits != null) {
            String[] subredditsArray = subreddits.split(SubredditSyncAdapter.PREF_SEPARATOR);
            for (String str : subredditsArray) {
                if (str.length() != 0) {
                    updatedSubredditList.add(str);
                }
            }
        }
        mSubredditList = updatedSubredditList;
        mTabPagerAdapter.notifyDataSetChanged();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onResume() {
        super.onResume();
        mSubredditChangeListener.setFirst(true);
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
        ContentResolver.requestSync(mAccount, getResources().getString(R.string.content_authority), settingsBundle);
        getLoaderManager().restartLoader(POST_LOADER, null, this);
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
             * your <provider> element in the manifest, then
             * call context.setIsSyncable(account, AUTHORITY, 1) here.
             */
        } else {
            Log.e(TAG, "createSyncAccount() error");
        }
        return newAccount;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case POST_LOADER:
                // Returns a new CursorLoader
                return new CursorLoader(
                        this,
                        RedditContract.PostEntry.buildPostBySubreddit(mActiveSubreddit),
                        null,
                        null,
                        null,
                        null
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG, "cursorl load finish " + (cursor == null? "null" : cursor.getCount() + ""));
        if (mActiveFragment != null) {
            mActiveFragment.setCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mActiveFragment != null) {
            mActiveFragment.setCursor(null);
        }
    }

    @Override
    public void onItemSelected(String postPermaLink) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, postPermaLink);
        startActivity(intent);
    }

    public class TabPagerAdapter extends FragmentStatePagerAdapter {
        public TabPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            MainFragment fragment = MainFragment.newInstance(mSubredditList.get(position));
            mSavedFragment.put(mSubredditList.get(position), fragment);
           return fragment;
        }

        @Override
        public int getCount() {
            return mSubredditList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Log.d(TAG, "get PageTitle");

            Bundle params = new Bundle();
            params.putString(getResources().getString(R.string.subreddit), mSubredditList.get(position));
            mFirebaseAnalytics.logEvent(getResources().getString(R.string.analytics_event_open_subreddit), params);

            return mSubredditList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mSavedFragment.remove(mSubredditList.get(position));
            super.destroyItem(container, position, object);
        }
    }
}

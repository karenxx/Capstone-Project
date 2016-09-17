package com.example.android.redditreader.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class SubredditSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static SubredditSyncAdapter sSubredditSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock){
            if(sSubredditSyncAdapter == null) {
                sSubredditSyncAdapter = new SubredditSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sSubredditSyncAdapter.getSyncAdapterBinder();
    }
}

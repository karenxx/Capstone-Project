package com.example.android.redditreader.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.android.redditreader.UI.LoginActivity;
import com.example.android.redditreader.handler.AuthenHandler;

import net.dean.jraw.auth.AuthenticationManager;

public class SubredditSyncService extends Service {
    private static final String TAG = "SubredditSyncService";
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

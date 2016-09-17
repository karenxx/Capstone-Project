package com.example.android.redditreader.UI;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.android.redditreader.R;
import com.example.android.redditreader.UserInfoActivity;
import com.example.android.redditreader.handler.AuthenHandler;
import com.example.android.redditreader.sync.SubredditSyncAdapter;

import net.dean.jraw.auth.AuthenticationManager;

public class MainActivity extends AppCompatActivity {
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //startActivity(new Intent(this, LoginActivity.class));
        SubredditSyncAdapter.initializeSyncAdapter(this);
    }

    public void userInfo(View view) {
        startActivity(new Intent(this, UserInfoActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAuthenState();
        //getPost();
    }

    public void checkAuthenState() {
        AuthenHandler authenHandler = AuthenHandler.get(this);
        String authenState = AuthenticationManager.get().checkAuthState().toString();
        switch (authenState) {
            case "READY":
                Log.d("MAIN", "ready");
                break;
            case "NONE":
                Log.d("Main", "none");
                Toast.makeText(MainActivity.this, "Log in first", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                break;
            case "NEED_REFRESH":
                Log.d("MAIN", "refresh");
                authenHandler.refreshAuthenTokenAsync();
                break;
        }
    }


//    public void getPost() {
//        new AsyncTask<Void, Void, String>() {
//
//            @Override
//            protected String doInBackground(Void... params) {
//                try {
//                    SubredditPaginator paginator = new SubredditPaginator(AuthenticationManager.get().getRedditClient());
//                    Listing<Submission> firstPage = paginator.next();
//                    String author = null;
//                    for (Submission submission : firstPage) {
//                        Log.d("MAIN", submission.getAuthor());
//                        author = submission.getAuthor();
//                    }
//                    return author;
//                } catch (Exception e) {
//                    Log.d(LOG_TAG, "Failed to get author" + e);
//                    return null;
//                }
//
//            }
//
//            @Override
//            protected void onPostExecute(String s) {
//                Log.d(LOG_TAG, "author" + s);
//            }
//        }.execute();
//    }
}

package com.example.android.redditreader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.models.LoggedInAccount;

public class UserInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        new AsyncTask<Void, Void, LoggedInAccount>() {
            @Override
            protected LoggedInAccount doInBackground(Void... params) {
                return AuthenticationManager.get().getRedditClient().me();
            }

            @Override
            protected void onPostExecute(LoggedInAccount data) {
                ((TextView) findViewById(R.id.user_name)).setText("Name: " + data.getFullName());


            }
        }.execute();
    }
}

package com.example.android.redditreader.UI;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.android.redditreader.R;
import com.example.android.redditreader.UserInfoActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startActivity(new Intent(this, LoginActivity.class));


    }

    public void userInfo(View view) {
        startActivity(new Intent(this, UserInfoActivity.class));
    }

}

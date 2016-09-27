package com.example.android.redditreader.UI;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.android.redditreader.R;


public class DetailActivity extends AppCompatActivity {

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        final WebView webView = (WebView) findViewById(R.id.post_detail_webview);

        String postPermaLink = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        String link = getApplicationContext().getString(R.string.reddit_link) + postPermaLink;
        webView.setWebViewClient(new MyWebViewClient());
        webView.loadUrl(link);

    }
}

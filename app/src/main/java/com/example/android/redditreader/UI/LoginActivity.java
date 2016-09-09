package com.example.android.redditreader.UI;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.android.redditreader.R;
import com.example.android.redditreader.handler.AuthenHandler;

import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.NoSuchTokenException;
import net.dean.jraw.auth.TokenStore;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.Credentials;

import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    private final String LOG_TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final WebView webView = (WebView) findViewById(R.id.webview);

        final AuthenHandler authenHandler = new AuthenHandler(getApplicationContext(), tokenStore);
        final URL authorizationUrl = authenHandler.getAuthUrl();
        // Load the authorization URL into the browser
        webView.loadUrl(authorizationUrl.toExternalForm());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url.contains("code=")) {
                    Log.d(LOG_TAG, "webview url: " + url);
                    // We've detected the redirect URL
                    onUserChallenge(url, authenHandler.credentials);
                    //  AuthenHandler.get().onUserChallenge(url, AuthenHandler.get().credentials);
                } else if (url.contains("error=")) {
                    Toast.makeText(LoginActivity.this, "You must press 'allow' to log in with this account", Toast.LENGTH_SHORT).show();
                    webView.loadUrl(authorizationUrl.toExternalForm());
                }
            }
        });
    }

    public TokenStore tokenStore = new TokenStore() {
        @Override
        public boolean isStored(String key) {
            return PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).contains(key);
        }

        @Override
        public String readToken(String key) throws NoSuchTokenException {
            String token = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getString(key, null);
            if (token == null) {
                throw new NoSuchTokenException(key);
            }
            return token;
        }

        @Override
        public void writeToken(String key, String token) {
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                    .putString(key, token);
        }
    };

    private void onUserChallenge(final String url, final Credentials creds) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    OAuthData data = AuthenticationManager.get().getRedditClient().getOAuthHelper()
                            .onUserChallenge(params[0], creds);
                    AuthenticationManager.get().getRedditClient().authenticate(data);
                    return AuthenticationManager.get().getRedditClient().getAuthenticatedUser();
                } catch (NetworkException | OAuthException e) {
                    Log.e(LOG_TAG, "Could not log in", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String s) {
                Log.i(LOG_TAG, s);
                LoginActivity.this.finish();
            }
        }.execute(url);
    }

}
package com.example.android.redditreader.handler;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.NoSuchTokenException;
import net.dean.jraw.auth.RefreshTokenHandler;
import net.dean.jraw.auth.TokenStore;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.OAuthHelper;

import java.net.URL;

public class AuthenHandler {
    private static AuthenHandler INSTANCE;
    public static AuthenHandler get(Context context) {
        return INSTANCE == null ? new AuthenHandler(context) : INSTANCE;
    }

    private final String LOG_TAG = AuthenHandler.class.getSimpleName();
    public static final String REDDIT_CLIENT_ID = "MFe5_m-ZqVQAig";
    public static final String REDDIT_REDIRECT_URL = "https://127.0.0.1:65010/authorize_callback";
    public final Credentials credentials = Credentials.installedApp(REDDIT_CLIENT_ID, REDDIT_REDIRECT_URL);
    public Context mContext;

    public AuthenHandler(Context context) {
        mContext = context;
        TokenStore tokenStore = new TokenStore() {
            @Override
            public boolean isStored(String key) {
                return PreferenceManager.getDefaultSharedPreferences(mContext).contains(key);
            }

            @Override
            public String readToken(String key) throws NoSuchTokenException {
                String token = PreferenceManager.getDefaultSharedPreferences(mContext)
                        .getString(key, null);
                if (token == null) {
                    throw new NoSuchTokenException(key);
                }
                return token;
            }

            @Override
            public void writeToken(String key, String token) {
                PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                        .putString(key, token);
            }
        };
        UserAgent myUserAgent = UserAgent.of("android", "com.example.android.redditreader", "v1.0", "karenxx");

        //Android-specific RedditClient
        RedditClient redditClient = new RedditClient(myUserAgent);

        // Store refresh tokens in SharedPreferences
        RefreshTokenHandler handler = new RefreshTokenHandler(tokenStore, redditClient);

        // Initialize the AuthenticationManager singleton
        AuthenticationManager.get().init(redditClient, handler);
        INSTANCE = this;
    }

    public URL getAuthUrl() {
        final OAuthHelper helper = AuthenticationManager.get().getRedditClient().getOAuthHelper();
        String[] scopes = {"identity", "read", "mysubreddits", "vote", "submit"};
        final URL authorizationUrl = helper.getAuthorizationUrl(credentials, true, true, scopes);

        return authorizationUrl;
    }

    public void refreshAuthenTokenAsync() {
        new AsyncTask<android.net.Credentials, Void, Void>() {
            @Override
            protected Void doInBackground(android.net.Credentials... params) {
                try {
                    AuthenticationManager.get().refreshAccessToken(credentials);
                } catch (NoSuchTokenException | OAuthException e) {
                    Log.e(LOG_TAG, "Could not refresh access token ", e);
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void v) {
                Log.d(LOG_TAG, "Reauthenticate");
            }
        }.execute();
    }

}

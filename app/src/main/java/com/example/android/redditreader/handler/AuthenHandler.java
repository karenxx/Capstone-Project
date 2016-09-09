package com.example.android.redditreader.handler;

import android.content.Context;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.RefreshTokenHandler;
import net.dean.jraw.auth.TokenStore;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthHelper;

import java.net.URL;

public class AuthenHandler {
//    private static final AuthenHandler INSTANCE = new AuthenHandler();
//    public static AuthenHandler get() {
//        return INSTANCE;
//    }

    private final String LOG_TAG = AuthenHandler.class.getSimpleName();
    public static final String REDDIT_CLIENT_ID = "MFe5_m-ZqVQAig";
    public static final String REDDIT_REDIRECT_URL = "https://127.0.0.1:65010/authorize_callback";
    public final Credentials credentials = Credentials.installedApp(REDDIT_CLIENT_ID, REDDIT_REDIRECT_URL);
    public Context mContext;
    public TokenStore mTokenStore;


    public AuthenHandler(Context context, TokenStore tokenStore) {
        mContext = context;
        mTokenStore = tokenStore;
        UserAgent myUserAgent = UserAgent.of("android", "com.example.android.redditreader", "v1.0", "karenxx");

        //Android-specific RedditClient
        RedditClient redditClient = new RedditClient(myUserAgent);

        // Store refresh tokens in SharedPreferences
        RefreshTokenHandler handler = new RefreshTokenHandler(mTokenStore, redditClient);

        // Initialize the AuthenticationManager singleton
        AuthenticationManager.get().init(redditClient, handler);
    }

    public URL getAuthUrl() {
        final OAuthHelper helper = AuthenticationManager.get().getRedditClient().getOAuthHelper();
        String[] scopes = {"identity", "read", "mysubreddits", "vote", "submit"};
        final URL authorizationUrl = helper.getAuthorizationUrl(credentials, true, true, scopes);

        return authorizationUrl;
    }



}

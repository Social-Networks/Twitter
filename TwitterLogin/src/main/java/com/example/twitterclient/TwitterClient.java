package com.example.twitterclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


public class TwitterClient {

    /* OAuth provides a method for users to grant third-party (which is my Twitter Application Etmskt in this case) access to
     * their resources without sharing their passwords.
     * It also provides a way to grant limited access (in scope, duration, etc.).
     * For more information, check out this intro link: http://goo.gl/7iidDe */

    /* Consumer/Secret Key paris are part of OAuth protocol and they are generated in your twitter developer console application.
   * TODO: Put it as an string in your strings.xml file to be able to use it for another twitter application in future. */
    private static final String CONSUMER_KEY = "bVjNuwSP1qqL12sKiE2Sg";
    private static final String CONSUMER_SECRET = "zQuauLDHUHJgjDL7b19pnbdNErgCQJLtFStDF2fr4Q";

    /* After finishing authorization process using OAuth protocol, web browser will put callback URL as an input and
    accordingly my activity will be invoked since there is an intent filter for this input once found in web browser. */
    private static final String CALLBACK_SCHEME = "mymy"; /* Typical to what is set in Manifest file. */
    private static final String CALLBACK_URL = CALLBACK_SCHEME + "://nnn"; /* Typical to what is set in Manifest file. */

    /* Key string for the value of callback URL that will be called just after finalizing Twitter authorization,
    * If should be started with previous mentioned scheme. */
    private static final String OAUTH_VERIFIER = "oauth_verifier";

    /* Key string for the preference value that indicates user login state to Twitter through my application.*/
    private static final String PREF_KEY_LOGIN_STATE = "PREF_KEY_LOGIN_STATE";

    /* Key string for the preference value that indicates access token and access token secret. */
    private static final String PREF_KEY_TOKEN = "PREF_KEY_TOKEN";
    private static final String PREF_KEY_TOKEN_SECRET = "PREF_KEY_TOKEN_SECRET";

    /* An instance of request token that is obtained during authorization process from OAUTH key pairs.
     * Then it is used to get the access token that is required before performing any twitter actions. */
    private RequestToken requestToken = null;

    private TwitterFactory twitterFactory;
    private Twitter twitter;

    /* Current context from which my application is running, It is important to be able to start activity within this class.
    * TODO: I am not sure from Java Design point of view whether it is meaningful to define it static or not? */
    private static Context mContext;

    /* An instance of current class that need to be used inside methods of  Asynchronous Tasks.
    * TODO: I am not sure from Java Design point of view whether it is meaningful to define it as static or not? */
    static TwitterClient instance = new TwitterClient(mContext);

    /* An enumeration that holds different states of current user login, It is useful since some methods will be called
    * twice (for example when twitter finishes authentication and get back again to same activity) and in every state,
    * you will do different staff. */
    LoginStateEnum loginState;

    /* Logging tag that will be used within class TwitterClient. */
    private static final String LOG_TAG_TW_CLIENT = "MyTwtClient";

    public TwitterClient(Context context) {
        mContext = context;
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setOAuthConsumerKey(CONSUMER_KEY);
        configurationBuilder.setOAuthConsumerSecret(CONSUMER_SECRET);

        Configuration configuration = configurationBuilder.build();

        twitterFactory = new TwitterFactory(configuration);

        twitter = twitterFactory.getInstance();

        loginState = LoginStateEnum.NEVER_LOGIN_B4;
    }

    public TwitterFactory getTwitterFactory() {
        return twitterFactory;
    }

    public void setTwitterFactory(AccessToken accessToken) {
        twitter = twitterFactory.getInstance(accessToken);
    }

    public Twitter getTwitter() {
        return twitter;
    }

    public RequestToken getRequestToken() {
        if (requestToken == null) {
            try {
                requestToken = twitterFactory.getInstance().getOAuthRequestToken(CALLBACK_URL);
            } catch (TwitterException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return requestToken;
    }

    public static TwitterClient getInstance() {
        return instance;
    }

    public void reset() {
        instance = new TwitterClient(mContext);
    }

    public void authenticateUser(SharedPreferences a_sharedPreferences) {
        /* Check if user already logged in to Twitter successfully through this app before (by checking preference variable).
        * If yes, this is measns that you are ready to do your job,
        * If no, get access token to authenticate the user.
        * In more details:
        * 1- User will be asked to sign in to Twitter with oAuth.
        * 2- At the sign in page of Twitter, if user authenticates himself successfully,
        * 3- he will be redirected to your app Activity.*/

        Log.d(LOG_TAG_TW_CLIENT, "Inside authenticateUser method");
        switch (getLoginState(a_sharedPreferences)) {
            case NEVER_LOGIN_B4:
                Log.i(LOG_TAG_TW_CLIENT, "Current Login State is " + LoginStateEnum.NEVER_LOGIN_B4.toString());


                /* Change Login state variable to be in progress so that when your activity is started again after successful
                * authorization, you do nothing until login state become finish. */
                updateLoginState(a_sharedPreferences, LoginStateEnum.LOGIN_IN_PROGRESS);

                new TwitterAuthenticateTask().execute();
                break;

            case LOGIN_IN_PROGRESS:
                Log.i(LOG_TAG_TW_CLIENT, "Current Login State is " + LoginStateEnum.LOGIN_IN_PROGRESS.toString());
                break;

            case LOGIN_FINISHED:
                Log.i(LOG_TAG_TW_CLIENT, "Current Login State is " + LoginStateEnum.LOGIN_FINISHED.toString());
                break;

            default:
                Log.w(LOG_TAG_TW_CLIENT, "Undefined Login State");
        }
    }

    public void getAccessToken(SharedPreferences a_sharedPreferences) {

        Log.d(LOG_TAG_TW_CLIENT, "Inside getAccessToken method");

        switch (getLoginState(a_sharedPreferences)) {
            case NEVER_LOGIN_B4:
                Log.i(LOG_TAG_TW_CLIENT, "Current Login State is " + LoginStateEnum.NEVER_LOGIN_B4.toString());
                break;

            case LOGIN_IN_PROGRESS:
                Log.i(LOG_TAG_TW_CLIENT, "Current Login State is " + LoginStateEnum.LOGIN_IN_PROGRESS.toString());
                Uri uri = ((Activity) mContext).getIntent().getData();

                /* Make sure that passed intent to activity starts with the same scheme defined before in Manifest file. */
                if (uri != null && uri.toString().startsWith(CALLBACK_URL)) {

                    Log.d(LOG_TAG_TW_CLIENT, "URi is " + uri.toString());

                    /* Get the value of callback URL after finish twitter authentication process. */
                    String verifier = uri.getQueryParameter(OAUTH_VERIFIER);
                    Log.d(LOG_TAG_TW_CLIENT, "verifier is " + verifier);

                    /* Get the access token as last step before performing twitter actions.
                    * Note that access token doesn't have expiration time,
                    * it  will be invalid if a user explicitly rejects your application from their settings or
                    * if a Twitter admin suspends your application. */
                    new TwitterGetAccessTokenTask().execute(verifier);
                }

                break;

            case LOGIN_FINISHED:
                Log.i(LOG_TAG_TW_CLIENT, "Current Login State is " + LoginStateEnum.LOGIN_FINISHED.toString());
                new TwitterGetAccessTokenTask().execute("");
                break;

            default:
                Log.w(LOG_TAG_TW_CLIENT, "Undefined Login State");
                break;
        }


    }

    /* AsyncTask class allows to perform background operations and publish results on the UI thread.
     * It should ideally be used for short operations (a few seconds at the most.)
     * An asynchronous task is defined by 3 generic types, called Params, Progress and Result
     * It would take no parameters but return a RequestToken. */
    private class TwitterAuthenticateTask extends AsyncTask<String, String, RequestToken> {

        @Override
        protected void onPostExecute(RequestToken requestToken) {
            Log.i(LOG_TAG_TW_CLIENT, "After getting access token, start my app activity");
            /* Get Authentication URL from request token. TODO: Change it from http://www.google.com to your Ifraag main website.
             * Note that the intent, that call Twitter Authentication URL, was defined with Intent.ACTION_VIEW.
             * That is an activity action to display the data to the user. It is the generic action you can use on a piece of data
             * to get the most reasonable thing to occur. In our case, the generated URI will invoke web browser.
             * Enter your Twitter log in information. If authentication is successful, Twitter will ask the browser to invoke this data scheme (it must be the same scheme that you entered inside Manifest file)
             * Accordingly, your activity will be notified and invoked because it is registering for that intent filter.
             * Thus you are back again to your Android application. */

            Log.i(LOG_TAG_TW_CLIENT, "Authentication URL is: " + requestToken.getAuthenticationURL());
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL()));
            mContext.startActivity(intent);
        }

        @Override
        protected RequestToken doInBackground(String... params) {
            Log.i(LOG_TAG_TW_CLIENT, "In background, get Request token");
            /* Note that callback scheme is set inside getRequestToken method, so after twitter finished authentication process
            * it will ask the browser to put this data scheme. Accordingly my application activity will be invoked again. */
            return TwitterClient.getInstance().getRequestToken();
        }
    }

    /* AsyncTask class allows to perform background operations and publish results on the UI thread.
     * It should ideally be used for short operations (a few seconds at the most.)
     * An asynchronous task is defined by 3 generic types, called Params, Progress and Result
     * It would take oauth_verifier as an input parameter and returns username of current twitter account. */
    class TwitterGetAccessTokenTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String userName) {
            Log.i(LOG_TAG_TW_CLIENT, "Welcome " + userName);
        }

        @Override
        protected String doInBackground(String... params) {

            Log.i(LOG_TAG_TW_CLIENT, "In background: get access token");
            SystemClock.sleep(100);

            /* Get instances of your twitter object and request token object */
            Twitter twitter = TwitterClient.getInstance().getTwitter();
            RequestToken requestToken = TwitterClient.getInstance().getRequestToken();

            /* param[0] represents oauth_verifier which is passed above inside getAccessToken method.
             * If it is not empty, this means that user has just logged into twitter, activity has been started due to finish of authorization process
             * else if it is empty, this means that activity has been started due to normal operation and access tokens should be restored first before
             * doing any twitter actions. */
            if (params[0].length() != 0) { /* Empty string is passed. isEmpty is added starting from Android API 9 */

                Log.i(LOG_TAG_TW_CLIENT,"activity is started due to fresh login to twitter");

                try {

                    /* Finally get access token and save with final login state. */
                    AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, params[0]);
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

                    /* Save access token and login state into preferences. */
                    saveAccessToken(sharedPreferences, accessToken);
                    updateLoginState(sharedPreferences, LoginStateEnum.LOGIN_FINISHED);

                    /* Get name of current logged-in username. */
                    return twitter.showUser(accessToken.getUserId()).getName();

                } catch (TwitterException e) {
                    e.printStackTrace();
                }
            } else { /* case activity is started normally NOT due to recent Twitter authentication process. */

                Log.i(LOG_TAG_TW_CLIENT,"activity is started normally");

                try {
                    AccessToken accessToken = restoreAccessToken();
                    TwitterClient.getInstance().setTwitterFactory(accessToken);
                    return TwitterClient.getInstance().getTwitter().showUser(accessToken.getUserId()).getName();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }

    private LoginStateEnum getLoginState(SharedPreferences a_sharedPreferences) {

        /* To know if user login to twitter through your application or not, check the value of the following key
        * that is saved in the shared preferences of your application. */
        String loginState = a_sharedPreferences.getString(PREF_KEY_LOGIN_STATE, LoginStateEnum.NEVER_LOGIN_B4.toString());

        /* Enum is java by default converts strings to enum object using valueof method. */
        return LoginStateEnum.valueOf(loginState);
    }

    private void saveAccessToken (SharedPreferences a_sharedPreferences, AccessToken a_accessToken){

        SharedPreferences.Editor editor = a_sharedPreferences.edit();

        editor.putString(PREF_KEY_TOKEN, a_accessToken.getToken());
        editor.putString(PREF_KEY_TOKEN_SECRET, a_accessToken.getTokenSecret());

        editor.commit();
    }

    private AccessToken restoreAccessToken () {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String accessTokenString = sharedPreferences.getString(PREF_KEY_TOKEN, "");
        String accessTokenSecret = sharedPreferences.getString(PREF_KEY_TOKEN_SECRET, "");

        if ((accessTokenString.length() != 0) &&
                (accessTokenSecret.length() != 0)) {

            /* Restore access token from saved values in shared preferences. */
            return new AccessToken(accessTokenString, accessTokenSecret);

        } else{
            return null;
        }
    }

    private void updateLoginState(SharedPreferences a_sharedPreferences, LoginStateEnum a_loginState) {

        Log.d(LOG_TAG_TW_CLIENT, "Inside updateLoginState: Login State is " + a_loginState.toString());
        SharedPreferences.Editor editor = a_sharedPreferences.edit();
        editor.putString(PREF_KEY_LOGIN_STATE, a_loginState.toString());
        editor.commit();
    }

    private enum LoginStateEnum {
        NEVER_LOGIN_B4(1), LOGIN_IN_PROGRESS(2), LOGIN_FINISHED(3);

        private int value;

        private LoginStateEnum(int a_value) {
            this.value = a_value;
        }

        /*public int getValue() {
            return value;
        }

        @Override
        public String toString() {
            switch (this){
                case NEVER_LOGIN_B4:
                    return "NEVER_LOGIN_B4";
                case LOGIN_IN_PROGRESS:
                    return "LOGIN_IN_PROGRESS";
                case LOGIN_FINISHED:
                    return "LOGIN_FINISHED";
                default:
                    return "Undefined LOGIN state";
            }
        }*/
    }

    public void getUserProfileInformation(){
        /*TODO: It is worth implementing this method. As analogous to Facebook one. */
    }

    public void updateStatusTxt(String a_msgTxt){

        /*TODO: Make sure that the given text message characters does not exceed current twitter limit of 140 character.
        * What about URLs, is it more optimized to use short expaned_urls in the displayed_url or not?
        * Note that you can't tweet two typical tweets sequentially. You will get an error in the 2nd tweet. */
        Log.i(LOG_TAG_TW_CLIENT, "Inside updateStateTxt ");
        new TwitterUpdateStatusTask().execute(a_msgTxt);
    }

    class TwitterUpdateStatusTask extends AsyncTask<String, String, Boolean> {

        @Override
        protected void onPostExecute(Boolean result) {
            if (result)
                Log.i(LOG_TAG_TW_CLIENT, "Tweet successfully");
            else
                Log.i(LOG_TAG_TW_CLIENT, "Tweet failed");
        }

        @Override
        protected Boolean doInBackground(String... params) {

            Log.d(LOG_TAG_TW_CLIENT, "do In background, update status");
            SystemClock.sleep(100);
            try{
                /*SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                String accessTokenString = sharedPreferences.getString(PREF_KEY_TOKEN, "");
                String accessTokenSecret = sharedPreferences.getString(PREF_KEY_TOKEN_SECRET, "");

                if ((accessTokenString.length()!=0) &&
                        (accessTokenSecret.length() !=0)) {
                    AccessToken accessToken = new AccessToken(accessTokenString, accessTokenSecret);*/
                AccessToken accessToken = restoreAccessToken();
                twitter4j.Status status = TwitterClient.getInstance().getTwitterFactory().getInstance(accessToken).updateStatus(params[0]);
                return true;
                /*}*/

            } catch (TwitterException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return false;  //To change body of implemented methods use File | Settings | File Templates.

        }
    }

}

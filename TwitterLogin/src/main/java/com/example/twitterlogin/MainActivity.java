package com.example.twitterlogin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import com.example.connection.ConnectionDetector;
import com.example.twitterclient.TwitterClient;

import twitter4j.Twitter;
import twitter4j.auth.RequestToken;


public class MainActivity extends ActionBarActivity {

    private static final String LOG_TAG_TW_CLIENT = "MyTwtClient";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        /* Check if internet connection is present whether through WiFi or Network */
        if (ConnectionDetector.isConnectionAvailable(getApplicationContext())) {
            Log.i(LOG_TAG_TW_CLIENT, "Internet Connection is available");
        } else {
            Log.i(LOG_TAG_TW_CLIENT, "Internet Connection isN'T available");
            MyAlertDialog.show(MainActivity.this, "Internet connection", "A valid internet connection can't be established");
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        TwitterClient twitterClient = new TwitterClient(this);

        /* Since each of these operations would run in an asynchronous task, a sleep duration for 100 msec is added in each
        * doInBackground method. */
        twitterClient.authenticateUser(sharedPreferences);
        twitterClient.getAccessToken(sharedPreferences);

        /* In my real application, I will never login to twitter through my application and update user status directly
        * so when running this app in a new device, status will fail but when you tried once again it should succeed. */

        /*Google map URL by this format can be opened on Desktop but not working on my phone.
        * When I past the link url in my browser, it asked me that browser would like to access my location then I approve.
        * Afterwards, when I tried to tweet again, the location url: open Google Maps application , get my current location then
        * moves forward to my the place marked in my URL.
        * TODO: Tell users in presentation video to open URL in web browser themselves. */

        /* Note that twitter will take care of converting your long URL just into restricted number of characters (expanded_url vs displayed_url)*/
        twitterClient.updateStatusTxt("My Text Msg: "+ "https://maps.google.com/maps?q=30.0380279%2C31.2405339&z=15"+ " My txt3 #MyHashTag");

    }

    /* AsyncTask class allows to perform background operations and publish results on the UI thread.
    * It should ideally be used for short operations (a few seconds at the most.)
    * An asynchronous task is defined by 3 generic types, called Params, Progress and Result*/
/*    private class TwitterAuthenticateTask extends AsyncTask<String, String, RequestToken> {

        @Override
        protected void onPostExecute(RequestToken requestToken) {
            Log.i(LOG_TAG_TW_CLIENT, "After getting access token, start my app activity");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL()));
            startActivity(intent);
        }

        @Override
        protected RequestToken doInBackground(String... params) {
            Log.i(LOG_TAG_TW_CLIENT, "In background, get access token");
            return TwitterClient.getInstance().getRequestToken();
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}

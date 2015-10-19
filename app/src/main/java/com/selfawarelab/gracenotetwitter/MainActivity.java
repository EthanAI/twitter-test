package com.selfawarelab.gracenotetwitter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.AppSession;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Coordinates;
import com.twitter.sdk.android.core.models.Place;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.TweetEntities;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetui.CompactTweetView;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetViewFetchAdapter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import io.fabric.sdk.android.Fabric;

/*
The problem is to write an Android app which displays tweets and corresponding images.
First, get a list of tweets from Twitter (however you like- latest trends, your own feed, etc.).
Next, use a word in the tweet or the location of the tweet or some other attribute of the tweet to
mash up the tweet list with images from some public source such as Flickr or Google Image Search.
You could, for example, match the first word of the tweet with a tag on a Flickr image.
The result should be a list of tweets, each with an associated image. Your app should display this
list to the user in some appropriate way.
Finally, your app should allow the user to switch between Alphabetical and Date-based ordering of
the tweets. For the Alphabetical ordering, just think of the entire tweet as a single String. For
the Date ordering, just use the creation Date / Time of the tweet. The reorder should just reorder
the existing content. It should not require any network calls.
As a hint, you will probably want to make some classes whose instances will function as in-memory
data models. Your models will represent several different data compositions including a composite
item that is a combination of two other kinds of items, as well a composite item that is a list of
another kind of item.
When you finish, reply back with a short explanation of your design decisions and an archive of
your code attached. You may use any third party libraries that help, just document this in your design decision
explanation.

    -Fetch tweets
        -Text
        -Date created
        -Sortable
    -Fetch images. Flickr / Wikimedia. This should be async
    -

 */

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "3m2UoJLnYMmNU5K5K4RdRSksu";
    private static final String TWITTER_SECRET = "iPsak6oEBmerVqrIDvrJzdAfDgdWURkkDfF8o0GdlzqLEGeCqc";

    // UI Elements
    private ListView listView;
    private ArrayAdapter<ProcessedTweet> arrayAdapter;

    // Data
    private ArrayList<ProcessedTweet> tweets = new ArrayList<>();

    // Buttons
    public void sortListByDate(View view) {
        arrayAdapter.sort(new Comparator<ProcessedTweet>() {
            @Override
            public int compare(ProcessedTweet t1, ProcessedTweet t2) {
                int compareResult = 0; // No sorting when there is an error

                try {
                    // Get date from the date string
                    String dateString1 = t1.createdAt;
                    String dateString2 = t2.createdAt;
                    SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss '+0000' yyyy");
                    Date date1 = format.parse(dateString1);
                    Date date2 = format.parse(dateString2);

                    compareResult = date1.compareTo(date2);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                return compareResult;
            }
        });
    }

    public void sortListByText(View view) {
        arrayAdapter.sort(new Comparator<ProcessedTweet>() {
            @Override
            public int compare(ProcessedTweet t1, ProcessedTweet t2) {
                return t1.text.compareTo(t2.text);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Twitter twitter = new Twitter(authConfig);
        Fabric.with(this, twitter);

        getTweets();
    }

    public void getTweets() {
        TwitterCore.getInstance().logInGuest(new Callback<AppSession>() {
            @Override
            public void success(Result<AppSession> appSessionResult) {
                AppSession session = appSessionResult.data;
                TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient(session);
                twitterApiClient.getStatusesService().userTimeline(null, "seinfeldtoday", 10, null, null, false, false, false, true, new Callback<List<Tweet>>() {
                    @Override
                    public void success(Result<List<Tweet>> listResult) {
                        List<Tweet> tweetList = listResult.data;
//                        Log.d(TAG, "got tweets: " + tweetList);
                        for (Tweet tweet : tweetList) {
                            Log.d(TAG, tweet.createdAt + " " + tweet.text);
                        }

                        processTweets(tweetList);
                    }

                    @Override
                    public void failure(TwitterException e) {
                        Log.d(TAG, "Could not retrieve tweets");
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void failure(TwitterException e) {
                Log.d(TAG, "Could not get guest Twitter session");
                e.printStackTrace();
            }
        });
    }

    public void processTweets(List<Tweet> tweetList) {
        // Fetch photo for each tweet and store in a mutable arrayList
        for(Tweet tweet : tweetList) {
            tweets.add(new ProcessedTweet(tweet));
        }

        arrayAdapter = new TweetAdapter(this, android.R.layout.simple_list_item_1, tweets);
        listView.setAdapter(arrayAdapter);
    }





}

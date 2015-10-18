package com.selfawarelab.gracenotetwitter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.AppSession;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetui.CompactTweetView;
import com.twitter.sdk.android.tweetui.TweetUtils;
import com.twitter.sdk.android.tweetui.TweetViewFetchAdapter;

import java.util.List;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Twitter twitter = new Twitter(authConfig);
        Fabric.with(this, twitter);

        getTweets();

    }

    public void getTweets() {
        // TODO: Base this Tweet ID on some data from elsewhere in your app
//        long tweetId = 631879971628183552L;
        long tweetId = 655116638522466304L;
//        TweetUtils.loadTweet(tweetId, new Callback<Tweet>() {
//            @Override
//            public void success(Result<Tweet> result) {
//                Log.d("TwitterKit", "Success: " + result.data.createdAt + " " + result.data.text);
//            }
//
//            @Override
//            public void failure(TwitterException exception) {
//                Log.d("TwitterKit", "Load Tweet failure", exception);
//            }
//        });

//        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
//        StatusesService statusesService = twitterApiClient.getStatusesService();
//        statusesService.userTimeline(null, "screen_name=twitterapi&count=2", null, null, null, null, null, null, null, new Callback<List<Tweet>>() {
//            @Override
//            public void success(Result<List<Tweet>> result) {
//                List<Tweet> tweets = result.data;
//                Log.d(TAG, "got tweets: " + tweets);
//            }
//
//            @Override
//            public void failure(TwitterException e) {
//                Log.d(TAG, "Failed to get tweets");
//            }
//        });

        TwitterCore.getInstance().logInGuest( new Callback<AppSession>() {
            @Override
            public void success(Result<AppSession> appSessionResult) {
                AppSession session = appSessionResult.data;
                TwitterApiClient twitterApiClient =  TwitterCore.getInstance().getApiClient(session);
                twitterApiClient.getStatusesService().userTimeline(null, "seinfeldtoday", 10, null, null, false, false, false, true, new Callback<List<Tweet>>() {
                    @Override
                    public void success(Result<List<Tweet>> listResult) {
                        List<Tweet> tweets = listResult.data;
                        Log.d(TAG, "got tweets: " + tweets);
                        for(Tweet tweet : tweets) {
                            Log.d(TAG, tweet.createdAt + " " + tweet.text);
                        }
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

}

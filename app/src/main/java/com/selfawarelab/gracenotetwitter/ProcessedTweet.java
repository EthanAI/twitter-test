package com.selfawarelab.gracenotetwitter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.twitter.sdk.android.core.models.Tweet;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Created by esmith on 10/18/15.
 */
public class ProcessedTweet extends Tweet {
    private final String TAG = this.getClass().getSimpleName();
    
    private static final String GETTY_KEY = "xyx4eqnrgc8gn8xr8762zqv8";
    private static final String GETTY_SECRET = "BxfERGMCRmtfyewE4zrYSpxUf3cdN8Gj2fs8Q9fv2GMjc";

    String queryTerm;
    String photoUrl;
    Bitmap bitmap;
    ProcessedTweet(Tweet tweet) {
        super(tweet.coordinates, tweet.createdAt, tweet.currentUserRetweet, tweet.entities, tweet.favoriteCount,
                tweet.favorited, tweet.filterLevel, tweet.id, tweet.idStr, tweet.inReplyToScreenName,
                tweet.inReplyToStatusId, tweet.inReplyToStatusIdStr, tweet.inReplyToUserId, tweet.inReplyToUserIdStr,
                tweet.lang, tweet.place, tweet.possiblySensitive, tweet.scopes, tweet.retweetCount,
                tweet.retweeted, tweet.retweetedStatus, tweet.source, tweet.text, tweet.truncated,
                tweet.user, tweet.withheldCopyright, tweet.withheldInCountries, tweet.withheldScope);

        queryTerm = getQueryTerm(tweet.text);
        photoUrl = getPhotoUrl(queryTerm);
        bitmap = getBitmap(photoUrl);
    }

    private String getQueryTerm(String text) {
        String[] words = text.split(" ");
        String query = words[0];
        return query;
    }

    private String getPhotoUrl(String queryTerm) {
        String photoUrl = "";
        final String queryUrl = "https://api.gettyimages.com/v3/search/images?fields=id,title,thumb,referral_destinations&sort_order=best&phrase=" + queryTerm;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<String> callable = new Callable<String>() {
            public String call() throws Exception {
                String response = httpGet(queryUrl);
                return response;
            }
        };

        FutureTask<String> futureTask = new FutureTask<String>(callable);
        executor.execute(futureTask);

        try {
            String result = futureTask.get();

            // Process result. Getty Images gives JSON
            JSONObject jsonObject = new JSONObject(result);
            JSONArray imageJSONArray = jsonObject.getJSONArray("images");
            JSONObject firstImageJSONObject = jsonObject.getJSONArray("images").getJSONObject(0);
            photoUrl = firstImageJSONObject.getJSONArray("display_sizes").getJSONObject(0).getString("uri");

            Log.d(TAG, "JSON: " + firstImageJSONObject.toString());
            Log.d(TAG, "Image uri: " + photoUrl);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return photoUrl;
    }

    private Bitmap getBitmap(final String urlString) {
        Bitmap bitmap = null;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Bitmap> callable = new Callable<Bitmap>() {
            public Bitmap call() throws Exception {
                Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(urlString).getContent());
                return bitmap;
            }
        };

        FutureTask<Bitmap> futureTask = new FutureTask<Bitmap>(callable);
        executor.execute(futureTask);

        try {
            bitmap = futureTask.get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public String httpGet(String url) {
        String responseString = "";

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams()); // Hmmm..
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Api-Key", GETTY_KEY);

            HttpResponse response = httpClient.execute(httpGet);
            Log.d(TAG, "Response status: " + response.getStatusLine());
            responseString = EntityUtils.toString(response.getEntity());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseString;
    }
}


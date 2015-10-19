package com.selfawarelab.gracenotetwitter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by esmith on 10/18/15.
 */
public class TweetAdapter extends ArrayAdapter<ProcessedTweet> {
    private final String TAG = this.getClass().getSimpleName();

    private final Context context;
    private final ArrayList<ProcessedTweet> processedTweets;

    public TweetAdapter(Context context, int resource, ArrayList<ProcessedTweet> tweets) {
        super(context, resource, tweets);
        this.context = context;
        this.processedTweets = tweets;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item, parent, false);
        ProcessedTweet tweet = processedTweets.get(position);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
        TextView dateTextView = (TextView) rowView.findViewById(R.id.dateTextView);
        TextView textView = (TextView) rowView.findViewById(R.id.textView);

        //Log.d(TAG, "Image size: " + tweet.bitmap.getByteCount());

        imageView.setImageBitmap(tweet.bitmap);

        dateTextView.setText(tweet.date.toString());
        textView.setText(tweet.text);

        return rowView;
    }

}

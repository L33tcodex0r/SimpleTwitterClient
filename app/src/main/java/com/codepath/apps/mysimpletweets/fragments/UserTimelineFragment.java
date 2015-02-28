package com.codepath.apps.mysimpletweets.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.mysimpletweets.TwitterApplication;
import com.codepath.apps.mysimpletweets.TwitterClient;
import com.codepath.apps.mysimpletweets.helpers.EndlessScrollListener;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserTimelineFragment extends TweetsListFragment {
    private static TwitterClient client;
    String screenName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get the client.
        client = TwitterApplication.getRestClient(); //singleton client
        populateTimeline();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, parent, savedInstanceState);

        lvTweets.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                getMoreTweets(screenName);
            }
        });
        return v;
    }

    //UserTimelineFragment.newInstance("someScreenName")
    public static UserTimelineFragment newInstance(String screen_name) {
        UserTimelineFragment userFragment = new UserTimelineFragment();
        Bundle args = new Bundle();
        args.putString("screen_name", screen_name);
        userFragment.setArguments(args);
        return userFragment;
    }

    //Send an API request to get the timeline json
    //Fill the listview by creating the tweet objects from the json
    private void populateTimeline() {
        screenName = getArguments().getString("screen_name");
        client.getUserTimeline(screenName, new JsonHttpResponseHandler() {
            //Success
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray json) {
                Log.d("DEBUG", "successful");
                aTweets.addAll(Tweet.fromJSONArray(json));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("DEBUG", errorResponse.toString());
            }
        });
    }

    public void getMoreTweets(String screenName) {
        client.getMoreTweets(screenName, new JsonHttpResponseHandler() {
            //Success
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray json) {
                aTweets.addAll(Tweet.fromJSONArray(json));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            }
        }, getLowestId() - 1);
    }

    public long getLowestId() {
        int length = aTweets.getCount();
        long minId = Long.MAX_VALUE;
        for (int i = 0; i < length; i++) {
            long currentId = aTweets.getItem(i).getId();
            if (currentId < minId) {
                minId = currentId;
            }
        }
        return minId;
    }
}

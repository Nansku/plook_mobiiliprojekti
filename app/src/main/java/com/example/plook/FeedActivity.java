package com.example.plook;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class FeedActivity extends AppCompatActivity
{
    int postAmount = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        ViewGroup content = findViewById(R.id.feed_content);

        for(int i = 0; i < postAmount; i++)
        {
            View child = getLayoutInflater().inflate(R.layout.layout_feed_post, content, false);
            content.addView(child);
        }
    }
}
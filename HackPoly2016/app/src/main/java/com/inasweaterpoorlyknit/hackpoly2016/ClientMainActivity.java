package com.inasweaterpoorlyknit.hackpoly2016;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ClientMainActivity extends AppCompatActivity {
    private static final int SEARCH_CODE = 2;
    String foundSongID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_main);

        final FloatingActionButton searchButton = (FloatingActionButton) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchSong();
            }
        });
    }

    public void searchSong() {
        Intent searchIntent = new Intent(this, SearchActivity.class);
        searchIntent.putExtra("foundSong", foundSongID);
        startActivityForResult(searchIntent, SEARCH_CODE);
    }
}

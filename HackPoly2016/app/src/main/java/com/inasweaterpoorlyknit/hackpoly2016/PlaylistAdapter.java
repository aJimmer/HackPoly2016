package com.inasweaterpoorlyknit.hackpoly2016;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Displays a list of youtube video titles and thumbnails
 * An adapter used for a listview in PlaylistFragment and SearchFragment
 * [does NOT currently keep track of song ids]
 */
public class PlaylistAdapter extends ArrayAdapter<String> {
    private final Activity context; // activity the fragment was called from
    
    private final ArrayList<String> songTitles; // pointer to an ArrayList of songTitles that exists in the activity
    private final ArrayList<Bitmap> songThumbnails; //pointer to an arraylist of previous downloaded thumbnails, hopefully will speed up the list generation

    // public constructor
    public PlaylistAdapter(Activity context, ArrayList<String> songTitles, ArrayList<Bitmap> songThumbnails) {
        super(context, R.layout.song_row, songTitles);
        
        this.context = context; // set the context to the activity that called it
        this.songTitles = songTitles; // set the member songTitles to the passed songTitles
        this.songThumbnails = songThumbnails;
    }

    
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.song_row, null, true);
        if (songThumbnails.size() > position) {
            TextView songTitle = (TextView) rowView.findViewById(R.id.song_title); // get access to the row's song_title
            songTitle.setText(songTitles.get(position));    // set the text of the row's song_title to the song title at correct position
            ImageView songThumbnail = (ImageView) rowView.findViewById(R.id.video_thumbnail);
            songThumbnail.setImageBitmap(songThumbnails.get(position));
        }
        return rowView; // return the rowView that was created
    }
}

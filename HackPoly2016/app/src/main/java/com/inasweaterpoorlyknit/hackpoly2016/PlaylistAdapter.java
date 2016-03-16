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
    private final ArrayList<String> thumbnailURLs; // pointer to an ArrayList of songThumbnails that exists in the activity

    // public constructor
    public PlaylistAdapter(Activity context, ArrayList<String> songTitles, ArrayList<String> thumbnailURLs){
        super(context, R.layout.song_row, songTitles);
        
        this.context = context; // set the context to the activity that called it
        this.songTitles = songTitles; // set the member songTitles to the passed songTitles
        this.thumbnailURLs = thumbnailURLs; // set the member thumbnailURLs to the passed thumbnailURLs
    }

    
    @Override
    public View getView(int position, View view, ViewGroup parent){
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.song_row, null, true);

        if(thumbnailURLs != null) { // if there are thumbnail URLs(and any songs on the playlist)
            TextView songTitle = (TextView) rowView.findViewById(R.id.song_title); // get access to the row's song_title
            songTitle.setText(songTitles.get(position));    // set the text of the row's song_title to the song title at correct position
            new DownloadImageTask((ImageView) rowView.findViewById(R.id.video_thumbnail))
                    .execute(thumbnailURLs.get(position));  // download the thumbnail for that song and set as bitmap for the imageview
        }
        return rowView; // return the rowView that was created
    }

    // AsyncTask to download the thumbnail images
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;  // image view to add the image to

        // publiv constructor
        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage; // initialize the member ImageView to the one passed 
        }

        // downloads any number of URLs in the background
        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];    // save the first url
            Bitmap thumbnail = null;          // thumbnail, set to null
            try {
                InputStream in = new java.net.URL(urlDisplay).openStream(); // get an input stream from specified url
                thumbnail = BitmapFactory.decodeStream(in);   // decode the inputStream as a Bitmap
            } catch (Exception e) { // printe any errors
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return thumbnail;   // return the thumbnail
        }

        // called after bitmap is loaded and returned from doInBackground()
        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result); // set the bitmap image in the bitmap view
        }
    }
}

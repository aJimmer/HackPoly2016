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
 * Created by Connor on 3/5/2016.
 */
public class PlaylistAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final ArrayList<String> songTitles;
    private final ArrayList<String> thumbnailURLs;

    public PlaylistAdapter(Activity context, ArrayList<String> songTitles, ArrayList<String> thumbnailURLs){
        super(context, R.layout.song_row, songTitles);
        this.context = context;
        this.songTitles = songTitles;
        this.thumbnailURLs = thumbnailURLs;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent){
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.song_row, null, true);

        if(thumbnailURLs != null) {
            TextView songTitle = (TextView) rowView.findViewById(R.id.song_title);
            songTitle.setText(songTitles.get(position));
            new DownloadImageTask((ImageView) rowView.findViewById(R.id.video_thumbnail))
                    .execute(thumbnailURLs.get(position));
        }

        return rowView;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urlDisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}

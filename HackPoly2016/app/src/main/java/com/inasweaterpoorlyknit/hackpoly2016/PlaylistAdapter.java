package com.inasweaterpoorlyknit.hackpoly2016;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Displays a list of youtube video titles and thumbnails
 * An adapter used for a listview in PlaylistFragment and SearchFragment
 * [does NOT currently keep track of song ids]
 */
public class PlaylistAdapter extends ArrayAdapter<SongData> {

    private final Activity context; // activity the fragment was called from
    private ArrayList<SongData> songList;
    SongData holder = new SongData();

    public PlaylistAdapter(Activity context, ArrayList<SongData> songList) {

        super(context, R.layout.song_row, songList);
        this.context = context; // set the context to the activity that called it
        this.songList = songList;
    }

    public Activity getContext(){
        return context;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        view = inflater.inflate(R.layout.song_row, null, true);

        if (songList.size() > position) {


            holder.rowdata = songList.get(position);
            holder.title = (TextView) view.findViewById(R.id.song_title);
            holder.title.setText(holder.rowdata.songTitle); //CAN ANYONE CHECK THIS OUT FOR ME????
            holder.thumbnail = (ImageView) view.findViewById(R.id.video_thumbnail);
            holder.thumbnail.setImageBitmap(holder.rowdata.songThumbnail);

        }


        return view; // return the rowView that was created
    }

}
package com.inasweaterpoorlyknit.hackpoly2016;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Displays a list of youtube video titles and thumbnails
 * An adapter used for a listview in PlaylistFragment and SearchFragment
 * [does NOT currently keep track of song ids]
 */
public class PlaylistAdapter extends ArrayAdapter<SongData> {

    private final Activity context; // activity the fragment was called from
    private final ArrayList<SongData> songList;

    public PlaylistAdapter(Activity context, ArrayList<SongData> songList) {

        super(context, R.layout.song_row, songList);
        this.context = context; // set the context to the activity that called it
        this.songList = songList;
    }


    @Override
    public View getView(int position, View view, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        view = inflater.inflate(R.layout.song_row, null, true);

        if (songList.size() > position) {

            final SongData holder = new SongData();

            holder.rowdata = songList.get(position);
            holder.title = (TextView) view.findViewById(R.id.song_title);
            holder.title.setText(holder.rowdata.songTitle); //CAN ANYONE CHECK THIS OUT FOR ME????
            holder.thumbnail = (ImageView) view.findViewById(R.id.video_thumbnail);
            holder.thumbnail.setImageBitmap(holder.rowdata.songThumbnail);
            holder.button = (Button) view.findViewById(R.id.upvoteButton);
            holder.button.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    holder.voteCount++;
                    holder.button.setText("UPVOTED: " + holder.voteCount);

                }
            });

        }
        return view; // return the rowView that was created
    }
}
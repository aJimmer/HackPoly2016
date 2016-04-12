package com.inasweaterpoorlyknit.hackpoly2016;

import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
/**
 * Created by Jimenez on 3/30/16.
 */

public class SongData {

    public SongData rowdata;
    public String songTitle;
    public String songID;
    public String songThumbnailURL;
    public Bitmap songThumbnail;

    public ImageView thumbnail;
    public TextView title;
    public Button button;
    public int voteCount = 0;

    SongData(){

    }

}
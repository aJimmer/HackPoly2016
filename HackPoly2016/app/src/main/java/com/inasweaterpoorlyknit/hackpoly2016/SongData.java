package com.inasweaterpoorlyknit.hackpoly2016;

import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
/**
 * Created by Jimenez on 3/30/16.
 */

public class SongData /*implements Comparable<SongData>*/{

    public SongData rowdata;
    public String songTitle;
    public String songID;
    public String songThumbnailURL;
    public Bitmap songThumbnail;

    public ImageView thumbnail;
    public TextView title;
    private int voteCount = 0;

    SongData(){

    }

    public void incrementVote(){
        voteCount++;
    }
    public void decrementVote(){
        voteCount--;
    }
    public int getVoteCount(){
        return voteCount;
    }

}
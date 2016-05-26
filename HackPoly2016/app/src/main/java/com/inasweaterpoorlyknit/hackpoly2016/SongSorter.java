package com.inasweaterpoorlyknit.hackpoly2016;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Jimenez on 5/23/16.
 */
public class SongSorter {
    ArrayList<SongData> songList = new ArrayList<>();

    public SongSorter(ArrayList<SongData> songList){
        this.songList = songList;
    }
    public ArrayList<SongData> getSortedSongsByVote() {
        Collections.sort(songList);
        return songList;
    }
}

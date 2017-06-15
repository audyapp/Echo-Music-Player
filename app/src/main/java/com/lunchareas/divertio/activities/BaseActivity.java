package com.lunchareas.divertio.activities;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

import com.lunchareas.divertio.fragments.CreatePlaylistNameFailureDialog;
import com.lunchareas.divertio.R;
import com.lunchareas.divertio.models.PlaylistDBHandler;
import com.lunchareas.divertio.models.PlaylistData;
import com.lunchareas.divertio.models.SongDBHandler;
import com.lunchareas.divertio.models.SongData;
import com.lunchareas.divertio.utils.PlaylistUtil;
import com.lunchareas.divertio.utils.SongUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getName();

    private int id;
    protected AudioManager am;
    protected List<SongData> songInfoList;
    protected List<PlaylistData> playlistInfoList;

    protected SongUtil songUtil;
    protected PlaylistUtil playlistUtil;

    protected Intent musicCreateIntent;
    protected Intent musicStartIntent;
    protected Intent musicPauseIntent;
    protected Intent musicChangeIntent;

    protected boolean musicBound;

    protected Context context;

    /*
    Probably should find a better way of initializing the view.
     */
    public BaseActivity(int id) {
        this.id = id;
    }

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add fonts
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
        .setDefaultFontPath("fonts/Lato-Medium.ttf")
        .setFontAttrId(R.attr.fontPath)
        .build());

        // Differs due to different activities
        setContentView(id);

        // Get context
        context = getApplicationContext();

        // Create utils
        songUtil = new SongUtil(context);
        playlistUtil = new PlaylistUtil(context);

        // Get all playlists
        PlaylistDBHandler dbPlaylist = new PlaylistDBHandler(this);
        playlistInfoList = dbPlaylist.getPlaylistDataList();

        // Get song info
        SongDBHandler db = new SongDBHandler(this);
        songInfoList = db.getSongDataList();
    }

    public abstract void setMainView();

    public void sendMusicCreateIntent(String name) {
        musicCreateIntent = new Intent(this, PlayMediaService.class);
        musicCreateIntent.setAction(PlayMediaService.MUSIC_CREATE);
        musicCreateIntent.putExtra(PlayMediaService.MUSIC_CREATE, name);
        this.startService(musicCreateIntent);
    }

    public void sendMusicStartIntent() {
        musicStartIntent = new Intent(this, PlayMediaService.class);
        musicStartIntent.setAction(PlayMediaService.MUSIC_PLAY);
        this.startService(musicStartIntent);
    }

    public void sendMusicPauseIntent() {
        musicPauseIntent = new Intent(this, PlayMediaService.class);
        musicPauseIntent.setAction(PlayMediaService.MUSIC_PAUSE);
        this.startService(musicPauseIntent);
    }

    public void sendMusicChangeIntent(int position) {
        musicChangeIntent = new Intent(this, PlayMediaService.class);
        musicChangeIntent.setAction(PlayMediaService.MUSIC_CHANGE);
        musicChangeIntent.putExtra(PlayMediaService.MUSIC_CHANGE, position);
        this.startService(musicChangeIntent);
    }
    public void sendListCreateIntent(List<SongData> songList) {
        Intent playlistCreateIntent = new Intent(context, PlayMediaService.class);

        // Shuffle list, get first song
        //SongData firstSong = songList.get(firstPos);
        Collections.shuffle(songList);

        // Create the string array
        String[] songNameList = new String[songList.size()];
        for (int i = 0; i < songList.size(); i++) {
            songNameList[i] = songList.get(i).getSongName();
        }

        // Send the intent
        playlistCreateIntent.setAction(PlayMediaService.PLAYLIST_CREATE);
        playlistCreateIntent.putExtra(PlayMediaService.PLAYLIST_CREATE, songNameList);
        context.startService(playlistCreateIntent);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public List<SongData> getSongInfoList() {
        updateSongInfoList();
        return this.songInfoList;
    }

    public List<PlaylistData> getPlaylistInfoList() {
        updatePlaylistInfoList();
        return this.playlistInfoList;
    }

    public void updateSongInfoList() {
        SongDBHandler db = new SongDBHandler(getApplicationContext());
        this.songInfoList = db.getSongDataList();
    }

    public void updatePlaylistInfoList() {
        PlaylistDBHandler db = new PlaylistDBHandler(getApplicationContext());
        this.playlistInfoList = db.getPlaylistDataList();
    }

    public void createPlaylistNameFailureDialog() {
        DialogFragment dialogFragment = new CreatePlaylistNameFailureDialog();
        dialogFragment.show(getSupportFragmentManager(), "CreatePlaylistNameFailure");
    }

    public List<SongData> getSongsFromIndexes(List<Integer> songIdxList) {
        List<SongData> songList = new ArrayList<>();
        for (Integer integer: songIdxList) {
            songList.add(songInfoList.get(integer));
        }

        return songList;
    }

    public List<PlaylistData> getPlaylistsFromIndexes(List<Integer> playlistIdxList) {
        List<PlaylistData> playlistList = new ArrayList<>();
        for (Integer integer: playlistIdxList) {
            playlistList.add(playlistInfoList.get(integer));
        }

        return playlistList;
    }
}

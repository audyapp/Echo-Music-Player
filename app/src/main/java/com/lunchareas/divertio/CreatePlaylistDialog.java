package com.lunchareas.divertio;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class CreatePlaylistDialog extends DialogFragment {

    private View createPlaylistView;
    private View createPlaylistTitle;
    private ArrayList<String> songInfoTemp;
    private ArrayList<Integer> selectedSongs;
    private ArrayList<SongData> songInfoList;
    private EditText playlistNameInput;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // get the list of songs to pick from
        songInfoList = ((PlaylistActivity)getActivity()).getSongInfoList();
        songInfoTemp = new ArrayList<>();
        for (int i = 0; i < songInfoList.size(); i++) {
            songInfoTemp.add(songInfoList.get(i).getSongName());
        }

        String[] songList = new String[songInfoTemp.size()];
        songList = songInfoTemp.toArray(songList);

        // get name of playlist and list of songs
        selectedSongs = new ArrayList<>();
        AlertDialog.Builder createPlaylistBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        createPlaylistView = inflater.inflate(R.layout.create_playlist_dialog, null);
        createPlaylistTitle = inflater.inflate(R.layout.create_playlist_title, null);
        createPlaylistBuilder
                .setView(createPlaylistView)
                .setCustomTitle(createPlaylistTitle)
                .setMultiChoiceItems(songList, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                        if (isChecked) {
                            selectedSongs.add(position);
                            System.out.println("Adding position " + position);
                        } else if (selectedSongs.contains(position)) {
                            selectedSongs.remove(Integer.valueOf(position));
                            System.out.println("Removing position " + position);
                        }
                    }
                })
                .setPositiveButton(R.string.create_playlist_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        executePlaylistCreate();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        System.out.println("Canceled creation...");
                    }
                });

        return createPlaylistBuilder.create();
    }

    private void executePlaylistCreate() {

        // create a list of the song data
        List<SongData> songDataList = new ArrayList<>();
        for (int i = 0; i < selectedSongs.size(); i++) {
            SongData songData = songInfoList.get(selectedSongs.get(i));
            songDataList.add(songData);
        }

        // get name from input
        playlistNameInput = (EditText) createPlaylistView.findViewById(R.id.dialog_create_playlist_name);
        String playlistName = playlistNameInput.getText().toString().trim();
        System.out.println("Playlist name: " + playlistName);

        // update database with new playlist
        PlaylistDBHandler db = new PlaylistDBHandler(getActivity());
        try {
            PlaylistData playlistData = new PlaylistData(playlistName, songDataList);
            db.addPlaylistData(playlistData);
            System.out.println("Successfully updated playlist database.");
        } catch (Exception e) {
            System.out.println("Playlist database update failure.");
        }

        ((PlaylistActivity) getActivity()).setPlaylistView();
    }
}
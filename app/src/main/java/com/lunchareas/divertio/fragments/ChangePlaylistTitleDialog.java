package com.lunchareas.divertio.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.lunchareas.divertio.R;
import com.lunchareas.divertio.activities.BaseActivity;
import com.lunchareas.divertio.models.PlaylistDBHandler;
import com.lunchareas.divertio.models.PlaylistData;
import com.lunchareas.divertio.utils.PlaylistUtil;

public class ChangePlaylistTitleDialog extends DialogFragment {

    private static final String TAG = ChangePlaylistTitleDialog.class.getName();

    public static final String MUSIC_POS = "music_pos";

    private View changeTitleView;
    private EditText newTitleInput;
    private String inputText;
    private String name;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get correct name
        name = (String) getArguments().get(MUSIC_POS);

        AlertDialog.Builder titleChangeDialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        changeTitleView = inflater.inflate(R.layout.dialog_change_playlist_title, null);
        titleChangeDialogBuilder
                .setView(changeTitleView)
                .setPositiveButton(R.string.dialog_change, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Get user input
                        newTitleInput = (EditText) changeTitleView.findViewById(R.id.change_playlist_title_hint);
                        inputText = newTitleInput.getText().toString();

                        // Change the playlist name
                        PlaylistData playlistData = new PlaylistDBHandler(getActivity()).getPlaylistData(name);
                        PlaylistUtil playlistUtil = new PlaylistUtil(getActivity());
                        playlistUtil.changePlaylistName(playlistData, inputText);

                        // Re-update the view
                        ((BaseActivity) getActivity()).setMainView();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });

        return titleChangeDialogBuilder.create();
    }
}

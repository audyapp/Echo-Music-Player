package com.lunchareas.divertio.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.lunchareas.divertio.fragments.AddSongsToPlaylistDialog;
import com.lunchareas.divertio.fragments.ChangePlaylistTitleDialog;
import com.lunchareas.divertio.fragments.CreatePlaylistDialog;
import com.lunchareas.divertio.fragments.DeletePlaylistDialog;
import com.lunchareas.divertio.adapters.PlaylistAdapter;
import com.lunchareas.divertio.fragments.DeleteSongsFromPlaylistDialog;
import com.lunchareas.divertio.models.PlaylistData;
import com.lunchareas.divertio.R;
import com.lunchareas.divertio.utils.PlaylistUtil;

import java.util.List;

public class PlaylistMenuActivity extends BaseListActivity {

    private static final String TAG = PlaylistMenuActivity.class.getName();

    public static final String PLAYLIST_NAME = "playlist_name";

    private ListView playlistView;

    public PlaylistMenuActivity() {
        super(R.layout.activity_playlist_menu);
    }

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initList() {

        // Create playlist
        PlaylistMenuActivity.this.setTitle("Playlists");
        playlistView = (ListView) findViewById(R.id.playlist_list);
        setMainView();

        // Setup single click listener
        playlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent i = new Intent(view.getContext(), PlaylistActivity.class);
                i.putExtra(PLAYLIST_NAME, playlistInfoList.get(position).getPlaylistName());
                startActivity(i);
                finish();
            }
        });

        // Simple long click listener
        playlistView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showChoiceMenu(view, position);
                return true;
            }
        });
    }

    @SuppressLint("NewApi")
    public void showChoiceMenu(View view, final int pos) {
        final PopupMenu popupMenu = new PopupMenu(context, view, Gravity.END);
        final PlaylistData playlistData = playlistInfoList.get(pos);

        // Handle individual clicks
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.playlist_rename_title: {

                        // Create popup for new playlist title
                        DialogFragment changePlaylistTitleDialog = new ChangePlaylistTitleDialog();
                        Bundle bundle = new Bundle();
                        bundle.putString(ChangePlaylistTitleDialog.MUSIC_POS, playlistData.getPlaylistName());
                        changePlaylistTitleDialog.setArguments(bundle);
                        changePlaylistTitleDialog.show(getSupportFragmentManager(), "ChangePlaylistTitle");

                        return true;
                    }
                    case R.id.playlist_remove_title: {

                        // Create popup for remove playlist title
                        playlistUtil = new PlaylistUtil(context);
                        playlistUtil.deletePlaylist(playlistData);
                        setMainView();

                        return true;
                    }
                    case R.id.playlist_add_music_title: {

                        // Create popup for music to add
                        DialogFragment addSongsDialog = new AddSongsToPlaylistDialog();
                        Bundle bundle = new Bundle();
                        bundle.putString(AddSongsToPlaylistDialog.MUSIC_POS, playlistData.getPlaylistName());
                        addSongsDialog.setArguments(bundle);
                        addSongsDialog.show(getSupportFragmentManager(), "AddSongsToPlaylist:");

                        return true;
                    }
                    case R.id.playlist_delete_music_title: {

                        // Create popup for music to delete
                        DialogFragment removeSongsDialog = new DeleteSongsFromPlaylistDialog();
                        Bundle bundle = new Bundle();
                        bundle.putString(DeleteSongsFromPlaylistDialog.MUSIC_POS, playlistData.getPlaylistName());
                        removeSongsDialog.setArguments(bundle);
                        removeSongsDialog.show(getSupportFragmentManager(), "RemoveSongsFromPlaylist");

                        return true;
                    }
                    case R.id.playlist_play_next: {

                        // Create queue controller to run
                        sendListCreateIntent(playlistData.getSongList());
                        songCtrlButton.setBackgroundResource(R.drawable.ic_pause);
                    }
                    default: {
                        return false;
                    }
                }
            }
        });

        // Show popup menu
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.playlist_menu_choice_menu, popupMenu.getMenu());
        popupMenu.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.playlist_menu_overflow_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.playlist_menu_create: {
                DialogFragment createPlaylistDialog = new CreatePlaylistDialog();
                createPlaylistDialog.show(getSupportFragmentManager(), "Upload");
                return true;
            }
            case R.id.playlist_menu_delete: {
                DialogFragment deletePlaylistDialog = new DeletePlaylistDialog();
                deletePlaylistDialog.show(getSupportFragmentManager(), "Delete");
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setMainView() {
        updatePlaylistInfoList();
        PlaylistAdapter playlistAdapter = new PlaylistAdapter(this, playlistInfoList);
        playlistView.setAdapter(playlistAdapter);

        // Set correct background color
        if (playlistInfoList.size() % 2 - 1 == 0) {
            findViewById(R.id.activity).setBackgroundResource(R.color.gray_2);
        } else {
            findViewById(R.id.activity).setBackgroundResource(R.color.gray_3);
        }
    }

    public List<PlaylistData> getPlaylistInfoList() {
        return this.playlistInfoList;
    }
}

package com.lunchareas.divertio;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class UploadSongDialog extends DialogFragment {

    private View uploadDialogView;
    private EditText songNameInput;
    private EditText userLinkInput;
    private boolean internetConnectionStatus;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        AlertDialog.Builder uploadBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        uploadDialogView = inflater.inflate(R.layout.upload_song_dialog, null);
        uploadBuilder
            .setView(uploadDialogView)
            .setPositiveButton(R.string.upload_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    // check internet connection status
                    Thread checkConnectionThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            internetConnectionStatus = hasActiveInternetConnection();
                        }
                    });
                    checkConnectionThread.start();
                    try {
                        checkConnectionThread.join();
                    } catch (Exception e) {
                        System.out.println("Couldn't wait for connection thread.");
                    }

                    // run the download procedure
                    executeDialogDownload();
                }
            })
            .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    System.out.println("Canceled MP3 upload.");
                }
            });

        return uploadBuilder.create();
    }

    private boolean isNetworkAvailable(Context c) {
        ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public boolean hasActiveInternetConnection() {
        System.out.println("Reached internet connection checker...");
        if (isNetworkAvailable(getActivity())) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.something.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(3000);
                urlc.connect();
                System.out.println("Network is available, checking for response code...");
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                System.out.println("No connection...");
            }
        } else {
            System.out.println("No network available...");
        }
        System.out.println("Apparently a network is available...");
        return false;
    }

    private void replaceDialogWithFailure() {
        ((MainActivity)getActivity()).replaceDialogWithFailure(this);
    }

    private void executeDialogDownload() {

        // only go ahead if there is internet
        if (internetConnectionStatus) {

            // get song name and link
            songNameInput = (EditText) uploadDialogView.findViewById(R.id.dialog_upload_name);
            userLinkInput = (EditText) uploadDialogView.findViewById(R.id.dialog_upload_link);
            String songName = songNameInput.getText().toString().trim();
            String userLink = userLinkInput.getText().toString().trim();
            String downloadMusicLink = "";
            String songFileName = songName + ".mp3";
            System.out.println("Inserted link is " + userLink + ".");

            // using advanced api to get link line
            try {
                String downloadInfoLink = "https://www.youtubeinmp3.com/download/?video=" + userLink;
                System.out.println("The link is: " + downloadInfoLink);

                // use jsoup to find the download link
                Document doc = Jsoup.connect(downloadInfoLink)
                        .header("Accept-Encoding", "gzip, deflate")
                        .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                        .maxBodySize(0)
                        .timeout(6000)
                        .get();
                if (doc == null) {
                    System.out.println("The doc is empty.");
                } else {
                    System.out.println("The doc is not empty.");
                }
                Element musicLinkElement = doc.getElementById("download");
                downloadMusicLink = "https://youtubeinmp3.com" + musicLinkElement.attr("href");
                System.out.println("Final Download Link: " + downloadMusicLink);

            } catch (Exception e) {
                e.printStackTrace();
            }

            // replace with error dialog if this fails
            DownloadManager.Request youtubeConvertRequest;
            try {
                // insert link into api and setup download
                youtubeConvertRequest = new DownloadManager.Request(Uri.parse(downloadMusicLink));
                youtubeConvertRequest.setDescription("Converting and downloading...");
                youtubeConvertRequest.setTitle(songFileName + " Download");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    youtubeConvertRequest.allowScanningByMediaScanner();
                    youtubeConvertRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                }
            } catch (Exception e) {
                replaceDialogWithFailure();
                return;
            }

            // download into music files directory
            youtubeConvertRequest.setDestinationInExternalPublicDir("/Divertio", songFileName);
            DownloadManager youtubeConvertManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
            youtubeConvertManager.enqueue(youtubeConvertRequest);

            // update database
            String musicFilePath = Environment.getExternalStorageDirectory().getPath() + "/Divertio/" + songFileName;
            SongDBHandler db = new SongDBHandler(getActivity());
            try {
                SongData songData = new SongData(songName, musicFilePath);
                db.addSongData(songData);
                System.out.println("Successfully updated song database.");
            } catch (Exception e) {
                System.out.println("Song database update failure.");
            }

            // reset the song list view
            ((MainActivity) getActivity()).setSongListView();

        } else {
            System.out.println("Could not connect to website?");
            replaceDialogWithFailure();
        }
    }
}
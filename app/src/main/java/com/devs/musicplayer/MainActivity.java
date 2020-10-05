package com.devs.musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    runtimePermission();
  }

  // taking runtime permission for external storage
  public void runtimePermission() {
    Dexter.withContext(this)
        .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        .withListener(
            new PermissionListener() {
              @Override
              public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                display();
              }

              @Override
              public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {}

              @Override
              public void onPermissionRationaleShouldBeShown(
                  PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
              }
            })
        .check();
  }

  // searching and adding files to the array
  public ArrayList<File> findSong(File file) {
    ArrayList<File> arrayList = new ArrayList<>();

    File[] files = file.listFiles();
    for (File singleFile : files) {
      if (singleFile.isDirectory() && !singleFile.isHidden()) {
        arrayList.addAll(findSong(singleFile));
      } else if (singleFile.getName().endsWith(".mp3")
          || singleFile.getName().endsWith(".wav") && !singleFile.isHidden()) {
        arrayList.add(singleFile);
      }
    }
    return arrayList;
  }

  void display() {
    RecyclerView recyclerView = findViewById(R.id.music_rv);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    final ArrayList<File> songs = findSong(Environment.getExternalStorageDirectory());
    final String[] songNames = new String[songs.size()];

    for (int i = 0; i < songs.size(); i++) {
      songNames[i] = songs.get(i).getName().toString().replace(".mp3", "").replace("wav", "");
    }

    SongsAdapter adapter = new SongsAdapter(songNames, this);
    recyclerView.setAdapter(adapter);

    adapter.setOnSongClickListener(
        new SongsAdapter.OnSongClickListener() {
          @Override
          public void onSongClick(int position) {
            ArrayList<File> songsToSend = new ArrayList<>();
            songsToSend.add(songs.get(position));
            startActivity(
                new Intent(getApplicationContext(), PlayerActivity.class)
                    .putExtra("song", songsToSend)
                    .putExtra("songName", songNames[position]));
          }
        });
  }
}

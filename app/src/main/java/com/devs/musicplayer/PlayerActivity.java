package com.devs.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
  TextView play;
  Button previous, next;
  SeekBar seekBar;
  TextView playerSongName;
  String sName;
  static MediaPlayer myMediaPlayer;
  int position;
  ArrayList<File> mySongs;
  Thread updateSeekBar;
  TextView curTime, totTime;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_player);

    play = findViewById(R.id.play);
    previous = findViewById(R.id.previous);
    next = findViewById(R.id.next);
    playerSongName = findViewById(R.id.song_name);
    seekBar = findViewById(R.id.seekBar);
    curTime = findViewById(R.id.curTime);
    totTime = findViewById(R.id.totalTime);

    getSupportActionBar().setTitle("Now Playing");
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setDisplayShowHomeEnabled(true);

    /*   updateSeekBar= new Thread() {
          @Override
          public void run() {

              int totalDuration= myMediaPlayer.getDuration();
              int currentPosition=0;

              while (currentPosition<totalDuration) {
                  try {
                      sleep(500);
                      currentPosition= myMediaPlayer.getCurrentPosition();
                      seekBar.setProgress(currentPosition);
                   //   curTime.setText(getTimeString(position));

                  }
                  catch (InterruptedException e) {
                      e.printStackTrace();
                  }
              }
          }
      };

    */

    if (myMediaPlayer != null) {
      myMediaPlayer.stop();
      myMediaPlayer.release();
    }

    // getting from intent
    Intent i = getIntent();
    Bundle bundle = i.getExtras();

    mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
    sName = mySongs.get(position).getName().toString();

    String songName = i.getStringExtra("songname");

    playerSongName.setText(songName);
    playerSongName.setSelected(true);

    position = bundle.getInt("pos", 0);

    Uri uri = Uri.parse(mySongs.get(position).toString());

    myMediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
    myMediaPlayer.start();
    seekBar.setMax(myMediaPlayer.getDuration());
    // updateSeekBar.start();
    totTime.setText(createTimeLabel(myMediaPlayer.getDuration()));
    // curTime.setText(getTimeString(position));

    // myMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    myMediaPlayer.setOnCompletionListener(
        new MediaPlayer.OnCompletionListener() {
          @Override
          public void onCompletion(MediaPlayer mediaPlayer) {
            try {
              position = ((position + 1) % mySongs.size());
              Uri uri = Uri.parse(mySongs.get(position).toString());
              myMediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
              sName = mySongs.get(position).getName().toString();
              playerSongName.setText(sName);
              myMediaPlayer.start();
              totTime.setText(createTimeLabel(myMediaPlayer.getDuration()));
              // curTime.setText(getTimeString(position));
              seekBar.setMax(myMediaPlayer.getDuration());
              updateSeekBar.start();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });

    seekBar.setOnSeekBarChangeListener(
        new SeekBar.OnSeekBarChangeListener() {
          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {}

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {

            myMediaPlayer.seekTo(seekBar.getProgress());
          }
        });

    /// new handler for seekbar and time update
    new Thread(
            new Runnable() {
              @Override
              public void run() {
                while (myMediaPlayer != null) {
                  try {
                    //                        Log.i("Thread ", "Thread Called");
                    // create new message to send to handler
                    if (myMediaPlayer.isPlaying()) {
                      Message msg = new Message();
                      msg.what = myMediaPlayer.getCurrentPosition();
                      handler.sendMessage(msg);
                      Thread.sleep(1000);
                    }
                  } catch (InterruptedException e) {
                    e.printStackTrace();
                  }
                }
              }
            })
        .start();

    play.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            seekBar.setMax(myMediaPlayer.getDuration());

            if (myMediaPlayer.isPlaying()) {
              play.setBackgroundResource(R.drawable.play);
              myMediaPlayer.pause();
            } else {

              play.setBackgroundResource(R.drawable.pause);
              myMediaPlayer.start();
            }
          }
        });

    next.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            myMediaPlayer.stop();
            myMediaPlayer.release();
            position = ((position + 1) % mySongs.size());
            Uri uri = Uri.parse(mySongs.get(position).toString());
            myMediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            // seekBar.setMax(myMediaPlayer.getDuration());
            sName = mySongs.get(position).getName().toString();
            playerSongName.setText(sName);
            totTime.setText(createTimeLabel(myMediaPlayer.getDuration()));
            myMediaPlayer.start();
          }
        });

    previous.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            myMediaPlayer.stop();
            myMediaPlayer.release();
            position = ((position - 1) < 0) ? (mySongs.size() - 1) : (position - 1);
            Uri uri = Uri.parse(mySongs.get(position).toString());
            myMediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            sName = mySongs.get(position).getName().toString();
            playerSongName.setText(sName);
            totTime.setText(createTimeLabel(myMediaPlayer.getDuration()));
            myMediaPlayer.start();
          }
        });
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
    }
    return super.onOptionsItemSelected(item);
  }

  public String createTimeLabel(int duration) {
    String timeLabel = "";
    int min = duration / 1000 / 60;
    int sec = duration / 1000 % 60;

    timeLabel += min + ":";
    if (sec < 10) timeLabel += "0";
    timeLabel += sec;

    return timeLabel;
  }

  @SuppressLint("HandlerLeak")
  private Handler handler =
      new Handler() {
        @Override
        public void handleMessage(Message msg) {
          //            Log.i("handler ", "handler called");
          int current_position = msg.what;
          seekBar.setProgress(current_position);
          String cTime = createTimeLabel(current_position);
          curTime.setText(cTime);
        }
      };
}

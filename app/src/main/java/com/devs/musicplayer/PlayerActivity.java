package com.devs.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
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

public class PlayerActivity extends AppCompatActivity implements Playable {
    int position;
    TextView play;
    Button previous, next;
    SeekBar seekBar;
    TextView playerSongName;
    String songName;
    static MediaPlayer myMediaPlayer;
    File song;
    ArrayList<File> songs;
    Thread updateSeekBar;
    TextView curTime, totTime;
    NotificationManager notificationManager;
    boolean isPlaying = false;


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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel();
            registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
            startService(new Intent(getBaseContext(),OnClearFromRecentService.class));
        }

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

        songs = (ArrayList) bundle.getParcelableArrayList("songs");
        position = bundle.getInt("pos", 0);
        song = songs.get(position);
        songName = i.getStringExtra("songName");

        playerSongName.setText(songName);
        playerSongName.setSelected(true);

        Uri uri = Uri.parse(song.toString());

        myMediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        CreateNotification.createNotification(PlayerActivity.this,songName,R.drawable.ic_baseline_pause,position,songs.size()-1);
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
                            position = ((position + 1) % songs.size());
                            Uri uri = Uri.parse(songs.get(position).toString());
                            myMediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                            songName = songs.get(position).getName();
                            playerSongName.setText(songName);
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
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

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
                             catch (IllegalStateException e){
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
                        position = ((position + 1) % songs.size());
                        Uri uri = Uri.parse(songs.get(position).toString());
                        myMediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                        seekBar.setMax(myMediaPlayer.getDuration());
                        songName = songs.get(position).getName();
                        playerSongName.setText(songName);
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
                        position = ((position - 1) < 0) ? (songs.size() - 1) : (position - 1);
                        Uri uri = Uri.parse(songs.get(position).toString());
                        myMediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                        seekBar.setMax(myMediaPlayer.getDuration());
                        songName = songs.get(position).getName();
                        playerSongName.setText(songName);
                        totTime.setText(createTimeLabel(myMediaPlayer.getDuration()));
                        myMediaPlayer.start();

                    }
                });
    }
    private void createChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID,"Music", NotificationManager.IMPORTANCE_LOW);

            notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null){
                notificationManager.createNotificationChannel(channel);
            }
        }
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
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");

            switch (action){
                case CreateNotification.ACTION_PREVIOUS:
                    onTrackPrevious();
                    break;
                case CreateNotification.ACTION_NEXT:
                    onTrackNext();
                    break;
                case CreateNotification.ACTION_PLAY:
                    if (isPlaying){
                        onTrackPause();
                    }else{
                        onTrackPlay();
                    }
                    break;
            }
        }
    };
    @Override
    public void onTrackPrevious() {
        position--;
        CreateNotification.createNotification(PlayerActivity.this,songName,R.drawable.ic_baseline_pause,position,songs.size()-1);
        songs.get(position).getName();
    }

    @Override
    public void onTrackPause() {
        CreateNotification.createNotification(PlayerActivity.this,songName,R.drawable.ic_baseline_play_arrow,position,songs.size()-1);
        play.setBackgroundResource(R.drawable.ic_baseline_play_arrow);
        songs.get(position).getName();
        isPlaying = false;
    }

    @Override
    public void onTrackPlay() {
        CreateNotification.createNotification(PlayerActivity.this,songName,R.drawable.ic_baseline_pause,position,songs.size()-1);
        play.setBackgroundResource(R.drawable.ic_baseline_pause);
        songs.get(position).getName();
        isPlaying = true;
    }

    @Override
    public void onTrackNext() {
        position++;
        CreateNotification.createNotification(PlayerActivity.this,songName,R.drawable.ic_baseline_pause,position,songs.size()-1);
        songs.get(position).getName();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationManager.cancelAll();
        }
        unregisterReceiver(broadcastReceiver);
    }
}

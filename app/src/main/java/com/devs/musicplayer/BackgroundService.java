package com.devs.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class BackgroundService extends Service {
  private static final int NOTIF_ID = 1;
  private static final String NOTIFI_CHANNEL_ID = "Devs_Player";

  public BackgroundService() {}

  @Override
  public IBinder onBind(Intent intent) {
    // TODO: Return the communication channel to the service.
    return null;
  }
}

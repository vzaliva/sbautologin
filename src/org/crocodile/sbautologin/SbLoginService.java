package org.crocodile.sbautologin;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SbLoginService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void onStart(Intent intent, int startId) {
    }

}
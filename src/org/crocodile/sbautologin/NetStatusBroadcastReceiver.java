package org.crocodile.sbautologin;

import android.content.*;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class NetStatusBroadcastReceiver extends BroadcastReceiver
{
    private static final String TAG = "SbAutoLoginBroadcastReceiver";
    private static final String STARBUCKS_SSID = "attwifi";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(TAG,"Broadcast received");
        NetworkInfo ni = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if(!ni.isConnected())
        {
            Log.d(TAG,"Not connected");
            return;
        }
        
        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo winfo = wifi.getConnectionInfo();
        String ssid = winfo.getSSID();
        Log.d(TAG,"SSID="+ssid);
        
        /*
        if(!intent.hasExtra(WifiManager.EXTRA_BSSID))
            return;
        String bssid = (String) intent.getStringExtra(WifiManager.EXTRA_BSSID);
        */
        
        if(STARBUCKS_SSID.equals(ssid))
        {
            Starbucks s = new Starbucks();
            try
            {
                s.login();
            } catch(Exception e)
            {
                Log.e(TAG,"Login failed",e);
            }
        }
    }

}

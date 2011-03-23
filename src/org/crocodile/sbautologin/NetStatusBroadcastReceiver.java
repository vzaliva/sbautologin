package org.crocodile.sbautologin;

import java.util.Date;

import android.content.*;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.crocodile.sbautologin.db.*;
import org.crocodile.sbautologin.model.HistoryItem;

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

        
        if(STARBUCKS_SSID.equals(ssid))
        {
            Starbucks s = new Starbucks();
            HistoryItem h = new HistoryItem();
            h.setDate(new Date());
            try
            {
                boolean status = s.login();
                h.setSuccess(true);
                if(status)
                    h.setMessage("Logged in");
                else
                    h.setMessage("Already logged in");
            } catch(Exception e)
            {
                Log.e(TAG,"Login failed",e);
                h.setSuccess(false);
                h.setMessage("Login failed: "+e.getMessage());
            }
            DBAccesser db = new DBAccesser(context);
            db.addHistoryItem(h);
        }
    }

}

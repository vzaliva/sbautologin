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
    
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(TAG,"Broadcast received");

        SharedPreferences settings = context.getSharedPreferences(Constants.PREFS_NAME, 0);
        if(!settings.getBoolean(Constants.PREF_KEY_ACTIVE, true))
        {
            Log.i(TAG,"Disabled. Ignoring broadcast.");
            return;
        }

        final String action = intent.getAction();
        if(!action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
        {
            Log.i(TAG,"Ignoring action "+action);
            return;
        }
        
        NetworkInfo ni = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if(!ni.isConnected())
        {
            Log.d(TAG,"Not connected");
            return;
        }
        
        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo winfo = wifi.getConnectionInfo();
        String ssid = winfo.getSSID();
        
        if(Constants.STARBUCKS_SSID.equals(ssid))
        {
            Log.d(TAG,"Starbucks SSDID detected. SSID="+ssid);
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
        } else
        {
            Log.d(TAG, "Unknown SSID "+ssid);
        }
    }

}

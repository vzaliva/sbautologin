
package org.crocodile.sbautologin;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import org.crocodile.sbautologin.db.DBAccesser;
import org.crocodile.sbautologin.model.HistoryItem;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

public class MainActivity extends Activity
{
    private static final String TAG = "SbAutoLogin";
    private boolean update = true;
    private Object monitor = new Object();

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch(item.getItemId())
        {
        case R.id.clear_hist_menu_item:
            clearHistory();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void clearHistory()
    {
        Log.d(TAG,"Clearing history");
        DBAccesser db = new DBAccesser(MainActivity.this);
        db.removeHistoryItems();
        synchronized(monitor)
        {
            monitor.notify();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        ToggleButton activeToggle = (ToggleButton) findViewById(R.id.active);
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        activeToggle.setChecked(settings.getBoolean(Constants.PREF_KEY_ACTIVE, true));
        activeToggle.setOnClickListener(new OnClickListener() {
            public void onClick(View buttonView)
            {
                SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(Constants.PREF_KEY_ACTIVE, ((ToggleButton) buttonView).isChecked());
                editor.commit();
            }
        });
        
        //addTestData();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        update = true;
        startUpdateThread();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        synchronized(monitor)
        {
            update = false;
            monitor.notify();
        }
    }

    public void startUpdateThread()
    {
        new Thread(new Runnable() {
            public void run()
            {
                DBAccesser db = new DBAccesser(MainActivity.this);
                handler.sendEmptyMessage(0);
                long maxId = db.getMaxId();
                while(true)
                {
                    int newMaxId = db.getMaxId();
                    if(maxId < newMaxId || newMaxId==0)
                    {
                        handler.sendEmptyMessage(0);
                        maxId = newMaxId;
                    }

                    try
                    {
                        synchronized(monitor)
                        {
                            monitor.wait(Constants.REFRESH_INTERVAL_MS);
                            if(!update)
                                break;
                        }
                    } catch(InterruptedException e)
                    {
                    }
                }
            }
        }).start();
    }

    public Handler handler = new Handler() {
                               @Override
                               public void handleMessage(Message msg)
                               {
                                   showHistory();
                               }
                           };

    private void showHistory()
    {
        DBAccesser db = new DBAccesser(this);
        ArrayList<HistoryItem> hist = db.getHistoryItems(Constants.HIST_LEN);

        TableLayout histtable = (TableLayout) findViewById(R.id.histTable);
        histtable.setStretchAllColumns(true);
        histtable.setShrinkAllColumns(true);
        histtable.removeAllViews();
        if(hist.isEmpty())
        {
            TextView nohist = new TextView(this);
            nohist.setText(R.string.nohist);
            nohist.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);
            histtable.addView(nohist);
        } else
        {

            int i = 0;
            for(HistoryItem h : hist)
            {
                i++;
                TableRow row = new TableRow(this);
                row.setGravity(Gravity.CENTER_HORIZONTAL);

                ImageView icon = new ImageView(this);
                icon.setImageResource(h.isSuccess() ? android.R.drawable.presence_online
                        : android.R.drawable.presence_busy);
                row.addView(icon);

                TextView dateCell = new TextView(this);
                CharSequence ds = DateUtils.formatSameDayTime(h.getDate().getTime(), new Date().getTime(),
                        DateFormat.SHORT, DateFormat.SHORT);
                dateCell.setText(ds);
                dateCell.setGravity(Gravity.LEFT);
                row.addView(dateCell);

                TextView msgCell = new TextView(this);
                msgCell.setText(h.getMessage());
                msgCell.setGravity(Gravity.LEFT);
                row.addView(msgCell);

                row.setGravity(Gravity.LEFT);
                histtable.addView(row);
            }
        }
    }

    @SuppressWarnings("unused")
    private void addTestData()
    {
        HistoryItem h = new HistoryItem();

        DBAccesser db = new DBAccesser(this);
        for(int i = 0; i < 2; i++)
        {
            h.setDate(new Date());
            h.setSuccess(i % 2 == 0 ? true : false);
            h.setMessage("Attempt " + i);
            db.addHistoryItem(h);
        }
    }

}
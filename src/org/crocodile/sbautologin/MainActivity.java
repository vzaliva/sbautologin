
package org.crocodile.sbautologin;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.crocodile.sbautologin.db.DBAccesser;
import org.crocodile.sbautologin.model.HistoryItem;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity
{

    private static final String TAG = "SbAutoLoginMain";
    static final String PREFS_NAME = "sbautologin";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        CheckBox activeChkBox = (CheckBox) findViewById(R.id.active);

        activeChkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("active", isChecked);
                editor.commit();
            }
        });

        addTestData();
        showHistory();
    }

    private void showHistory()
    {
        DBAccesser db = new DBAccesser(this);
        ArrayList<HistoryItem> hist = db.getHistoryItems(4);
        int i=0;
        TableLayout histtable = (TableLayout) findViewById(R.id.histTable);
        histtable.setStretchAllColumns(true);  
        histtable.setShrinkAllColumns(true);  
        for(HistoryItem h:hist)
        {
            i++;
            Log.d(TAG,"Read: "+h.getMessage());
            TableRow row = new TableRow(this); 
            row.setGravity(Gravity.CENTER_HORIZONTAL);  

            TextView dateCell = new TextView(this);            
            CharSequence ds = DateUtils.formatSameDayTime(h.getDate().getTime(), 
                    new Date().getTime(), 
                    DateFormat.SHORT, 
                    DateFormat.SHORT);
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

    private void addTestData()
    {
        HistoryItem h = new HistoryItem();

        DBAccesser db = new DBAccesser(this);
        for(int i = 0; i < 10; i++)
        {
            h.setDate(new Date());
            h.setSuccess(i % 2 == 0 ? true : false);
            h.setMessage("Attempt " + i);
            Log.d(TAG,"Write: "+h.getMessage());
            db.addHistoryItem(h);
        }
    }

}
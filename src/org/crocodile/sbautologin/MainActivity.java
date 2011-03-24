
package org.crocodile.sbautologin;

import java.util.ArrayList;
import java.util.Date;

import org.crocodile.sbautologin.db.DBAccesser;
import org.crocodile.sbautologin.model.HistoryItem;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;

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
        DBAccesser db = new DBAccesser(getBaseContext());
        ArrayList<HistoryItem> hist = db.getHistoryItems(4);
        for(HistoryItem h:hist)
        {
            Log.d(TAG,"Read: "+h.getMessage());
        }
    }

    private void addTestData()
    {
        HistoryItem h = new HistoryItem();

        DBAccesser db = new DBAccesser(getBaseContext());
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
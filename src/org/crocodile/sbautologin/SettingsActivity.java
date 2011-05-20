package org.crocodile.sbautologin;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    private SharedPreferences prefs;
    private CheckBox successChbx, errorChbx, loggedinChbx;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings);
        prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);

        successChbx = (CheckBox) findViewById(R.id.prefs_checkbox_success);
        errorChbx = (CheckBox) findViewById(R.id.prefs_checkbox_error);
        loggedinChbx = (CheckBox) findViewById(R.id.prefs_checkbox_already_logged);

        Button save = (Button) findViewById(R.id.prefs_save);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(Constants.PREF_KEY_NOTIFY_WHEN_SUCCESS, successChbx.isChecked());
                editor.putBoolean(Constants.PREF_KEY_NOTIFY_WHEN_ERROR, errorChbx.isChecked());
                editor.putBoolean(Constants.PREF_KEY_NOTIFY_WHEN_ALREADY_LOGGED_IN, loggedinChbx.isChecked());
                editor.commit();
                Toast.makeText(getApplicationContext(), R.string.conf_save, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

        @Override
    public void onResume() {
        super.onResume();

        successChbx.setChecked(prefs.getBoolean(Constants.PREF_KEY_NOTIFY_WHEN_SUCCESS, true));
        errorChbx.setChecked(prefs.getBoolean(Constants.PREF_KEY_NOTIFY_WHEN_ERROR, true));
        loggedinChbx.setChecked(prefs.getBoolean(Constants.PREF_KEY_NOTIFY_WHEN_ALREADY_LOGGED_IN, false));
    }

}

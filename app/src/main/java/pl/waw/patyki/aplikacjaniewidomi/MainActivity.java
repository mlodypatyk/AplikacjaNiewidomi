package pl.waw.patyki.aplikacjaniewidomi;


import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {

    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        boolean isFirstRun = preferences.getBoolean("FIRSTRUN", true);
        boolean switch_state = preferences.getBoolean("NOTIFICATIONS", true);
        if (isFirstRun){
            editor.putBoolean("FIRSTRUN", false);
            editor.putBoolean("NOTIFICATIONS", true);
            editor.apply();
            PeriodicWorkRequest notifications_in_background =
                    new PeriodicWorkRequest.Builder(NotificationChecker.class, 1, TimeUnit.MINUTES)
                        .build();
            WorkManager.getInstance(this).enqueue(notifications_in_background);
            Log.d("info", "zrobilem startup");
        }
        Switch switch1 = (Switch) this.findViewById(R.id.switch1);
        switch1.setChecked(switch_state);

    }
    public void switch_click(View view){
        Switch switch1 = (Switch) view.findViewById(R.id.switch1);
        editor.putBoolean("NOTIFICATIONS", switch1.isChecked());
        editor.apply();
    }
    public void button_click(View view){
        WorkRequest test = new OneTimeWorkRequest.Builder(NotificationChecker.class).build();
        WorkManager
                .getInstance(this.getApplicationContext())
                .enqueue(test);
    }
    public void reset_button_click(View view){
        editor.putBoolean("FIRSTRUN", true);
        editor.apply();
        this.recreate();
    }
}
package pl.waw.patyki.aplikacjaniewidomi;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NotificationChecker extends Worker {

    Context context_here;

    public NotificationChecker(
            @NonNull Context context,
            @NonNull WorkerParameters params){
        super(context, params);
        context_here = context;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context_here.getString(R.string.channel_name);
            String description = context_here.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("nie_wiem", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context_here.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    @NonNull
    @Override
    public Result doWork() {
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo;
        wifiInfo = wifiManager.getConnectionInfo();
        String ssid = "null";
        if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
            ssid = wifiInfo.getSSID();
        }
        Log.d("beka", ssid);
        String connected = "false";
        if(ssid.equals("\"none\"")){ // your wifi ssid
            connected = "true";
        }
        try {
            URL url = new URL("http://your.server.here/notification"); // your server url here
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Wifi-Connected", connected);
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK){
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                Log.d("wynik", response.toString());
                SharedPreferences preferencje = PreferenceManager.getDefaultSharedPreferences(context_here);
                if (response.toString().equals("True") && preferencje.getBoolean("NOTIFICATIONS", true)){
                    createNotificationChannel();
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context_here, "nie_wiem")
                            .setSmallIcon(R.drawable.ic_launcher_background)
                            .setContentTitle("Światło się pali")
                            .setContentText("Wykryto światło po zachodzie słońca.")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context_here);
                    notificationManager.notify(1, builder.build());
                }
            }
        }
        catch(Exception e){
            Log.d("exceptions", e.toString());
        }


        return Result.success();
    }
}


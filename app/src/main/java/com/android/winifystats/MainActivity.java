package com.android.winifystats;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.winifystats.model.NotificationWorkTime;
import com.android.winifystats.model.StatisticsDTO;
import com.android.winifystats.model.TokenDTO;
import com.android.winifystats.model.WifiBroadcastReceiver;
import com.android.winifystats.model.WinifyEmployee;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import az.plainpie.PieView;
import az.plainpie.animation.PieAngleAnimation;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.R.attr.animation;


public class MainActivity extends AppCompatActivity {


    private WinifyEmployee service;
    private Button startTimerButton;
    private TokenDTO token;
    private PieView pieView;

    private final Handler mHandler = new Handler();
    private long currentDailyTime;

    private TextView weeklyWorkingTime;
    private TextView monthlyWorkingTime;
    private int mNotificationId;
    private long mNeededWorkingTime = 28800000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        token = new TokenDTO();
        token.setToken(This.getCache().getToken());

        startTimerButton = (Button) findViewById(R.id.timer_start);

        weeklyWorkingTime = (TextView) findViewById(R.id.weekly);
        monthlyWorkingTime = (TextView) findViewById(R.id.monthly);

        BroadcastReceiver broadcastReceiver = new WifiBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        getApplicationContext().registerReceiver(broadcastReceiver, intentFilter);

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mWifi.isConnected()) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("No Wifi Connection!")
                            .setContentText("Please connect to Winify Wifi")
                            .setAutoCancel(true);
            Intent resultIntent = new Intent(this, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(mNotificationId, mBuilder.build());
        }







        pieView = (PieView) findViewById(R.id.pieView);
        pieView.setPercentageBackgroundColor(getResources().getColor(R.color.green));
        // Change the color fill of the background of the widget, by default is transparent
        pieView.setMainBackgroundColor(getResources().getColor(R.color.colorPrimary));


        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("http://192.168.3.145")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();
        service = retrofit.create(WinifyEmployee.class);


        startTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStopTimer();
            }
        });
        checkTimerStatus();
        getStatistics();


    }

    private void getStatistics() {
        service.getStatistics(token).enqueue(new Callback<StatisticsDTO>() {
            @Override
            public void onResponse(Call<StatisticsDTO> call, Response<StatisticsDTO> response) {
                if (response.isSuccessful()) {
                    long currentWorkingTime = response.body().getDaily();
                    weeklyWorkingTime.setText("Weekly Worked: " + formatTime(response.body().getWeekly()));
                    monthlyWorkingTime.setText("Monthly Worked: " + formatTime(response.body().getMonthly()));
                    updateTimer(currentWorkingTime);
                    if (currentWorkingTime >= mNeededWorkingTime){
                        NotificationWorkTime.sendNotification(MainActivity.this);
                    }


                } else {
                    switch (response.code()) {
                        case 401:
                            logout();
                            break;
                    }
                }

            }

            @Override
            public void onFailure(Call<StatisticsDTO> call, Throwable t) {

            }
        });

    }

    private void updateTimer(long time) {
        currentDailyTime = time;
        displayTimer(currentDailyTime);
        mHandler.removeCallbacks(mUpdateUI);
        if (This.getCache().isTimerStarted()) {
            mHandler.post(mUpdateUI);
        }
    }

    private final Runnable mUpdateUI = new Runnable() {
        public void run() {
            currentDailyTime += 1000;
            displayTimer(currentDailyTime);
            mHandler.postDelayed(mUpdateUI, 1000); // 1 second

        }
    };

    public void displayTimer(long time) {
        pieView.setPercentage(time * 100F / 28800000);
        pieView.setInnerText(formatTime(time));

    }


    public String formatTime(long millis) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));

    }

    private void startStopTimer() {
        startTimerButton.setEnabled(false);
        if (This.getCache().isTimerStarted()) {
            stopTimer();
        } else {
            startTimer();
        }
    }

    private void startTimer() {

        service.startTimer(token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                startTimerButton.setEnabled(true);
                if (response.isSuccessful()) {
                    This.getCache().setTimerStarted(true);
                    checkTimerStatus();

                } else {
                    switch (response.code()) {
                        case 417:
                            This.getCache().setTimerStarted(true);
                            checkTimerStatus();

                            try {
                                Toast.makeText(MainActivity.this, response.errorBody().string(), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {

                                Log.e("MainActivity", e.getMessage());
                            }
                            break;
                        case 401:
                            logout();
                            break;
                    }


                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                startTimerButton.setEnabled(true);
            }
        });

    }

    private void stopTimer() {
        service.stopTimer(token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                startTimerButton.setEnabled(true);
                if (response.isSuccessful()) {
                    This.getCache().setTimerStarted(false);
                    checkTimerStatus();
                } else {
                    switch (response.code()) {
                        case 417:
                            This.getCache().setTimerStarted(false);
                            checkTimerStatus();

                            try {
                                Toast.makeText(MainActivity.this, response.errorBody().string(), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {

                                Log.e("MainActivity", e.getMessage());
                            }
                            break;
                        case 401:
                            logout();
                            break;
                    }


                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                startTimerButton.setEnabled(true);
            }
        });
    }


    private void checkTimerStatus() {
        updateTimer(currentDailyTime);
        startTimerButton.setText(This.getCache().isTimerStarted() ? "Stop Timer" : "Start Timer");
        startTimerButton.setBackgroundResource(This.getCache().isTimerStarted()
                ? R.drawable.round_button_stop : R.drawable.round_button_start);
        pieView.setPercentageBackgroundColor(This.getCache().isTimerStarted()
                ? getResources().getColor(R.color.green) : getResources().getColor(R.color.orange));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mMenuInflater = getMenuInflater();
        mMenuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public void logout() {
        This.getCache().logout();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case R.id.logout_button:
                logout();
                break;

        }
        return true;
    }
}

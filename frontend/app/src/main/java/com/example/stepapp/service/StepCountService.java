package com.example.stepapp.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.util.ULocale;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.stepapp.persistence.DailyStepsLocalDaoService;
import com.example.stepapp.persistence.model.DailySteps;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

public class StepCountService extends Service {

    public static boolean RUNNING;
    public static DailySteps dailySteps;

    public static DailyStepsLocalDaoService localService;

    public Sensor stepDetector;
    public SensorManager manager;
    public SensorEventListener stepCounterListener;

    static{
        RUNNING = false;
        dailySteps = new DailySteps();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //TODO Fetch step count and current day, if available - preferably remotely

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        jdf.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        String date = jdf.format(new Date());
        String day = date.substring(0,10);

        dailySteps = localService.get(getApplicationContext(), day);

        if(dailySteps == null)
            dailySteps = new DailySteps();

        manager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        stepDetector = manager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        stepCounterListener = new StepCounterListener(getApplicationContext());

        manager.registerListener(stepCounterListener, stepDetector, SensorManager.SENSOR_DELAY_NORMAL);

        return START_REDELIVER_INTENT;

    }

    @Override
    public void onCreate() {
        super.onCreate();
        localService = new DailyStepsLocalDaoService(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //TODO Save steps onto DB

        localService.update(getApplicationContext(), dailySteps);

        // Unregister sensor listeners
        manager.unregisterListener(stepCounterListener);

    }

    public static void startStepCountingService(Context context){

        context.startService(new Intent(context, StepCountService.class));
        RUNNING = true;

    }

    public static void stopStepCountingService(Context context){

        context.stopService(new Intent(context, StepCountService.class));
        RUNNING = false;

    }

}

// Sensor event listener
class StepCounterListener implements SensorEventListener {

    public Context context;
    public String timestamp;
    public String day;

    public StepCounterListener(Context context){
        this.context = context;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // case Step detector
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {// Calculate the number of steps

            long timeInMillis = System.currentTimeMillis() + (event.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000;

            // Convert the timestamp to date
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
            jdf.setTimeZone(TimeZone.getTimeZone("GMT+2"));
            String date = jdf.format(timeInMillis);

            // Get the date, the day and the hour
            timestamp = date;
            day = date.substring(0,10);

            countSteps(event.values[0], day);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //
    }

    // Calculate the number of steps from the step detector
    private void countSteps(float step, String day) {

        String old = StepCountService.dailySteps.day;
        StepCountService.dailySteps.setDay(day);

        // If new day, update database
        if(!Objects.equals(day, old)) {

            //TODO update database, new day
            StepCountService.dailySteps.steps = (int) step;
            StepCountService.localService.insert(context, StepCountService.dailySteps);

        }else {

            StepCountService.dailySteps.setSteps(StepCountService.dailySteps.getSteps() + (int) step);
            StepCountService.localService.update(context, StepCountService.dailySteps);

        }

        Log.d("DAILY STEPS UPDATE", "Steps: " + StepCountService.dailySteps);

    }

}
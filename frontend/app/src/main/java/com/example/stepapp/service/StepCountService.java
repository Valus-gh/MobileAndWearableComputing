package com.example.stepapp.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.stepapp.R;

import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.TimeZone;

public class StepCountService extends Service {

    public static boolean RUNNING;
    public static int dailySteps;
    public static String day;

    public Sensor stepDetector;
    public SensorManager manager;
    public SensorEventListener stepCounterListener;

    static{
        RUNNING = false;
        dailySteps = 0;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //TODO Fetch step count and current day, if available


        manager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        stepDetector = manager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        stepCounterListener = new StepCounterListener();

        manager.registerListener(stepCounterListener, stepDetector, SensorManager.SENSOR_DELAY_NORMAL);

        return START_REDELIVER_INTENT;

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //TODO Save steps onto DB

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

    public String timestamp;
    public String day;

    public StepCounterListener(){
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

            Log.d("BRUH", "sensor changed");

            countSteps(event.values[0], day);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //
    }

    // Calculate the number of steps from the step detector
    private void countSteps(float step, String day) {

        //Step count
        StepCountService.dailySteps += (int) step;
        Log.d("DAILY STEPS UPDATE", "Steps: " + StepCountService.dailySteps);

        // If new day, update database
        if(!Objects.equals(day, StepCountService.day)) {
            //TODO update database, new day
        }

        // Update current day
        StepCountService.day = day;

    }

}
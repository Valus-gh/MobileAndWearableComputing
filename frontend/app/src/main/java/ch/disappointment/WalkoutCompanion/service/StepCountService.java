package ch.disappointment.WalkoutCompanion.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
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

import androidx.annotation.Nullable;

import ch.disappointment.WalkoutCompanion.DayUtils;
import ch.disappointment.WalkoutCompanion.MainActivity;
import ch.disappointment.WalkoutCompanion.NotificationChannels;
import ch.disappointment.WalkoutCompanion.NotificationIDs;

import ch.disappointment.WalkoutCompanion.R;
import ch.disappointment.WalkoutCompanion.api.ApiService;
import ch.disappointment.WalkoutCompanion.persistence.DailyStepsDaoService;
import ch.disappointment.WalkoutCompanion.persistence.DailyStepsLocalDaoService;
import ch.disappointment.WalkoutCompanion.persistence.DailyStepsRemoteDaoService;
import ch.disappointment.WalkoutCompanion.persistence.model.DailySteps;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

public class StepCountService extends Service {

    public static boolean RUNNING;
    public static DailySteps dailySteps;

    public static DailyStepsDaoService<DailySteps> dailyStepsService;

    public Sensor stepDetector;
    public SensorManager manager;
    public SensorEventListener stepCounterListener;
    public Notification notification;

    static {
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

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent,
                        PendingIntent.FLAG_IMMUTABLE);

        notification = new Notification.Builder(this, NotificationChannels.DEFAULT_NOTIFICATION_CHANNEL.getValue())
                .setContentTitle(getText(R.string.svc_notification_title))
                .setContentText(getText(R.string.svc_notification_message))
                .setSmallIcon(R.drawable.stepp)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.svc_notification_message))
                .build();

        startForeground(NotificationIDs.PERSISTENT_SERVICE.getValue(), notification);

        dailyStepsService.get(getApplicationContext(), DayUtils.getCurrentDay(), (res) -> {
            if (res == null) {
                dailySteps = new DailySteps();
            } else {
                dailySteps = res;
            }
        });

        manager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        stepDetector = manager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        stepCounterListener = new StepCounterListener(getApplicationContext());

        manager.registerListener(stepCounterListener, stepDetector, SensorManager.SENSOR_DELAY_NORMAL);

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(ApiService.getInstance(getApplicationContext()).isLocal())
            dailyStepsService = new DailyStepsLocalDaoService(getApplicationContext());
        else
            dailyStepsService = new DailyStepsRemoteDaoService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        dailyStepsService.update(getApplicationContext(), dailySteps, () -> {});

        // Unregister sensor listeners
        manager.unregisterListener(stepCounterListener);

    }

    public static void startStepCountingService(Context context) {

        context.startForegroundService(new Intent(context, StepCountService.class));
        RUNNING = true;

    }

    public static void stopStepCountingService(Context context) {

        context.stopService(new Intent(context, StepCountService.class));
        RUNNING = false;

    }

}

// Sensor event listener
class StepCounterListener implements SensorEventListener {

    public Context context;
    public String timestamp;
    public String day;

    public StepCounterListener(Context context) {
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
            day = date.substring(0, 10);

            countSteps(event.values[0], day);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //
    }

    // Calculate the number of steps from the step detector
    private void countSteps(float step, String day) {

        String old = StepCountService.dailySteps.date;
        StepCountService.dailySteps.setDate(day);


        // If new day, update database
        if (!Objects.equals(day, old)) {

            //TODO update database, new day
            StepCountService.dailySteps.steps = (int) step;

            Log.d("DAILY STEPS UPDATE", "Steps: " + StepCountService.dailySteps);
            StepCountService.dailyStepsService.insert(context, StepCountService.dailySteps, () -> {
                Log.d("REMOTE UPDATE", "Steps: " + StepCountService.dailySteps);
            });

        } else {

            StepCountService.dailySteps.setSteps(StepCountService.dailySteps.getSteps() + (int) step);

            Log.d("DAILY STEPS CREATE", "Steps: " + StepCountService.dailySteps);
            StepCountService.dailyStepsService.update(context, StepCountService.dailySteps, () -> {
                Log.d("REMOTE UPDATE", "Steps: " + StepCountService.dailySteps);
            });

        }


    }

}
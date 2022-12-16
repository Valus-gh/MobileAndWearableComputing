package ch.disappointment.WalkoutCompanion;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.Toast;

import ch.disappointment.WalkoutCompanion.api.ApiService;
import ch.disappointment.WalkoutCompanion.persistence.DailyStepsDaoService;
import ch.disappointment.WalkoutCompanion.persistence.DailyStepsRemoteDaoService;
import ch.disappointment.WalkoutCompanion.persistence.TokensDaoService;
import ch.disappointment.WalkoutCompanion.persistence.model.DailySteps;
import ch.disappointment.WalkoutCompanion.service.StepCountService;
import ch.disappointment.WalkoutCompanion.ui.home.HomeFragment;
import ch.disappointment.WalkoutCompanion.ui.report.ReportFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    private ConstraintLayout mainLayout;
    private ConstraintLayout fragmentContainer;
    private BottomNavigationView bottomnav;

    private static final int REQUEST_ACTIVITY_RECOGNITION_PERMISSION = 45;
    private final boolean runningQOrLater =
            android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mainLayout = (ConstraintLayout) findViewById(R.id.main_layout);
        fragmentContainer = (ConstraintLayout) findViewById(R.id.fragment_container);
        bottomnav = (BottomNavigationView) findViewById(R.id.bottom_nav_view);


        bottomnav.setOnNavigationItemSelectedListener(this);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

        // Ask for activity recognition permission
        if (runningQOrLater) {
            getActivity();
        }

        createNotificationChannel();
        populateDB();

    }


    void populateDB(){

        DailyStepsRemoteDaoService service = new DailyStepsRemoteDaoService();

        service.insert(this, new DailySteps(50, "2022-12-01"), () -> {});
        service.insert(this, new DailySteps(23, "2022-12-02"), () -> {});
        service.insert(this, new DailySteps(50, "2022-12-03"), () -> {});
        service.insert(this, new DailySteps(23, "2022-12-04"), () -> {});
        service.insert(this, new DailySteps(54, "2022-12-05"), () -> {});
        service.insert(this, new DailySteps(34, "2022-12-06"), () -> {});
        service.insert(this, new DailySteps(12, "2022-12-07"), () -> {});
        service.insert(this, new DailySteps(34, "2022-12-08"), () -> {});
        service.insert(this, new DailySteps(53, "2022-12-09"), () -> {});
        service.insert(this, new DailySteps(50, "2022-12-10"), () -> {});
        service.insert(this, new DailySteps(87, "2022-12-11"), () -> {});
        service.insert(this, new DailySteps(0, "2022-12-12"), () -> {});
        service.insert(this, new DailySteps(45, "2022-12-13"), () -> {});
        service.insert(this, new DailySteps(45, "2022-12-14"), () -> {});
        service.insert(this, new DailySteps(34, "2022-12-15"), () -> {});
        service.insert(this, new DailySteps(90, "2022-12-16"), () -> {});

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_logout){
            if(StepCountService.RUNNING)
                StepCountService.stopStepCountingService(this);

            ApiService.getInstance(this).logout();
            TokensDaoService daoService = new TokensDaoService(this);
            daoService.deleteAllExcept(this, "LOCAL_USER");

            Intent loginActivityIntent = new Intent(this, LoginActivity.class);
            startActivity(loginActivityIntent);

            return true;
        }

        return false;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        switch (item.getItemId()) {
            case R.id.nav_steps_item:
                fragment = new HomeFragment();
                break;
            case R.id.nav_report_item:
                fragment = new ReportFragment();
                break;
//            case R.id.nav_profile_item:
//                fragment = new ProfileFragment();
//                break;

            default:
        }
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // Ask for permission
    private void getActivity() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACTIVITY_RECOGNITION},
                    REQUEST_ACTIVITY_RECOGNITION_PERMISSION);
        }
    }

    private void createNotificationChannel() {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NotificationChannels.DEFAULT_NOTIFICATION_CHANNEL.getValue(), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ACTIVITY_RECOGNITION_PERMISSION) {
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getActivity();
                } else {
                    Toast.makeText(this,
                            R.string.step_permission_denied,
                            Toast.LENGTH_SHORT).show();
                }
        }
    }

}
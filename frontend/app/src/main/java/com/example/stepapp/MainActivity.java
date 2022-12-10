package com.example.stepapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.Toast;

import com.example.stepapp.service.StepCountService;
import com.example.stepapp.ui.home.HomeFragment;
import com.example.stepapp.ui.report.ReportFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
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

        // Start step-counting BG service
        if(!StepCountService.RUNNING) StepCountService.startStepCountingService(this);

        // Ask for activity recognition permission
        if (runningQOrLater) {
            getActivity();
        }

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
    protected void onDestroy() {
        super.onDestroy();

        // Stop step-counting BG service
        if(StepCountService.RUNNING) StepCountService.stopStepCountingService(this);

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
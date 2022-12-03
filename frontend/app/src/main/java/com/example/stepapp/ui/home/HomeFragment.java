package com.example.stepapp.ui.home;

import android.os.Bundle;
import android.os.Handler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;

import com.example.stepapp.R;
import com.example.stepapp.service.StepCountService;

public class HomeFragment extends Fragment {

    // Text view and Progress Bar variables
    public TextView stepsCountTextView;
    public ProgressBar stepsCountProgressBar;

    private Handler updateHandler;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        this.stepsCountTextView = root.findViewById(R.id.stepsCount);
        this.stepsCountProgressBar = root.findViewById(R.id.progressBar);

        updateHandler = new Handler();
        updateHandler.post(updateStepsView);

        return root;

    }

    private final Runnable updateStepsView = new Runnable() {
        @Override
        public void run() {
            if(StepCountService.RUNNING){
                stepsCountTextView.setText(String.valueOf(StepCountService.dailySteps.getSteps()));
                stepsCountProgressBar.setProgress(StepCountService.dailySteps.getSteps());
            }
            updateHandler.postDelayed(this, 1000);
        }
    };

}

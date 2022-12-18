package ch.disappointment.WalkoutCompanion.ui.home;

import android.os.Bundle;
import android.os.Handler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;

import ch.disappointment.WalkoutCompanion.DayUtils;
import ch.disappointment.WalkoutCompanion.R;
import ch.disappointment.WalkoutCompanion.api.ApiService;
import ch.disappointment.WalkoutCompanion.persistence.DailyStepsDaoService;
import ch.disappointment.WalkoutCompanion.persistence.DailyStepsLocalDaoService;
import ch.disappointment.WalkoutCompanion.persistence.DailyStepsRemoteDaoService;
import ch.disappointment.WalkoutCompanion.persistence.model.DailySteps;
import ch.disappointment.WalkoutCompanion.service.StepCountService;

public class HomeFragment extends Fragment {

    // Text view and Progress Bar variables
    public TextView stepsCountTextView;
    public TextView goalTextView;
    public ProgressBar stepsCountProgressBar;
    public Button toggleServiceButton;

    private Handler updateHandler;
    private DailyStepsDaoService<DailySteps> stepsDaoService;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        this.stepsCountTextView = root.findViewById(R.id.stepsCount);
        this.stepsCountProgressBar = root.findViewById(R.id.progressBar);
        this.toggleServiceButton = root.findViewById(R.id.startServiceBtn);
        this.goalTextView = root.findViewById(R.id.goal_text_view);

        if(StepCountService.RUNNING)
            this.toggleServiceButton.setText(getText(R.string.stop_service_button_text));
        else
            this.toggleServiceButton.setText(getText(R.string.start_service_button_text));

        this.toggleServiceButton.setOnClickListener((view) -> {
            if(StepCountService.RUNNING){
                this.toggleServiceButton.setText(getText(R.string.start_service_button_text));
                StepCountService.stopStepCountingService(requireContext());
            }else{
                this.toggleServiceButton.setText(getText(R.string.stop_service_button_text));
                StepCountService.startStepCountingService(requireContext());
            }
        });

        // Load latest step count
        if(ApiService.getInstance(getContext()).isLocal())
            stepsDaoService = new DailyStepsLocalDaoService(getContext());
        else
            stepsDaoService = new DailyStepsRemoteDaoService();

        stepsDaoService.get(getContext(), DayUtils.getCurrentDay(), (dailySteps -> {
            if(dailySteps == null) {
                dailySteps = new DailySteps();
                dailySteps.date = DayUtils.getCurrentDay();
                dailySteps.steps = 0;
            }

            StepCountService.dailySteps.steps = dailySteps.steps;
            StepCountService.dailySteps.date = dailySteps.date;
        }));

        stepsDaoService.getGoal(getContext(), goal -> {
            StepCountService.goal = goal;
        });

        updateHandler = new Handler();
        updateHandler.post(updateStepsView);

        return root;
    }

    private final Runnable updateStepsView = new Runnable() {
        @Override
        public void run() {
            stepsCountTextView.setText(String.valueOf(StepCountService.dailySteps.getSteps()));
            stepsCountProgressBar.setProgress(StepCountService.dailySteps.getSteps());
            stepsCountProgressBar.setMax(StepCountService.goal);
            goalTextView.setText("Goal: " + StepCountService.goal);
            updateHandler.postDelayed(this, 500);
        }
    };

}

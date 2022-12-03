package com.example.stepapp.ui.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.stepapp.R;
import com.example.stepapp.persistence.DailyStepsLocalDaoService;
import com.jjoe64.graphview.GraphView;

public class ReportFragment extends Fragment {

    private GraphView userStepsGraph;
    private TextView dailyStepsLabel;
    private TextView averageStepsLabel;

    DailyStepsLocalDaoService localService;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.report_fragment, container, false);

        userStepsGraph = root.findViewById(R.id.UserStepsGraph);
        dailyStepsLabel = root.findViewById(R.id.DailyStepsLabel);
        averageStepsLabel = root.findViewById(R.id.AverageStepsLabel);

        localService = new DailyStepsLocalDaoService(getContext());

        //TODO fetch last 30 days of steps and update graph

        //TODO update labels

        return root;
    }
}
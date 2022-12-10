package com.example.stepapp.ui.report;

import static com.example.stepapp.service.StepCountService.dailySteps;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.anychart.AnyChart;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.enums.Anchor;
import com.anychart.graphics.vector.Stroke;
import com.example.stepapp.R;
import com.example.stepapp.api.ApiService;
import com.example.stepapp.persistence.DailyStepsLocalDaoService;

import com.anychart.AnyChartView;
import com.example.stepapp.persistence.model.DailySteps;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class ReportFragment extends Fragment {

    private AnyChartView userStepsGraph;
    private TextView dailyStepsLabel;
    private TextView averageStepsLabel;

    private LineGraphManager<DailyStepsDataEntry> manager;

    DailyStepsLocalDaoService localService;

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.report_fragment, container, false);

        userStepsGraph = root.findViewById(R.id.UserStepsGraph);
        dailyStepsLabel = root.findViewById(R.id.DailyStepsLabel);
        averageStepsLabel = root.findViewById(R.id.AverageStepsLabel);

        localService = new DailyStepsLocalDaoService(getContext());

        //TODO setup chart

        userStepsGraph.setProgressBar(root.findViewById(R.id.loadingBar));

        manager = new LineGraphManager<>(userStepsGraph);
        manager.setTitle("Daily Steps over 30 days");
        manager.setYTitle("Number of Steps");

        //TODO fetch last 30 days LOCAL

        List<DailySteps> userSteps = localService.getAll(getContext());

        userSteps = userSteps.stream()
                .sorted((o1, o2) -> {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDateTime d1 = LocalDateTime.parse(o1.getDay(), formatter);
                    LocalDateTime d2 = LocalDateTime.parse(o2.getDay(), formatter);
                    return d1.compareTo(d2);
                }).limit(30).collect(Collectors.toList());

        //TODO fetch last 30 days REMOTE


        //TODO add entries

        if(!userSteps.isEmpty()){
            for(int i = 0; i < 30 && i < userSteps.size(); i++){
                DailySteps steps = userSteps.get(i);
                manager.addEntry(new DailyStepsDataEntry(steps.getDay(), steps.getSteps(), null));
            }
        }

        manager.addSeries(ApiService.getInstance(getContext()).getLoggedUser().getUsername(), manager.getMappingString("x", "value"));

        manager.render();

        //TODO update labels

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        jdf.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        String date = jdf.format(new Date());
        String day = date.substring(0,10);

        Optional<DailySteps> today =
                userSteps.stream()
                .filter(dailySteps -> dailySteps.getDay().equals(day))
                .findFirst();

        today.ifPresent(dailySteps -> dailyStepsLabel.setText(getResources().getString(R.string.dailyStepsLabel) + dailySteps.getSteps()));

        int average = 0;

        if(!userSteps.isEmpty()){
            for(int i = 0; i < 30 && i < userSteps.size(); i++)
                average += userSteps.get(i).getSteps();

            average /= Math.min(userSteps.size(), 30);
        }

        averageStepsLabel.setText(getResources().getString(R.string.averageStepsLabel) + average);

        return root;
    }
}

class DailyStepsDataEntry extends ValueDataEntry {

    public DailyStepsDataEntry(String x, Number userValue, Number meanValue) {
        super(x, userValue);
        setValue("value2", meanValue);
    }
}

class LineGraphManager<T extends DataEntry> {

    AnyChartView graph;
    Cartesian cartesian;
    List<DataEntry> seriesData;
    Set set;

    public LineGraphManager(AnyChartView graph) {
        this.graph = graph;
        cartesian = AnyChart.line();
        seriesData = new ArrayList<>();
        set = Set.instantiate();
    }

    public void setTitle(String title) {
        cartesian.title(title);
    }

    public void setYTitle(String title) {
        cartesian.yAxis(0).title(title);
    }

    public void addEntry(T entry) {
        seriesData.add(entry);
    }

    public Mapping getMappingString(String x, String value) {
        return set.mapAs("{ x: '" + x + "', value: '" + value + "' }");
    }

    public void addSeries(String name, Mapping mapping) {
        Line series = cartesian.line(mapping);
        series.name(name);
        series.hovered().markers().enabled(true);
        series.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);
    }

    public void render() {

        cartesian.animation(true);

        cartesian.padding(10d, 20d, 5d, 20d);

        cartesian.crosshair().enabled(true);
        cartesian.crosshair()
                .yLabel(true)
                .yStroke((Stroke) null, null, null, (String) null, (String) null);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

        cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);

        set.data(seriesData);

        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 10d, 0d);

        graph.setChart(cartesian);

    }

}

class CustomDataEntry extends ValueDataEntry {

    CustomDataEntry(String x, Number value, Number value2, Number value3) {
        super(x, value);
        setValue("value2", value2);
        setValue("value3", value3);
    }

}
package ch.disappointment.WalkoutCompanion.ui.report;

import android.annotation.SuppressLint;
import android.os.Bundle;
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
import com.anychart.enums.HoverMode;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.enums.Anchor;
import com.anychart.graphics.vector.Fill;
import com.anychart.graphics.vector.Stroke;

import ch.disappointment.WalkoutCompanion.R;
import ch.disappointment.WalkoutCompanion.api.ApiService;
import ch.disappointment.WalkoutCompanion.persistence.DailyStepsDaoService;
import ch.disappointment.WalkoutCompanion.persistence.DailyStepsLocalDaoService;

import com.anychart.AnyChartView;
import ch.disappointment.WalkoutCompanion.persistence.DailyStepsRemoteDaoService;
import ch.disappointment.WalkoutCompanion.persistence.model.DailySteps;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Represents the report fragment that showcases user-to-userbase comparisons.
 *
 * Uses the AnyChart library 'com.github.AnyChart:AnyChart-Android:1.1.2'
 */

public class ReportFragment extends Fragment {

    private AnyChartView userStepsGraph;
    private TextView dailyStepsLabel;
    private TextView averageStepsLabel;

    private LineGraphManager<DailyStepsDataEntry> manager;

    DailyStepsDaoService<DailySteps> dailyStepsService;

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.report_fragment, container, false);

        userStepsGraph = root.findViewById(R.id.UserStepsGraph);
        dailyStepsLabel = root.findViewById(R.id.DailyStepsNumber);
        averageStepsLabel = root.findViewById(R.id.AverageStepsNumber);

        if(ApiService.getInstance(getContext()).isLocal())
            dailyStepsService = new DailyStepsLocalDaoService(getContext());
        else
            dailyStepsService = new DailyStepsRemoteDaoService();


        userStepsGraph.setProgressBar(root.findViewById(R.id.loadingBar));

        manager = new LineGraphManager<>(userStepsGraph);
        manager.setTitle("Daily Steps over 30 days");
        manager.setYTitle("Number of Steps");

        if(ApiService.getInstance(requireContext()).isLocal())
            dailyStepsService.getAll(getContext(), populateChartLocal());
        else
            dailyStepsService.getAll(getContext(), populateChartRemote());


        return root;
    }


    /**
     * Populates the line chart with the user's data
     * @return the function executed once the database has fetched remote data
     */
    private Consumer<List<DailySteps>> populateChartLocal(){
        return (res) ->{
            List<DailySteps> userSteps = res.stream()
                    .sorted((o1, o2) -> {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDateTime d1 = LocalDate.parse(o1.getDate(), formatter).atStartOfDay();
                        LocalDateTime d2 = LocalDate.parse(o2.getDate(), formatter).atStartOfDay();
                        return d1.compareTo(d2);
                    }).limit(30).collect(Collectors.toList());

            if (!userSteps.isEmpty()) {
                for (int i = 0; i < 30 && i < userSteps.size(); i++) {
                    DailySteps steps = userSteps.get(i);
                    manager.addEntry(new DailyStepsDataEntry(steps.getDate(), steps.getSteps(), null));
                }
            }

            manager.addSeries(ApiService.getInstance(getContext()).getLoggedUser().getUsername(), manager.getMappingString("x", "value"), "#FFC107");

            manager.render();

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
            jdf.setTimeZone(TimeZone.getTimeZone("GMT+2"));
            String date = jdf.format(new Date());
            String day = date.substring(0, 10);

            Optional<DailySteps> today =
                    userSteps.stream()
                            .filter(dailySteps -> dailySteps.getDate().equals(day))
                            .findFirst();


            today.ifPresent(dailySteps -> dailyStepsLabel.setText(String.valueOf(dailySteps.getSteps())));

            int average = 0;

            if (!userSteps.isEmpty()) {
                for (int i = 0; i < 30 && i < userSteps.size(); i++)
                    average += userSteps.get(i).getSteps();

                average /= Math.min(userSteps.size(), 30);
            }

            averageStepsLabel.setText(String.valueOf(average));
        };

    }

    /**
     * Populates the line chart with the user's data, compared with the average of the userbase
     * @return the function executed once the database has fetched remote data
     */
    private Consumer<List<DailySteps>> populateChartRemote() {
        return (res) -> {

            List<DailySteps> userSteps = res.stream()
                    .sorted((o1, o2) -> {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDateTime d1 = LocalDate.parse(o1.getDate(), formatter).atStartOfDay();
                        LocalDateTime d2 = LocalDate.parse(o2.getDate(), formatter).atStartOfDay();
                        return d1.compareTo(d2);
                    }).skip(res.size()-30).collect(Collectors.toList());

            manager.addSeries(ApiService.getInstance(getContext()).getLoggedUser().getUsername(), manager.getMappingString("x", "value"), "#FFC107");
            manager.addSeries("Other Users Average", manager.getMappingString("x", "value2"), "#E64A19");

            dailyStepsService.getAllExceptUser(getContext(), otherUsers ->{

                // Fetch steps from all users, sort by day, get average for each day

                TreeMap<String, List<DailySteps>> stepsByDay = new TreeMap<>();

                List<String> dates = otherUsers.stream()
                        .map(dailySteps -> dailySteps.date)
                        .distinct()
                        .collect(Collectors.toList());

                dates.forEach(date -> stepsByDay.put(date, new ArrayList<>()));

                otherUsers.forEach(user ->{
                    Objects.requireNonNull(stepsByDay.get(user.date)).add(user);
                });

                TreeMap<String, Double> averageStepsByDay = new TreeMap<>((o1, o2) -> {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDateTime d1 = LocalDate.parse(o1, formatter).atStartOfDay();
                    LocalDateTime d2 = LocalDate.parse(o2, formatter).atStartOfDay();
                    return d1.compareTo(d2);
                });

                for(String key : stepsByDay.keySet()){

                    List<DailySteps> list = stepsByDay.get(key);

                    if(list != null) {

                        OptionalDouble avg = list.stream().mapToInt(d -> d.steps).average();

                        if(avg.isPresent())
                            averageStepsByDay.put(key, avg.getAsDouble());

                    }

                }

                // For each daily average, if we are within 30 days, find the corresponding value from the userSteps and add as an entry

                if (!averageStepsByDay.isEmpty()) {
                    averageStepsByDay.forEach((key, val) -> {

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDateTime now = LocalDateTime.now();
                        LocalDateTime dateOfAverage = LocalDate.parse(key, formatter).atStartOfDay();
                        LocalDateTime currentMinus30Days = now.minusDays(30);

                        if(!dateOfAverage.isBefore(currentMinus30Days)) {

                            Integer userStepsToday = null;

                            for(int i = 0; i < userSteps.size(); i++){
                                if(userSteps.get(i).date.equals(key)){
                                    userStepsToday = userSteps.get(i).steps;
                                    break;
                                }
                            }

                            manager.addEntry(new DailyStepsDataEntry(key, userStepsToday, val));

                        }

                    });
                }

                manager.render();

                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
                jdf.setTimeZone(TimeZone.getTimeZone("GMT+2"));
                String date = jdf.format(new Date());
                String day = date.substring(0, 10);

                Optional<DailySteps> today =
                        userSteps.stream()
                                .filter(dailySteps -> dailySteps.getDate().equals(day))
                                .findFirst();


                today.ifPresent(dailySteps -> dailyStepsLabel.setText(String.valueOf(dailySteps.getSteps())));

                int average = 0;

                if (!userSteps.isEmpty()) {
                    for (int i = 0; i < 30 && i < userSteps.size(); i++)
                        average += userSteps.get(i).getSteps();

                    average /= Math.min(userSteps.size(), 30);
                }

                averageStepsLabel.setText(String.valueOf(average));

            });

        };

    }

}

/**
 * Auxiliary class to represent a DailySteps instance within the chart
 */
class DailyStepsDataEntry extends ValueDataEntry {

    public DailyStepsDataEntry(String x, Number userValue, Number meanValue) {
        super(x, userValue);
        setValue("value2", meanValue);
    }
}

/**
 * Auxiliary class to manage an instance of a line chart.
 * @param <T>
 */
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

    public void addSeries(String name, Mapping mapping, String color) {
        Line series = cartesian.line(mapping);
        series.name(name);
        series.stroke(color);
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

        cartesian.background().fill("#00000000");
        cartesian.interactivity().hoverMode(HoverMode.BY_X);

        graph.setBackgroundColor("#00000000");
        graph.setChart(cartesian);

    }

}
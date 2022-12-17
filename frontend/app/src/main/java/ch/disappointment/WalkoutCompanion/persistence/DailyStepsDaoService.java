package ch.disappointment.WalkoutCompanion.persistence;

import android.content.Context;

import ch.disappointment.WalkoutCompanion.persistence.model.DailySteps;

import java.util.List;
import java.util.function.Consumer;

public interface DailyStepsDaoService<T extends DailySteps> {

    void get(Context context, String day, Consumer<T> onResult);

    void getAll(Context context, Consumer<List<T>> onResult);

    void getAllExceptUser(Context context, Consumer<List<T>> onResult);

    void insert(Context context, T record, Runnable onDone);

    void update(Context context, T record, Runnable onDone);

    void delete(Context context, T record, Runnable onDone);

    void setGoal(Context context, int goal, Runnable onDone);
    void getGoal(Context context, Consumer<Integer> onResult);
}

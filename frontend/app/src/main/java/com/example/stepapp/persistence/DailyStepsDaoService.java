package com.example.stepapp.persistence;

import android.content.Context;

import com.example.stepapp.persistence.model.DailySteps;

import java.util.List;
import java.util.function.Consumer;

public interface DailyStepsDaoService<T extends DailySteps> {

    void get(Context context, String day, Consumer<T> onResult);

    void getAll(Context context, Consumer<List<T>> onResult);

    void getAllExceptUser(Context context, Consumer<List<T>> onResult);

    void insert(Context context, T record, Runnable onDone);

    void update(Context context, T record, Runnable onDone);

    void delete(Context context, T record, Runnable onDone);

}

package com.example.stepapp.persistence;

import android.content.Context;

import com.example.stepapp.persistence.model.DailySteps;

import java.util.List;

public interface DailyStepsDaoService<T extends DailySteps> {

    T get(Context context, String day);

    List<T> getAllForTimeframe(Context context, int days);

    long insert(Context context, T record);

    long update(Context context, T record);

    long delete(Context context, T record);

}
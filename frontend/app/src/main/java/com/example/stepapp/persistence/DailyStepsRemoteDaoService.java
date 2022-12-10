package com.example.stepapp.persistence;

import android.content.Context;
import android.widget.Toast;

import com.example.stepapp.api.ApiService;
import com.example.stepapp.api.exception.ApiException;
import com.example.stepapp.persistence.model.DailySteps;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class DailyStepsRemoteDaoService implements DailyStepsDaoService<DailySteps> {
    @Override
    public void get(Context context, String day, Consumer<DailySteps> onResult) {
        ApiService.getInstance(context).getDailySteps(day, onResult, (err) -> {
            if(err.getStatus() != 404)
                this.catchError(context, err);
            onResult.accept(null);
        });
    }

    @Override
    public void getAll(Context context, Consumer<List<DailySteps>> onResult) {
        ApiService.getInstance(context).getAllSteps(
                (arr) -> onResult.accept(Arrays.asList(arr)),
                (err) -> {
                    if(err.getStatus() != 404)
                        this.catchError(context, err);
                    onResult.accept(new ArrayList<>());
                });
    }

    @Override
    public void getAllExceptUser(Context context, Consumer<List<DailySteps>> onResult) {
        ApiService.getInstance(context).getAllStepsExceptUser(
                (arr) -> onResult.accept(Arrays.asList(arr)),
                (err) -> {
                    if(err.getStatus() != 404)
                        this.catchError(context, err);

                    onResult.accept(new ArrayList<>());
                });
    }

    @Override
    public void insert(Context context, DailySteps record, Runnable onDone) {
        try {
            ApiService.getInstance(context).setDailySteps(
                    record,
                    (arr) -> onDone.run(),
                    (err) -> {
                        this.catchError(context, err);
                        onDone.run();
                    });
        }catch (JSONException e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void update(Context context, DailySteps record, Runnable onDone) {
        try {
            ApiService.getInstance(context).setDailySteps(
                    record,
                    (arr) -> onDone.run(),
                    (err) -> {
                        this.catchError(context, err);
                        onDone.run();
                    });
        }catch (JSONException e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void delete(Context context, DailySteps record, Runnable onDone) {

        ApiService.getInstance(context).deleteDailySteps(
                record.date,
                onDone,
                (err) -> {
                    this.catchError(context, err);
                    onDone.run();
                });
    }

    private void catchError(Context ctx, ApiException err) {
        Toast.makeText(ctx, err.getMessage(), Toast.LENGTH_LONG).show();
    }
}

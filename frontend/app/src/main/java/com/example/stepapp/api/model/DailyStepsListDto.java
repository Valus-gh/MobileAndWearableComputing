package com.example.stepapp.api.model;

import com.example.stepapp.persistence.model.DailySteps;

public class DailyStepsListDto {
    private DailySteps[] items;

    public DailyStepsListDto(DailySteps[] items) {
        this.items = items;
    }

    public DailyStepsListDto() {
    }

    public DailySteps[] getItems() {
        return items;
    }

    public void setItems(DailySteps[] items) {
        this.items = items;
    }
}

package ch.disappointment.WalkoutCompanion.api.model;

import ch.disappointment.WalkoutCompanion.persistence.model.DailySteps;

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

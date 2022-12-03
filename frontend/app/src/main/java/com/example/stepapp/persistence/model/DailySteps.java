package com.example.stepapp.persistence.model;

public class DailySteps {

    public int steps;
    public String day;

    public DailySteps() {
    }

    public DailySteps(int steps, String day) {
        this.steps = steps;
        this.day = day;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void incrementSteps(){
        steps++;
    }

}

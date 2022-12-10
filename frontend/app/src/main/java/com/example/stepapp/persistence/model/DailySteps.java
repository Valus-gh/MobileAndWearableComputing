package com.example.stepapp.persistence.model;

public class DailySteps {

    public int steps;
    public String date;

    public DailySteps() {
    }

    public DailySteps(int steps, String date) {
        this.steps = steps;
        this.date = date;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void incrementSteps(){
        steps++;
    }

    @Override
    public String toString() {
        return "DailySteps{" +
                "steps=" + steps +
                ", day='" + date + '\'' +
                '}';
    }
}

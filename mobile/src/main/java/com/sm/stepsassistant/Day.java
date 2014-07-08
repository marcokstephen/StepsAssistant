package com.sm.stepsassistant;

import org.json.JSONException;
import org.json.JSONObject;

public class Day {
    private int day;
    private int month;
    private int year;
    private int stepCount;
    private int time;

    public Day(JSONObject dayObject){
        try {
            this.day = dayObject.getInt("day");
            this.month = dayObject.getInt("month");
            this.year = dayObject.getInt("year");
            this.stepCount = dayObject.getInt("steps");
            this.time = dayObject.getInt("msTime");
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    public int getDay() {
        return this.day;
    }

    public int getMonth() {
        return this.month;
    }

    public int getYear() {
        return this.year;
    }

    public int getStepCount() {
        return this.stepCount;
    }

    public int getTime() {
        return this.time;
    }

    @Override
    public String toString(){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("day",day);
            jsonObject.put("month",month);
            jsonObject.put("year",year);
            jsonObject.put("steps", stepCount);
            jsonObject.put("msTime",time);
            return jsonObject.toString();
        } catch (JSONException e){
            e.printStackTrace();
        }
        return super.toString();
    }
}

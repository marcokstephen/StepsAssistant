package com.sm.stepsassistant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.List;

public class HistoryListAdapter extends BaseAdapter {

    private LayoutInflater myInflater;
    private List<Day> dayList;

    public HistoryListAdapter(Context ctxt, List<Day> days){
        myInflater = (LayoutInflater)ctxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dayList = days;
    }

    @Override
    public int getCount() {
        return dayList.size();
    }

    @Override
    public Object getItem(int i) {
        return dayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null){
            view = myInflater.inflate(R.layout.history_listview_item, null);
        }
        int time = dayList.get(i).getTime();
        int hour,minute,second,day,month,year;
        hour = time/3600;
        time %= 3600;
        minute = time/60;
        time %= 60;
        second = time;
        day = dayList.get(i).getDay();
        month = dayList.get(i).getMonth()+1;
        year = dayList.get(i).getYear();

        TextView dateTextView = (TextView) view.findViewById(R.id.dateTextView);
        TextView stepTextView = (TextView) view.findViewById(R.id.stepsTextView);
        TextView timeTextView = (TextView) view.findViewById(R.id.timeTextView);

        String dayString = day+"/"+month+"/"+year;
        dateTextView.setText(dayString);
        stepTextView.setText(NumberFormat.getInstance().format(dayList.get(i).getStepCount())+" steps");
        timeTextView.setText(String.format("%02d",hour)+":"+String.format("%02d",minute)+":"+String.format("%02d",second));
        return view;
    }
}

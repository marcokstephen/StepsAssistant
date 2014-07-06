package com.sm.stepsassistant.fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sm.stepsassistant.R;

import java.text.NumberFormat;


public class MainFragment extends Fragment {

    private static int stepsShow;
    private static String timeShow;

    public MainFragment(int steps, String time){
        stepsShow = steps;
        timeShow = time;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        TextView stepsTextView = (TextView)view.findViewById(R.id.stepTextView);
        TextView timeTextView = (TextView)view.findViewById(R.id.timeTextView);
        stepsTextView.setText(NumberFormat.getInstance().format(stepsShow)+"");
        timeTextView.setText(timeShow);
        return view;
    }
}

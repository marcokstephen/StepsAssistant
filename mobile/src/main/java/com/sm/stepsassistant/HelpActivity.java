package com.sm.stepsassistant;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.sm.stepsassistant.R;

public class HelpActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        TextView helpTextView = (TextView) findViewById(R.id.helpTextView);

        String htmlString = "&#8226; To use this app, you must be actively connected to an Android Wear device.<p>" +
                " &#8226; To change statistics such as daily step goal, you must change settings on this mobile app<p> " +
                "&#8226;To request a feature or report a bug, please contact me at: apps@stephenmarcok.com";
        helpTextView.setText(Html.fromHtml(htmlString));
    }
}

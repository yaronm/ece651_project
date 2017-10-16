package com.example.owner.newactivity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by Owner on 2017-10-16.
 */

public class Display extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display);
        String username = getIntent().getStringExtra("Username");

        TextView tv = (TextView)findViewById(R.id.TVusername);
        tv.setText(username);
    }

}

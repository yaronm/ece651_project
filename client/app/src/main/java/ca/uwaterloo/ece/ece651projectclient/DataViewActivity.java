package ca.uwaterloo.ece.ece651projectclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

public class DataViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_view);

        final Blackboard blackboard = ((BlackboardApplication) getApplication()).getBlackboard();
        blackboard.userLocation().addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object o) {
        TextView textView = (TextView) findViewById(R.id.textView);
        TextView textView2 = (TextView) findViewById(R.id.textView2);
        textView.setText("" + blackboard.userLocation().value().getLatitude());
        textView2.setText("" + blackboard.userLocation().value().getLongitude());

           }
        });
    }
}

package ca.uwaterloo.ece.ece651projectclient;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class DataViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_view);
        application = (BlackboardApplication) getApplication();

        Observer dis_loc = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                //To display location
                TextView toDisplayLocation = (TextView) findViewById(R.id.displayLocation);
                if (application.getBlackboard().userLocation().value() == null) {
                    toDisplayLocation.setText(getString(R.string.No_userLocation));
                } else {
                    toDisplayLocation.setText(application.getBlackboard().userLocation().value().toString());
                }
            }
        };

        dis_loc.update(null, null);

        application.getBlackboard().userLocation().addObserver(dis_loc);

        Observer other_deltas = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                // get the other players deltas from the blackboard
                Map<String, PolarCoordinates> othersDeltas = application.getBlackboard().othersDeltas().value();
                // display each one
                TextView toDisplayDeltas = (TextView) findViewById(R.id.displayDeltas);
                if (application.getBlackboard().othersDeltas().value() == null) {
                    toDisplayDeltas.setText(getString(R.string.No_othersDeltaLocation));
                } else {
                    toDisplayDeltas.setText(getString(R.string.Deltas));
                    for (String userName : othersDeltas.keySet()) {
                        PolarCoordinates polarCoordinates = othersDeltas.get(userName);
                        toDisplayDeltas.append("\n  " + userName + ": " + polarCoordinates.getRho() + ", " + polarCoordinates.getPhi());
                    }
                }
            }
        };

        other_deltas.update(null, null);

        application.getBlackboard().othersDeltas().addObserver(other_deltas);

        Observer dis_gameId = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                //To display GameId
                TextView toDisplayGameId = (TextView) findViewById(R.id.displayGameId);
                if (application.getBlackboard().othersDeltas().value() == null) {
                    toDisplayGameId.setText(getString(R.string.No_gameId));
                } else {
                    toDisplayGameId.setText(application.getBlackboard().currentGameId().value().toString());
                }
            }
        };

        dis_gameId.update(null, null);

        application.getBlackboard().currentGameId().addObserver(dis_gameId);

        Observer dis_userName = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                //To display userName
                TextView toDisplayUserName = (TextView) findViewById(R.id.displayUserName);
                if (application.getBlackboard().userName().value() == null) {
                    toDisplayUserName.setText(getString(R.string.No_userName));
                } else {
                    toDisplayUserName.setText(application.getBlackboard().userName().value().toString());
                }
            }
        };

        dis_userName.update(null, null);

        application.getBlackboard().userName().addObserver(dis_userName);

        Observer dis_orientation = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                //To display user orientation
                TextView toDisplayOrientation = (TextView) findViewById(R.id.displayOrientation);
                if (application.getBlackboard().userOrientation().value() == null) {
                    toDisplayOrientation.setText(getString(R.string.No_userOrientation));
                } else {
                    toDisplayOrientation.setText(application.getBlackboard().userOrientation().value().toString());
                }
            }
        };

        dis_orientation.update(null, null);

        application.getBlackboard().userOrientation().addObserver(dis_orientation);

        Observer others_names = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                // get the other players names from the blackboard
                Set<String> othersNames = application.getBlackboard().othersNames().value();
                // display each one
                TextView toDisplayOthersNames = (TextView) findViewById(R.id.displayOthersNames);
                if (application.getBlackboard().othersNames().value() == null) {
                    toDisplayOthersNames.setText(getString(R.string.No_othersNames));
                } else {
                    for (String userName : othersNames)
                        toDisplayOthersNames.append("\n  " + userName);
                }
            }
        };

        others_names.update(null, null);

        application.getBlackboard().othersNames().addObserver(others_names);

        Observer others_location = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                // get the other players location from the blackboard
                Map<String, Location> others_location = application.getBlackboard().othersLocations().value();
                // display each one
                TextView toDisplayOtherLocation = (TextView) findViewById(R.id.displayOthersLocation);
                if (application.getBlackboard().othersLocations().value() == null) {
                    toDisplayOtherLocation.setText(getString(R.string.No_othersLocation));
                } else {
                    for (String userName : others_location.keySet())
                        toDisplayOtherLocation.append("\n  " + userName);
                }
            }
        };

        others_location.update(null, null);

        application.getBlackboard().othersLocations().addObserver(others_location);

    }

    BlackboardApplication application = new BlackboardApplication();

}

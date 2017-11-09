package ca.uwaterloo.ece.ece651projectclient;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

        Observer userNameObserver = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                TextView displayUserName = (TextView) findViewById(R.id.displayUserName);
                String userName = application.getBlackboard().userName().value();
                if (userName == null) {
                    displayUserName.setText("null");
                } else {
                    displayUserName.setText(userName.toString());
                }
            }
        };
        userNameObserver.update(null, null);
        application.getBlackboard().userName().addObserver(userNameObserver);

        Observer userLocationObserver = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                TextView displayUserLocation = (TextView) findViewById(R.id.displayUserLocation);
                Location userLocation = application.getBlackboard().userLocation().value();
                if (userLocation == null) {
                    displayUserLocation.setText("null");
                } else {
                    displayUserLocation.setText(userLocation.getLatitude() + ", " +
                            userLocation.getLongitude());
                }
            }
        };
        userLocationObserver.update(null, null);
        application.getBlackboard().userLocation().addObserver(userLocationObserver);

        Observer userOrientationObserver = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                TextView displayUserOrientation =
                        (TextView) findViewById(R.id.displayUserOrientation);
                Float userOrientation = application.getBlackboard().userOrientation().value();
                if (userOrientation == null) {
                    displayUserOrientation.setText("null");
                } else {
                    displayUserOrientation.setText(userOrientation.toString());
                }
            }
        };
        userOrientationObserver.update(null, null);
        application.getBlackboard().userOrientation().addObserver(userOrientationObserver);

        Observer currentGameIdObserver = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                TextView displayCurrentGameId = (TextView) findViewById(R.id.displayCurrentGameId);
                String currentGameId = application.getBlackboard().currentGameId().value();
                if (currentGameId == null) {
                    displayCurrentGameId.setText("null");
                } else {
                    displayCurrentGameId.setText(currentGameId);
                }
            }
        };
        currentGameIdObserver.update(null, null);
        application.getBlackboard().currentGameId().addObserver(currentGameIdObserver);

        Observer othersNamesObserver = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                TextView displayOthersNames = (TextView) findViewById(R.id.displayOthersNames);
                Set<String> othersNames = application.getBlackboard().othersNames().value();
                if (othersNames == null) {
                    displayOthersNames.setText("null");
                } else if (othersNames.isEmpty()) {
                    displayOthersNames.setText("empty");
                } else {
                    displayOthersNames.setText("");
                    for (String name : othersNames)
                        displayOthersNames.append("\n" + name);
                }
            }
        };
        othersNamesObserver.update(null, null);
        application.getBlackboard().othersNames().addObserver(othersNamesObserver);

        Observer othersLocationsObserver = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                TextView displayOthersLocations =
                        (TextView) findViewById(R.id.displayOthersLocations);
                Map<String, Location> othersLocations =
                        application.getBlackboard().othersLocations().value();
                if (othersLocations == null) {
                    displayOthersLocations.setText("null");
                } else if (othersLocations.isEmpty()) {
                    displayOthersLocations.setText("empty");
                } else {
                    displayOthersLocations.setText("");
                    for (String name : othersLocations.keySet()) {
                        double latitude = othersLocations.get(name).getLatitude();
                        double longitude = othersLocations.get(name).getLongitude();
                        displayOthersLocations.append("\n" + name + ": " +
                                latitude + ", " + longitude);
                    }
                }
            }
        };
        othersLocationsObserver.update(null, null);
        application.getBlackboard().othersLocations().addObserver(othersLocationsObserver);

        Observer othersDeltasObserver = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                TextView displayOthersDeltas = (TextView) findViewById(R.id.displayOthersDeltas);
                Map<String, PolarCoordinates> othersDeltas =
                        application.getBlackboard().othersDeltas().value();
                if (othersDeltas == null) {
                    displayOthersDeltas.setText("null");
                } else if (othersDeltas.isEmpty()) {
                    displayOthersDeltas.setText("empty");
                } else {
                    displayOthersDeltas.setText("");
                    for (String name : othersDeltas.keySet())
                        displayOthersDeltas.append("\n" + name + ": " + othersDeltas.get(name));
                }
            }
        };
        othersDeltasObserver.update(null, null);
        application.getBlackboard().othersDeltas().addObserver(othersDeltasObserver);
    }

    @Override
    public void onResume() {
        super.onResume();
        application.getBlackboard().currentActivity().set(this);
    }

    BlackboardApplication application = new BlackboardApplication();

}

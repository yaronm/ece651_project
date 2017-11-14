package ca.uwaterloo.ece.ece651projectclient;
/**
 * This class is for connection logic.
 * a. Get user location and orientation to update
 * b. Get other locations
 * c. compute deltas and update
 *
 * Created by Chen XU and Haoyuan Zhang on 2017/10/18.
 * @author Chen XU and Haoyuan Zhang
 *
 * Constructor
 * ConnectionLogic(Activity current_activity, Blackboard blackboard, String myname)();
 * @param Activity current_activity
 * @param Blackboard blackboard
 *
 *
 * Methods:
 * 1. void getOurLocation(); //start location service
 * This method can start monitor changes of user location
 * Create a instance of location manager and location listener;
 *
 * 2. void updateLocation(); //update our location
 * Update location of user to the blackboard;
 *
 * 3. void readOtherLocation(); //download other locations, compute deltas and update
 * Monitor and read other users locations from blackboard
 * Call computeDeltas(); to compute deltas
 * Update deltas to the blackboard;
 *
 * 4.deleteListener();
 * close listener service;
 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import static ca.uwaterloo.ece.ece651projectclient.GameState.JOINING;
import static ca.uwaterloo.ece.ece651projectclient.GameState.PAUSED;
import static ca.uwaterloo.ece.ece651projectclient.GameState.RUNNING;

class GameLogicLocation {

    //variables for initialization
    private Blackboard blackboard;
    private Context userContext;
    private Activity current_activity;
    private Date currentTime;
    private Date endTime;
    private long gameTime;


    //variables for location
    private LocationManager locationManager;
    private String provider;

    //set parameters
    /**
     * This is constructor of class GameLogicLocation
     * */
    public GameLogicLocation(Context userContext, Blackboard blackboard) {
        this.userContext = userContext;
        this.blackboard = blackboard;
        initialization();
    }

    /**
     * get user location
     * */
    public void getOurLocation() {
        // request for GPS permission
        current_activity = blackboard.currentActivity().value();
        if (ContextCompat.checkSelfPermission(userContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(current_activity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        }
        locationManager = (LocationManager) userContext.getSystemService(Context
                .LOCATION_SERVICE);
        //choose provider to get locations
        List<String> providerList = locationManager.getProviders(true);
        if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            Log.i("location", "No location provider to use");
            return;
        }


        //check whether program has permission to access GPS
        try {
            updateLocation(locationManager.getLastKnownLocation(provider));
            locationManager.requestLocationUpdates(provider, 5000, 1, locationListener);
        } catch (SecurityException e) {
            Log.i("location", "no permission to use GPS");
        }
    }

    /**
     * get other location
     * */
    public void readOtherLocation() {
        // as the game logic, observe the blackboard for changes to the other players locations
        blackboard.othersLocations().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                Log.i("location", "Observed that othersLocations blackboard field has been " +
                        "updated");
                // if the other players locations are updated, recompute their deltas
                computeDeltas();
            }
        });
        // also observe the blackboard for changes to the user location and also recompute deltas
        // when it changes
        blackboard.userLocation().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                Log.i("location", "Observed that userLocation blackboard field has been updated");
                computeDeltas();
            }
        });
    }

    /**
     * compute deltas
     * */
    public void computeDeltas() {
        // get the other players locations from the blackboard
        Map<String, Location> othersLocations = blackboard.othersLocations().value();
        // get the other players deltas from the blackboard
        Map<String, PolarCoordinates> othersDeltas = blackboard.othersDeltas().value();
        // clear the deltas (because we are about to recompute them)
        othersDeltas.clear();

        // recompute the deltas

        float distance;
        float bearing;
        Location one_person_location;
        // get the most-up-to-date user name irectly from the blackboard
        Location userLocation = blackboard.userLocation().value();
        // check that the user location is non-null before computing deltas; computing deltas when
        // the user location is undefined does not make sense
        if (userLocation != null)
            for (String otherName : othersLocations.keySet()) {
                one_person_location=othersLocations.get(otherName);
                distance = userLocation. distanceTo(one_person_location);
                bearing = userLocation. bearingTo(one_person_location);
                othersDeltas.put(otherName, new PolarCoordinates(distance, bearing));
            }
        // update the blackboard with those new deltas
        Log.i("location", "Updated othersDeltas blackboard field");
        blackboard.othersDeltas().set(othersDeltas);
    }

    /**
     * Updates the blackboard with the given location.
     *
     * @param location the location to pass to the blackboard
     */
    public void updateLocation(Location location) {
        // update the blackboard with  new locations
        Log.i("location", "Updated location to the blackboard field");
        blackboard.userLocation().set(location);
    }


    /**
     * Timer
     */
    public void timer(){
        endTime = blackboard.gameEndTime().value();
        currentTime = new Date();
        gameTime = endTime.getTime() - currentTime.getTime();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Log.d("location","location listener has been closed");
                deleteListener();
            }
        };
        timer.schedule(task,gameTime);

    }
    /**
     * delete Listener
     */
    public void deleteListener() {

        locationManager.removeUpdates(locationListener);
    }


    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
        Log.d("location","latitude is: "+location.getLatitude()+"longitude is: "+location.getLongitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    /**
     * observer the state
     */
    public void gameState() {
        blackboard.gameState().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                Log.i("location", "Observed that game state has been updated");
                    if(blackboard.gameState().value()==GameState.RUNNING){
                        getOurLocation();

                     }
                    else (blackboard.gameState().value()==GameState.PAUSED){
                        locationManager.removeUpdates(locationListener);
                    }

            }
        });
    }
    /**
     * initialization
     */
    public void initialization(){
        while (blackboard.gameState().value() != GameState.RUNNING){

        }
        getOurLocation();
        readOtherLocation();
        gameState();
        blackboard.gameEndTime().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                Log.i("location", "Observed that end time has been updated");
                timer();
            }
        });
    }

}

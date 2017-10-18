package ca.uwaterloo.ece.connectionlogic;

/**
 *  This class is for connection logic.
 *      a. Get user location and orientation to update
 *      b. Get other locations
 *      c. compute deltas and update
 *
 *  Created by Chen XU and Haoyuan Zhang on 2017/10/18.
 *  @author Chen XU and Haoyuan Zhang
 *
 *  Constructor
 *  ConnectionLogic(Activity current_activity, Blackboard blackboard, String myname)();
 *  @param  Activity current_activity
 *  @param  Blackboard blackboard
 *  @param  String username
 *
 *
 * Methods:
 * 1. void getOurLocation(); //start location service
 *    This method can start monitor changes of user location
 *    Create a instance of location manager and location listener;
 *
 * 2. void updateLocation(); //update our location
 *    Update location of user to the blackboard;
 *
 * 3. void readOtherLocation(); //download other locations, compute deltas and update
 *    Monitor and read other users locations from blackboard
 *    Call computeDeltas(); to compute deltas
 *    Update deltas to the blackboard;
 */

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class ConnectionLogic {


    private LocationManager locationManager;
    private String provider;
    private Location location;
    private float direction;
    private String myname;
    private Activity current_activity;
    private Blackboard blackboard;

    //set parameters
    /**
     * This is constructor of class ConnectionLogic
     * */
    public ConnectionLogic(Activity current_activity, Blackboard blackboard, String myname){
        this.current_activity=current_activity;
        this.blackboard=blackboard;
        this.myname = myname;
    }

    /**
    * get user location
    * */
    public void getOurLocation(){
    locationManager = (LocationManager) current_activity.getSystemService(Context.LOCATION_SERVICE);
    //choose provider to get locations
    List<String> providerList = locationManager.getProviders(true);
    if (providerList.contains(LocationManager.GPS_PROVIDER)){
        provider = LocationManager.GPS_PROVIDER;
    }else if (providerList.contains(LocationManager.NETWORK_PROVIDER)){
        provider = LocationManager.NETWORK_PROVIDER;
    }else {
        Log.i("location", "No location provider to use");
        return;
    }

    location = locationManager.getLastKnownLocation(provider);
    locationManager.requestLocationUpdates(provider, 5000, 1, new myLocationListener);
    }

    /**
     * get other location
     * */
    public void readOtherLocation(){
        // as the game logic, observe the blackboard for changes to the other players locations
        blackboard.othersLocations().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                Log.i("location", "Observed that othersLocations blackboard field has been updated");
                // if the other players locations are updated, recompute their deltas
                computeDeltas();
            }
        });
    }

    /**
     * compute deltas
     * */
    public void computeDeltas{
        // get the other players locations from the blackboard
        Map<String, Location> othersLocations = blackboard.othersLocations().value();
        // get the other players deltas from the blackboard
        Map<String, PolarCoordinates> othersDeltas = blackboard.othersDeltas().value();
        // clear the deltas (because we are about to recompute them)
        othersDeltas.clear();

        // recompute the deltas
        float deltas[]=new float[3];
        for (String username: othersLocations.keySet()) {
            if(username != myname){
                Location.distanceBetween(location.getLatitude(),location.getLongitude(),
                        othersLocations.get(username).getLatitude(),
                        othersLocations.get(username).getLongitude(),deltas);
                othersDeltas.put(username, new PolarCoordinates(deltas[0],deltas[1]));
            }
        }
        // update the blackboard with those new deltas
        Log.i("location", "Updated othersDeltas blackboard field");
        blackboard.othersDeltas().set(othersDeltas);
    }

    /**
     * updateLocation
     * */
    public void updateLocation() {

        // get the other players locations from the blackboard
        Map<String, Location> othersLocations = blackboard.othersLocations().value();
        // add the updated locations
        othersLocations.put(myname,location);
        // update the blackboard with those new locations
        Log.i("location", "Updated othersLocations blackboard field");
        blackboard.othersLocations().set(othersLocations);
    }

    /**
     * get current orientation
     */
    public static void changeDirection{
        direction = location.getBearing();
    }

    /**
     * update direction
     * */
    public void updateOrientation() {
        blackboard.userOrientation().set(direction);
    }

}



/**
 * This class is just tools for detect changes of location
 * */
//listen changes of location
class myLocationListener implements LocationListener {
    @Override
    public void onLocationChanged(Location location) {
        ConnectionLogic.changeDirection();
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
}
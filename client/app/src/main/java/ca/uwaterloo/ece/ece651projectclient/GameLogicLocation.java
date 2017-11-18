package ca.uwaterloo.ece.ece651projectclient;

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

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Game logic class for handling changes in location.
 */
class GameLogicLocation {

    private static final String TAG = "GLLocation";

    /**
     * Creates a location game logic component.
     *
     * @param blackboard a blackboard
     * @param context    a application context for accessing device location; if null, component
     *                   will expect to receive location updates via direct method invocation
     */
    public GameLogicLocation(final Blackboard blackboard, Context context) {
        // store blackboard and context
        this.blackboard = blackboard;
        this.context = context;
        // get the location manager from the given context
        manager = context == null ? null :
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // configure component to listen for game state changes
        blackboard.gameState().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                // get the game state
                GameState state = blackboard.gameState().value();
                // enable sensors only when the game is in the RUNNING state
                switch (state) {
                    case RUNNING:
                        enableLocation();
                        break;
                    default:
                        disableLocation();
                        break;
                }
            }
        });
    }

    private Blackboard blackboard;
    private Context context;

    private LocationManager manager;
    private LocationListener listener;

    /**
     * Enables location updates to the blackboard.
     */
    private void enableLocation() {
        // check that the location manager is available
        if (manager == null) {
            Log.d(TAG, "Could not enable location updates: location manager is null");
            return;
        }
        // clear previous listeners
        disableLocation();
        // create a new listener
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // update the user location
                updateLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };
        // choose provider to get locations
        List<String> providerList = manager.getProviders(true);
        String provider;
        if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            Log.i(TAG, "Could not enable location updates: no location provider to use");
            return;
        }
        Log.i(TAG, "Selected location provider: " + provider);
        // check that we have permission to access the user location
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // check that the current activity is available
            Activity currentActivity = blackboard.currentActivity().value();
            if (currentActivity == null) {
                Log.d(TAG, "Could not enable location updates: current activity is null");
                return;
            }
            // request permissions to access the user location
            ActivityCompat.requestPermissions(currentActivity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        // register location listener
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            manager.requestLocationUpdates(provider, 5000, 1, listener);
            updateLocation(manager.getLastKnownLocation(provider));
        } else {
            Log.d(TAG, "Could not enable location updates: location permissions not given");
            return;
        }
    }

    /**
     * Disables location updates to the blackboard.
     */
    private void disableLocation() {
        // check that the location manager is available
        if (manager == null) {
            Log.d(TAG, "Could not enable location updates: location manager is null");
            return;
        }
        // diable location listener
        if (listener != null) {
            manager.removeUpdates(listener);
        }
    }

    /**
     * Updates the blackboard with the given location.
     *
     * @param location the location to pass to the blackboard
     */
    public void updateLocation(Location location) {
        // update the blackboard with a new location
        blackboard.userLocation().set(location);
    }

}

package ca.uwaterloo.ece.ece651projectclient.example;

import android.location.Location;
import android.util.Log;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import ca.uwaterloo.ece.ece651projectclient.Blackboard;

/**
 * An example database connection demonstrating setting and observing blackboard data.
 */
public class ExampleBlackboardConnectionDatabase {

    private static final String TAG = "ExampleBlkConDatabase";

    public ExampleBlackboardConnectionDatabase(Blackboard blackboard) {
        // store the blackboard
        this.blackboard = blackboard;
        // as the database connection, observe the blackboard for changes to the username and
        // current game id that should be used to identify from which game to pull data
        blackboard.userName().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                Log.i(TAG, "Observed that userName blackboard field has been updated");
                downloadOthersLocations();
            }
        });
        blackboard.currentGameId().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                Log.i(TAG, "Observed that currentGameId blackboard field has been updated");
                downloadOthersLocations();
            }
        });
    }

    private Blackboard blackboard;

    /**
     * Downloads (or pretends to download) other players locations from the server.
     */
    protected void downloadOthersLocations() {
        // get the other players locations from the blackboard
        Map<String, Location> othersLocations = blackboard.othersLocations().value();
        // add the (mock) downloaded locations
        othersLocations.put("otherUser1", new Location("someProvider"));
        othersLocations.put("otherUser2", new Location("someProvider"));
        othersLocations.put("otherUser3", new Location("someProvider"));
        // update the blackboard with those new locations
        Log.i(TAG, "Updated othersLocations blackboard field");
        blackboard.othersLocations().set(othersLocations);
    }

}

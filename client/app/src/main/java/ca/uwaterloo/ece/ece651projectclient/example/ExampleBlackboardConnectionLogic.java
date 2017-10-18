package ca.uwaterloo.ece.ece651projectclient.example;

import android.location.Location;
import android.util.Log;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import ca.uwaterloo.ece.ece651projectclient.Blackboard;
import ca.uwaterloo.ece.ece651projectclient.PolarCoordinates;

/**
 * An example game logic demonstrating setting and observing blackboard data.
 */
public class ExampleBlackboardConnectionLogic {

    private static final String TAG = "ExampleBlkConLogic";
    private Blackboard blackboard;


    public ExampleBlackboardConnectionLogic(Blackboard blackboard) {
        // store the blackboard
        this.blackboard = blackboard;
        // as the game logic, observe the blackboard for changes to the other players locations
        blackboard.othersLocations().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                Log.i(TAG, "Observed that othersLocations blackboard field has been updated");
                // if the other players locations are updated, recompute their deltas
                computeDeltas();
            }
        });
    }


    /**
     * Computes the (mock) deltas from the updated other players locations.
     */
    protected void computeDeltas() {
        // get the other players locations from the blackboard
        Map<String, Location> othersLocations = blackboard.othersLocations().value();
        // get the other players deltas from the blackboard
        Map<String, PolarCoordinates> othersDeltas = blackboard.othersDeltas().value();
        // clear the deltas (because we are about to recompute them)
        othersDeltas.clear();
        // recompute the (mock) deltas
        for (String userName: othersLocations.keySet())
            othersDeltas.put(userName, new PolarCoordinates(0, 0));
        // update the blackboard with those new deltas
        Log.i(TAG, "Updated othersDeltas blackboard field");
        blackboard.othersDeltas().set(othersDeltas);
    }

}

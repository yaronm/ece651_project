package ca.uwaterloo.ece.ece651projectclient;

import android.location.Location;
import android.util.Log;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * Game logic class for handling updating the deltas to other players.
 */
public class GameLogicDeltas {

    /**
     * Creates a delta updating game logic component.
     *
     * @param blackboard a blackboard
     */
    public GameLogicDeltas(Blackboard blackboard) {
        // store the blackboard
        this.blackboard = blackboard;
        // configure component to listen for game state changes
        Observer listener = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                updateDeltas();
            }
        };
        blackboard.userLocation().addObserver(listener);
        blackboard.othersLocations().addObserver(listener);
    }

    private Blackboard blackboard;

    /**
     * Updates the blackboard with the correct deltas.
     */
    public void updateDeltas() {
        // get the other players locations from the blackboard
        Map<String, Location> othersLocations = blackboard.othersLocations().value();
        // get the other players deltas from the blackboard
        Map<String, PolarCoordinates> othersDeltas = blackboard.othersDeltas().value();
        // clear the deltas (because we are about to recompute them)
        othersDeltas.clear();
        // get the user name
        Location userLocation = blackboard.userLocation().value();
        // check that the user location is non-null before computing deltas
        if (userLocation != null) {
            for (String otherName : othersLocations.keySet()) {
                Location othersLocation = othersLocations.get(otherName);
                float distance = userLocation.distanceTo(othersLocation);
                float bearing = userLocation.bearingTo(othersLocation);
                othersDeltas.put(otherName, new PolarCoordinates(distance, bearing));
            }
        }
        // update the blackboard with those new deltas
        blackboard.othersDeltas().set(othersDeltas);
    }

}

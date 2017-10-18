package ca.uwaterloo.ece.ece651projectclient;

import android.location.Location;

import java.util.Map;
import java.util.Set;

/**
 * An abstract blackboard interface. Provides {@link BlackboardData} objects that store observable
 * data.
 */
public interface Blackboard {

    /**
     * @return the unique identifier string of the current game
     */
    public BlackboardData<String> currentGameId();

    /**
     * @return the distance and bearing to each other user in the current game, indexed by
     *         username; bearings are represented in degrees East of true North
     */
    public BlackboardData<Map<String, PolarCoordinates>> othersDeltas();

    /**
     * @return the location of each other user in the current game, indexed by username
     */
    public BlackboardData<Map<String, Location>> othersLocations();

    /**
     * @return the set of all other users in the current game, represented by their usernames
     */
    public BlackboardData<Set<String>> othersNames();

    /**
     * @return the location of the user
     */
    public BlackboardData<Location> userLocation();

    /**
     * @return the username of the user
     */
    public BlackboardData<String> userName();

    /**
     * @return the orientation of the user; represented in degrees East of true North
     */
    public BlackboardData<Float> userOrientation();

}

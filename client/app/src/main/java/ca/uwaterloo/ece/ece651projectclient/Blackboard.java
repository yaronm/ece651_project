package ca.uwaterloo.ece.ece651projectclient;

import android.app.Activity;
import android.location.Location;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * An abstract blackboard interface. Provides {@link BlackboardData} objects that store observable
 * data.
 */
public interface Blackboard {

    /**
     * @return the currently active activity
     */
    public BlackboardData<Activity> currentActivity();

    /**
     * @return the unique identifier string of the current game
     */
    public BlackboardData<String> currentGameId();

    /**
     * @return the time and date at which the game will end
     */
    public BlackboardData<Date> gameEndTime();

    /**
     * @return the state of the game.
     */
    public BlackboardData<GameState> gameState();

    /**
     * @return the number of players in the current game; values greater than
     * <code>{@link #othersNames()}.value().size() + 1</code> indicate open player slots in the
     * current game that have not yet been filled
     */
    public BlackboardData<Integer> numberOfPlayers();

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
     * @return the set of all other users in the current game, represented by their username
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

    /**
     * @return the visibility matrix of the current game
     */
    public BlackboardData<Map<String, Set<String>>> visibilityMatrix();

    /**
     * @return the type of the visibility matrix of the current game
     */
    public BlackboardData<VisibilityMatrixType> visibilityMatrixType();

}

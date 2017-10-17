package ca.uwaterloo.ece.ece651projectclient;

import android.location.Location;

import java.util.Map;
import java.util.Set;

/**
 * A blackboard interface for use in implementing a blackboard architectural style.
 */
public interface Blackboard {

    /**
     * Adds a blackboard observer to be notified when this blackboard is updated.
     *
     * @param observer a blackboard observer object
     * @throws NullPointerException if observer is null
     */
    public void addObserver(BlackboardObserver observer);

    /**
     * Retrieves the distance and bearing to each other user. Bearing values are represented in
     * degrees East of true North.
     *
     * @return a map from usernames to polar coordinate distance/bearing pairs
     */
    public Map<String, PolarCoordinates> getDeltas();

    /**
     * @return the current orientation of the user in degrees East of true North.
     */
    public float getOrientation();

    /**
     * @return the identifier string that uniquely identifies the current game
     */
    public String getGameId();

    /**
     * @return the current location of the user or null, if the location cannot be determined
     */
    public Location getLocation();

    /**
     * Retrieves the location of each other user.
     *
     * @return a map from usernames to locations
     */
    public Map<String, Location> getLocations();

    /**
     * @return the username that uniquely identifies the current user
     */
    public String getUsername();

    /**
     * Retrieves the username of every other user participating in the current game.
     *
     * @return a set of usernames
     */
    public Set<String> getUsernames();

    /**
     * Removes an blackboard observer that should no longer be notified when this blackboard is
     * updated.
     *
     * @param observer a blackboard observer object
     * @throws NullPointerException if observer is null
     */
    public void removeObserver(BlackboardObserver observer);

    /**
     * Stores the distance and bearing to each other user. Bearing values are represented in degrees
     * East of true North. Passing null is equivalent to passing an empty collection.
     *
     * @param deltas a map from usernames to polar coordinate distance/bearing pairs
     */
    public void setDeltas(Map<String, PolarCoordinates> deltas);

    /**
     * Sets the current orientation of the user in degrees East of true North.
     *
     * @param orientation the current orientation of the user
     */
    public void setOrientation(float orientation);

    /**
     * Sets the identifier string that uniquely identifies the current game
     *
     * @param id a unique identifier string
     * @throws NullPointerException if id is null
     */
    public void setGameId(String id);

    /**
     * Sets the current location of the user. A null location indicates that the current location
     * cannot be determined.
     *
     * @param location a location
     */
    public void setLocation(Location location);

    /**
     * Stores the location of each other user. Passing null is equivalent to passing an empty
     * collection.
     *
     * @param locations a map from usernames to locations
     */
    public void setLocations(Map<String, Location> locations);

    /**
     * Sets the username that uniquely identifies the current user
     *
     * @param username a unique username
     * @throws NullPointerException if username is null
     */
    public void setUsername(String username);

    /**
     * Stores the username of every other user participating in the current game. Passing null is
     * equivalent to passing an empty collection.
     *
     * @param usernames a set of usernames
     */
    public void setUsernames(Set<String> usernames);

}

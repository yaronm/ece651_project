package ca.uwaterloo.ece.ece651projectclient;

import android.location.Location;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A concrete blackboard implementation.
 *
 * @see Blackboard
 */
public class ConcreteBlackboard implements Blackboard {

    List<BlackboardObserver> observers = new LinkedList<>();
    Map<String, PolarCoordinates> deltas = new HashMap<>();
    float orientation = 0;
    String gameId = "";
    Location location = null;
    Map<String, Location> locations = new HashMap<>();
    String username = "";
    Set<String> usernames = new HashSet<>();

    @Override
    public void addObserver(BlackboardObserver observer) {
        if (observer == null)
            throw new NullPointerException();
        observers.add(observer);
    }

    @Override
    public Map<String, PolarCoordinates> getDeltas() {
        return Collections.unmodifiableMap(deltas);
    }

    @Override
    public float getOrientation() {
        return orientation;
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public Map<String, Location> getLocations() {
        return Collections.unmodifiableMap(locations);
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Set<String> getUsernames() {
        return Collections.unmodifiableSet(usernames);
    }

    @Override
    public void removeObserver(BlackboardObserver observer) {
        if (observer == null)
            throw new NullPointerException();
        observers.remove(observer);
    }

    @Override
    public void setDeltas(Map<String, PolarCoordinates> deltas) {
        if (deltas == null)
            this.deltas = new HashMap<>();
        else
            this.deltas = new HashMap<>(deltas);
        notifyObservers();
    }

    @Override
    public void setOrientation(float orientation) {
        this.orientation = orientation;
    }

    @Override
    public void setGameId(String id) {
        if (id == null)
            throw new NullPointerException();
        gameId = id;
    }

    @Override
    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public void setLocations(Map<String, Location> locations) {
        if (locations == null)
            this.locations = new HashMap<>();
        else
            this.locations = new HashMap<>(locations);
        notifyObservers();
    }

    @Override
    public void setUsername(String username) {
        if (username == null)
            throw new NullPointerException();
        this.username = username;
    }

    @Override
    public void setUsernames(Set<String> usernames) {
        if (usernames == null)
            this.usernames = new HashSet<>();
        else
            this.usernames = new HashSet<>(usernames);
        notifyObservers();
    }

    /**
     * Notifies each observer that this blackboard has been updated.
     */
    protected void notifyObservers() {
        for (BlackboardObserver observer : observers)
            observer.onUpdate(this);
    }

}

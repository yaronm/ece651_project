package ca.uwaterloo.ece.ece651projectclient;

import android.location.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConcreteBlackboard implements Blackboard {

    private final BlackboardData<String> currentGameId = new BlackboardData<>("");

    @Override
    public BlackboardData<String> currentGameId() {
        return currentGameId;
    }

    private final BlackboardData<Map<String, PolarCoordinates>> othersDeltas = new
            BlackboardData<Map<String, PolarCoordinates>>(new HashMap<String, PolarCoordinates>());

    @Override
    public BlackboardData<Map<String, PolarCoordinates>> othersDeltas() {
        return othersDeltas;
    }

    private final BlackboardData<Map<String, Location>> othersLocations = new
            BlackboardData<Map<String, Location>>(new HashMap<String, Location>());

    @Override
    public BlackboardData<Map<String, Location>> othersLocations() {
        return othersLocations;
    }

    private final BlackboardData<Set<String>> othersNames = new
            BlackboardData<Set<String>>(new HashSet<String>());

    @Override
    public BlackboardData<Set<String>> othersNames() {
        return othersNames;
    }

    private final BlackboardData<Location> userLocation = new BlackboardData<>(null);

    @Override
    public BlackboardData<Location> userLocation() {
        return userLocation;
    }

    private final BlackboardData<String> userName = new BlackboardData<>("");

    @Override
    public BlackboardData<String> userName() {
        return userName;
    }

    private final BlackboardData<Float> userOrientation = new BlackboardData<>(0f);

    @Override
    public BlackboardData<Float> userOrientation() {
        return userOrientation;
    }

}

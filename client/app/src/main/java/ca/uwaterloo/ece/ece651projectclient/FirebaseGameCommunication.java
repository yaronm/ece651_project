package ca.uwaterloo.ece.ece651projectclient;

import android.location.Location;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * A class that handles automatic configuration of communication with the firebase server during
 * gameplay. Responsible for enabling and disabling synchronizing of gameplay data with the server.
 */
public class FirebaseGameCommunication {

    private static final String TAG = "FireGameComm";

    /**
     * Initializes a firebase game communication instance and attaches it to the given blackboard.
     *
     * @param blackboard a blackboard
     */
    public FirebaseGameCommunication(Blackboard blackboard) {
        // store the blackboard
        this.blackboard = blackboard;
        // get access to the firebase database
        database = FirebaseDatabase.getInstance().getReference();
        // observe the blackboard for when the game state is set
        blackboard.gameState().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                gameStateSet();
            }
        });
    }

    private Blackboard blackboard;
    private DatabaseReference database;

    /**
     * Handles when the game state is set on the blackboard.
     */
    private void gameStateSet() {
        // get the new game state
        GameState gameState = blackboard.gameState().value();
        // invoke the appropriate handler method
        switch (gameState) {
            case RUNNING:
                enableSynchronization();
                break;
            default:
                disableSynchronization();
                break;
        }
    }

    private ValueEventListener playersListener;
    private Map<String, ValueEventListener> locationListeners;
    private ValueEventListener visibilityListener;

    /**
     * Enables data synchronization from the firebase server. Requires that the userName and
     * currentGameId are set in the blackboard.
     *
     * @return whether synchronization enabling succeeded
     */
    boolean enableSynchronization() {
        // check that the user name is set
        final String userName = blackboard.userName().value();
        if (userName == null) {
            Log.d(TAG, "Could not enable game data synchronization: User name not set");
            return false;
        }

        // check that the current game id is set
        String currentGameId = blackboard.currentGameId().value();
        if (currentGameId == null) {
            Log.d(TAG, "Could not enable game data synchronization: Current game id not set");
            return false;
        }

        // start listening to changes to the game's players
        disableSynchronization();
        playersListener = database.child("games").child(currentGameId).child("players")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // check that received players set is non-null
                        Map<String, Object> players = dataSnapshot.getValue(
                                new GenericTypeIndicator<Map<String, Object>>() {
                                });
                        if (players == null) {
                            Log.d(TAG, "Received null list of players");
                            return;
                        }
                        // update the blackboard's othersNames to match the received players set,
                        // but with the current user's name left out
                        Set<String> othersNames = blackboard.othersNames().value();
                        othersNames.clear();
                        othersNames.addAll(players.keySet());
                        othersNames.remove(userName);
                        blackboard.othersNames().set(othersNames);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
        // start listening to changes to the game's visibility matrix
        visibilityListener = database.child("games").child(currentGameId).child("visibility")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // check that received visibility matrix is non-null
                        Map<String, Object> visibility = dataSnapshot.getValue(
                                new GenericTypeIndicator<Map<String, Object>>() {
                                });
                        if (visibility == null) {
                            Log.d(TAG, "Received null visibility matrix");
                            return;
                        }
                        // update the blackboard's visibility matrix to match
                        blackboard.visibilityMatrix().set(VisibilityMatrix
                                .fromFirebaseSerializableMap(visibility));
                        // re-configure location listeners to the updated visibilities
                        enableLocationSynchronization();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
        return true;
    }

    /**
     * Enables location synchronization from the firebase server. Requires that the userName and
     * visibilityMatrix are set in the blackboard.
     *
     * @return whether location synchronization enabling succeeded
     */
    boolean enableLocationSynchronization() {
        // check that the user name is set
        final String userName = blackboard.userName().value();
        if (userName == null) {
            Log.d(TAG, "Could not enable location data synchronization: User name not set");
            return false;
        }

        // check that the visibility matrix is set
        VisibilityMatrix visibilityMatrix = blackboard.visibilityMatrix().value();
        if (visibilityMatrix == null) {
            Log.d(TAG, "Could not enable location data synchronization: Visibility matrix not set");
            return false;
        }

        // get the set of players that are visible to the user
        Set<String> visiblePlayers = visibilityMatrix.asMap().get(userName);
        if (visiblePlayers == null) {
            return true;
        }
        // start listening to changes to visible players' locations
        disableLocationSynchronization();
        locationListeners = new HashMap<>();
        for (final String player : visiblePlayers) {
            ValueEventListener listener = database.child("players").child(player).child("location")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // check that the received player location data is non-null
                            Map<String, Double> location = dataSnapshot.getValue(
                                    new GenericTypeIndicator<Map<String, Double>>() {});
                            if (location != null) {
                                // update the blackboard's location data for this player to match
                                Map<String, Location> othersLocations =
                                        blackboard.othersLocations().value();
                                othersLocations.put(player, FirebaseUtils
                                        .deserializeFirebaseLocation(location));
                                blackboard.othersLocations().set(othersLocations);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
            locationListeners.put(player, listener);
        }
        return true;
    }

    /**
     * Disables data synchronization from the firebase server.
     */
    void disableSynchronization() {
        if (playersListener != null) {
            database.child("games").child("players").removeEventListener(playersListener);
            playersListener = null;
        }
        if (visibilityListener != null) {
            database.child("games").child("visibility").removeEventListener(visibilityListener);
            visibilityListener = null;
        }
        disableLocationSynchronization();
    }

    /**
     * Disables location data synchronization from the firebase server.
     */
    void disableLocationSynchronization() {
        // disable location synchronization from server
        if (locationListeners != null) {
            for (Map.Entry<String, ValueEventListener> entry : locationListeners.entrySet()) {
                database.child("players").child(entry.getKey()).child("location")
                        .removeEventListener(entry.getValue());
            }
            locationListeners = null;
        }
        // clear cached blackboard locations
        blackboard.othersLocations().set(new HashMap<String, Location>());
    }

}

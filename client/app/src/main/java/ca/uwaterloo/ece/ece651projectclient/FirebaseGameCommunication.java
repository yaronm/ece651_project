package ca.uwaterloo.ece.ece651projectclient;

import android.location.Location;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
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

    private Observer locationObserver;
    private Observer taggedObserver;

    /**
     * Enables data synchronization to and from the firebase server. Requires that the userName and
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
        final String currentGameId = blackboard.currentGameId().value();
        if (currentGameId == null) {
            Log.d(TAG, "Could not enable game data synchronization: Current game id not set");
            return false;
        }

        // start observing whether the user has been tagged for upload to the server
        taggedObserver = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                // check the tagger is non-null
                final String tagger = blackboard.userTaggedBy().value();
                if (tagger == null) {
                    Log.d(TAG, "Could not report tag: null tagger");
                    return;
                }

                // update the game atomically to set the user as tagged out
                BlockingTransactionHandler handler = new BlockingTransactionHandler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        // check that received visibility matrix is non-null
                        Map<String, Object> visibility = mutableData.child("visibility").getValue(
                                new GenericTypeIndicator<Map<String, Object>>() {});
                        if (visibility == null) {
                            Log.d(TAG, "Could not report tag: null visibility matrix");
                            return Transaction.abort();
                        }
                        // assign the tagged player's target(s) to the tagger
                        VisibilityMatrix visibilityMatrix = VisibilityMatrix
                                .fromFirebaseSerializableMap(visibility);
                        visibilityMatrix.transferTargets(userName, tagger);
                        mutableData.child("visibility").setValue(
                                visibilityMatrix.asFirebaseSerializableMap());
                        // apply the transaction
                        return Transaction.success(mutableData);
                    }
                };
                // invoke the tagging transaction only while the server game data is cached in the
                // local firebase client by an outstanding listener; a necessary workaround to
                // avoid the tagging transaction getting applied to a local null value
                // see: https://groups.google.com/forum/#!topic/firebase-talk/tyj-5G6Fzgs
                BlockingValueEventListener listener = new BlockingValueEventListener() {};
                database.child("games").child(currentGameId).addValueEventListener(listener);
                listener.getSnapshot();
                database.child("games").child(currentGameId).runTransaction(handler);
                database.child("games").child(currentGameId).removeEventListener(listener);
                // wait and check that the tag was successful
                if (!handler.isCommitted()) {
                    Log.d(TAG, "Could not report tag: tag rejected by server");
                    return;
                }
                // finish tag by recording that the user was tagged
                database.child("games").child(currentGameId).child("out").child(userName)
                        .setValue(true);
                // and set the user to the OUT game state
                blackboard.gameState().set(GameState.OUT);
            }
        };
        blackboard.userTaggedBy().addObserver(taggedObserver);
        // start listening to changes to the game's players
        disableSynchronization();
        playersListener = database.child("games").child(currentGameId).child("players")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // check that received players set is non-null
                        Map<String, Object> players = dataSnapshot.getValue(
                                new GenericTypeIndicator<Map<String, Object>>() {});
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
                                new GenericTypeIndicator<Map<String, Object>>() {});
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
     * Enables location synchronization to and from the firebase server. Requires that the userName
     * and visibilityMatrix are set in the blackboard.
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

        // start observing user location changes for upload to the server
        locationObserver = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                database.child("users").child(userName).child("location").setValue(
                        FirebaseUtils.serializeFirebaseLocation(blackboard.userLocation().value()));
            }
        };
        blackboard.userLocation().addObserver(locationObserver);
        // get the set of players that are visible to the user
        Set<String> visiblePlayers = visibilityMatrix.asMap().get(userName);
        if (visiblePlayers == null) {
            return true;
        }
        // start listening to changes to visible players' locations
        disableLocationSynchronization();
        locationListeners = new HashMap<>();
        for (final String player : visiblePlayers) {
            ValueEventListener listener = database.child("users").child(player).child("location")
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
     * Disables data synchronization to and from the firebase server.
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
     * Disables location data synchronization to and from the firebase server.
     */
    void disableLocationSynchronization() {
        // disable location synchronization to server
        if (locationObserver != null) {
            blackboard.userLocation().deleteObserver(locationObserver);
            locationObserver = null;
        }
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

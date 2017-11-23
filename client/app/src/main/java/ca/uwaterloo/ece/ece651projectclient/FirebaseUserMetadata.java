package ca.uwaterloo.ece.ece651projectclient;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * A class that handles communication of user metadata, including the list of currently active
 * games, with the firebase server.
 */
public class FirebaseUserMetadata {

    private static final String TAG = "FBUserMeta";

    /**
     * Initializes a firebase user metadata communication instance and attaches it to the given
     * blackboard.
     *
     * @param blackboard a blackboard
     */
    public FirebaseUserMetadata(Blackboard blackboard, DatabaseReference database) {
        // store the blackboard
        this.blackboard = blackboard;
        // get access to the firebase database
        this.database = database;
        // observe the blackboard for when the game state is set
        Observer observer = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                // get the username
                String userName = FirebaseUserMetadata.this.blackboard.userName().value();
                // get the game state
                GameState gameState = FirebaseUserMetadata.this.blackboard.gameState().value();
                // invoke the appropriate handler method
                if (userName != null && gameState == GameState.UNINITIALIZED) {
                    enableSynchronization();
                } else {
                    disableSynchronization();
                }
            }
        };
        blackboard.userName().addObserver(observer);
        blackboard.gameState().addObserver(observer);
    }

    private Blackboard blackboard;
    private DatabaseReference database;

    private ValueEventListener gamesListener;

    /**
     * Enables user metadata synchronization from the firebase server. Requires that the userName
     * is set in the blackboard.
     */
    void enableSynchronization() {
        // check that the user name is set
        final String userName = blackboard.userName().value();
        if (userName == null) {
            Log.d(TAG, "Could not enable user metadata synchronization: user name is null");
            return;
        }

        // start observing the list of games for available games to join
        disableSynchronization();
        gamesListener = database.child("games").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get the list of games
                Map<String, Map<String, Object>> games = dataSnapshot.getValue(
                        new GenericTypeIndicator<Map<String, Map<String, Object>>>() {});
                if (games == null) {
                    return;
                }

                // search through all games
                Set<String> availableGames = new HashSet<>();
                for (Map.Entry<String, Map<String, Object>> game : games.entrySet()) {
                    // get the game's players
                    Map<String, Object> players =
                            (Map<String, Object>) game.getValue().get("players");
                    // if the user is already a player, add the game to the set of available games
                    if (players.containsKey(userName)) {
                        availableGames.add(game.getKey());
                        continue;
                    }
                    // get the game's number of players
                    long numberOfPlayers = (long) game.getValue().get("numberOfPlayers");
                    // if the game is open, add the game to the set of available games
                    if (players.size() != numberOfPlayers) {
                        availableGames.add(game.getKey());
                    }
                }
                blackboard.availableGames().set(availableGames);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    /**
     * Disables user metadata synchronization from the firebase server.
     */
    void disableSynchronization() {
        if (gamesListener != null) {
            database.child("games").removeEventListener(gamesListener);
            gamesListener = null;
        }
    }

}

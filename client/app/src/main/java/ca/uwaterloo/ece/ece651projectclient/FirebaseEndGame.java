package ca.uwaterloo.ece.ece651projectclient;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * A class that handles endgame detection and response.
 */
public class FirebaseEndGame {

    private static final String TAG = "FBEndGame";

    /**
     * Initializes a firebase endgame communication instance and attaches it to the given
     * blackboard.
     *
     * @param blackboard a blackboard
     */
    public FirebaseEndGame(Blackboard blackboard, DatabaseReference database) {
        // store the blackboard
        this.blackboard = blackboard;
        // get access to the firebase database
        this.database = database;
        // observe the blackboard for when the game state is set
        blackboard.gameState().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                // get the new game state
                GameState gameState = FirebaseEndGame.this.blackboard.gameState().value();
                // invoke the appropriate handler method
                switch (gameState) {
                    case RUNNING:
                    case PAUSED:
                    case OUT:
                        enableEndgameListener();
                        break;
                    default:
                        disableEndgameListener();
                        break;
                }
            }
        });
    }

    private Blackboard blackboard;
    private DatabaseReference database;

    private ValueEventListener visibilityListener;

    /**
     * Enables listening for the end of the game.
     */
    void enableEndgameListener() {
        // check that the current game id is set
        final String currentGameId = blackboard.currentGameId().value();
        if (currentGameId == null) {
            Log.d(TAG, "Could not enable game data synchronization: Current game id not set");
            return;
        }

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
                        // if the game has ended, update the game state
                        VisibilityMatrix visibilityMatrix =
                                VisibilityMatrix.fromFirebaseSerializableMap(visibility);
                        Log.d(TAG, visibility.toString());
                        Log.d(TAG, visibilityMatrix.getPlayers().toString());
                        if (visibilityMatrix.getOut().size() ==
                                blackboard.othersNames().value().size()) {
                            blackboard.gameState().set(GameState.ENDED);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }

    /**
     * Disables listening for the end of the game.
     */
    void disableEndgameListener() {
        if (visibilityListener != null) {
            database.child("games").child("visibility").removeEventListener(visibilityListener);
            visibilityListener = null;
        }
    }

}

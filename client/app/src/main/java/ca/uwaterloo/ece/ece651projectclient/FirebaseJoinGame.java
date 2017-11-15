package ca.uwaterloo.ece.ece651projectclient;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * A class for handling firebase communication during the game joining process.
 */
public class FirebaseJoinGame {

    private static final String TAG = "FBJoinGame";

    private static final GenericTypeIndicator<Map<String, Object>> MAP_TYPE = new
            GenericTypeIndicator<Map<String, Object>>() {};

    /**
     * Initializes a FirebaseJoinGame instance and attaches it to the given blackboard.
     *
     * @param blackboard a blackboard
     * @param database   a firebase database reference
     */
    public FirebaseJoinGame(Blackboard blackboard, DatabaseReference database) {
        // store the blackboard and database reference
        this.blackboard = blackboard;
        this.database = database;
        // observe the blackboard for when the game state changes to JOINING
        blackboard.gameState().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                if (FirebaseJoinGame.this.blackboard.gameState().value() == GameState.JOINING) {
                    joinGame();
                }
            }
        });
    }

    private Blackboard blackboard;
    private DatabaseReference database;

    /**
     * Joins an already existing game. The currentGameId on the blackboard must be set to the game
     * id of the game that is intended to be joined. Joining may be rejected if the game is full.
     * On success, sets the gameState to RUNNING. On failure, sets the gameState to UNINITIALIZED.
     */
    private void joinGame() {
        // check that the user name is set
        final String userName = blackboard.userName().value();
        if (userName == null) {
            Log.d(TAG, "Could not join game: User name not set");
            blackboard.gameState().set(GameState.UNINITIALIZED);
            return;
        }

        // check that the current game id is set
        final String currentGameId = blackboard.currentGameId().value();
        if (currentGameId == null) {
            Log.d(TAG, "Could not join game: Current game id not set");
            blackboard.gameState().set(GameState.UNINITIALIZED);
            return;
        }

        // join the game atomically via a transaction
        final Transaction.Handler handler = new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                // verify that the game exists
                if (mutableData.getValue() == null) {
                    return Transaction.abort();
                }

                // retrieve player data
                Map<String, Object> players = mutableData.child("players").getValue(MAP_TYPE);
                long numberOfPlayers = mutableData.child("numberOfPlayers").getValue(Long.class);
                // if the user is already a player in the game, do nothing
                if (players.containsKey(userName)) {
                    return Transaction.success(mutableData);
                }

                // if the user is not already a player in the game, add the user
                else if (players.size() < numberOfPlayers) {
                    players.put(userName, true);
                    mutableData.child("players").setValue(players);
                    VisibilityMatrix visibility = VisibilityMatrix.fromFirebaseSerializableMap(
                            mutableData.child("visibility").getValue(MAP_TYPE));
                    visibility.assignPlayer(userName);
                    mutableData.child("visibility").setValue(
                            visibility.asFirebaseSerializableMap());
                    return Transaction.success(mutableData);
                }

                // otherwise, there's no room in the game for the player
                return Transaction.abort();
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean completed,
                                   DataSnapshot dataSnapshot) {
                // check that the game was joined successfully
                if (!completed) {
                    Log.d(TAG, "Could not join game: Joining rejected by server");
                    blackboard.gameState().set(GameState.UNINITIALIZED);
                    return;
                }

                // finally, wait for the game to become ready to play
                waitToRunGame(currentGameId);
            }
        };
        // invoke the game joining transaction only while the server game data is cached in the
        // local firebase client by an outstanding listener; a necessary workaround to avoid the
        // game joining transaction getting applied to a local null value
        // see: https://groups.google.com/forum/#!topic/firebase-talk/tyj-5G6Fzgs
        final DatabaseReference game = database.child("games").child(currentGameId);
        final ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                game.runTransaction(handler);
                game.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        game.addValueEventListener(listener);
    }

    /**
     * Schedules the game to move to a RUNNING state when the required number of players join the
     * current game. Changing currentGameId between the scheduling event and the point at which
     * the game becomes ready to play cancels this scheduled game state transition.
     */
    private void waitToRunGame(String currentGameId) {
        // wait until the required number of players have joined
        final DatabaseReference game = database.child("games").child(currentGameId);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // retrieve game data
                String gameId = dataSnapshot.getKey();
                Map<String, Object> players = dataSnapshot.child("players").getValue(MAP_TYPE);
                long numberOfPlayers = dataSnapshot.child("numberOfPlayers").getValue(Long.class);

                // check that the game we are waiting to play is still the current game
                if (gameId != blackboard.currentGameId().value()) {
                    game.removeEventListener(this);
                    blackboard.gameState().set(GameState.UNINITIALIZED);
                }

                // if the required number of players have joined, start the game running
                else if (players.size() == numberOfPlayers) {
                    game.removeEventListener(this);
                    blackboard.gameState().set(GameState.RUNNING);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        game.addValueEventListener(listener);
    }

}

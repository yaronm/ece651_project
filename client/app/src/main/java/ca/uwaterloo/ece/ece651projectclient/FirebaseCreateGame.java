package ca.uwaterloo.ece.ece651projectclient;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * A class for handling firebase communication during the game creation process.
 */
public class FirebaseCreateGame {

    private static final String TAG = "FBCreateGame";

    /**
     * Initializes a FirebaseCreateGame instance and attaches it to the given blackboard.
     *
     * @param blackboard a blackboard
     * @param database   a firebase database reference
     */
    public FirebaseCreateGame(Blackboard blackboard, DatabaseReference database) {
        // store the blackboard and database reference
        this.blackboard = blackboard;
        this.database = database;
        // observe the blackboard for when the game state changes to CREATING
        blackboard.gameState().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                if (FirebaseCreateGame.this.blackboard.gameState().value() == GameState.CREATING) {
                    createGame();
                }
            }
        });
    }

    private Blackboard blackboard;
    private DatabaseReference database;

    /**
     * Creates a new game using available data from the blackboard. The blackboard must (1) provide
     * at least one opponent via othersNames or have the numberOfPlayers set to at least 2 and (2)
     * provide either a non-CUSTOM visibility matrix type or a pre-defined custom visibility matrix.
     * On success, sets the currentGameId to contain the unique game identifier of the new game and
     * sets the gameState to JOINING. On failure, sets the gameState to UNINITIALIZED.
     */
    private void createGame() {
        // check that the user name is set
        String userName = blackboard.userName().value();
        if (userName == null) {
            Log.d(TAG, "Could not create game: User name not set");
            blackboard.gameState().set(GameState.UNINITIALIZED);
            return;
        }

        // get the other participating players' names
        final Set<String> players = blackboard.othersNames().value();
        // add the user
        players.add(userName);

        // get the number of players to be in the game
        int numberOfPlayers = blackboard.numberOfPlayers().value();
        // if there are already sufficient named players in the game, update accordingly
        if (players.size() > numberOfPlayers) {
            numberOfPlayers = players.size();
        }

        // get the visibility matrix type
        VisibilityMatrixType visibilityMatrixType = blackboard.visibilityMatrixType().value();
        // get the visibility matrix
        VisibilityMatrix visibilityMatrix = blackboard.visibilityMatrix().value();

        // check that there are at least 2 players
        if (numberOfPlayers < 2) {
            Log.d(TAG, "Could not create game: Not enough players");
            blackboard.gameState().set(GameState.UNINITIALIZED);
            return;
        }

        // if the visibility matrix is provided, check that it is valid
        if (visibilityMatrixType == VisibilityMatrixType.CUSTOM) {
            if (visibilityMatrix == null) {
                Log.d(TAG, "Could not create game: null visibility matrix");
                blackboard.gameState().set(GameState.UNINITIALIZED);
                return;
            } else if (!visibilityMatrix.isValid(numberOfPlayers)) {
                Log.d(TAG, "Could not create game: Invalid visibility matrix");
                blackboard.gameState().set(GameState.UNINITIALIZED);
                return;
            }
        }
        // otherwise, generate a visibility matrix
        else {
            visibilityMatrix = new VisibilityMatrix(visibilityMatrixType, numberOfPlayers);
        }
        // populate the visibility matrix with the players that are already in the game
        for (String player : players) {
            visibilityMatrix.assignPlayer(player);
        }

        // finally, create the game atomically via a transaction
        final Map<String, Boolean> finalPlayers = FirebaseUtils.setToMap(players, true);
        final int finalNumberOfPlayers = numberOfPlayers;
        final Map<String, Object> finalVisibilityMatrix =
                visibilityMatrix.asFirebaseSerializableMap();
        final DatabaseReference newGame = database.child("games").push();
        newGame.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                // if the game does not already exist, create the game
                if (mutableData.getValue() == null) {
                    Map<String, Object> game = new HashMap<>();
                    game.put("players", finalPlayers);
                    game.put("numberOfPlayers", finalNumberOfPlayers);
                    game.put("visibility", finalVisibilityMatrix);
                    mutableData.setValue(game);
                    return Transaction.success(mutableData);
                }

                // otherwise, do not attempt to re-create the game
                return Transaction.abort();
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot
                    dataSnapshot) {
                // check that the new game was created successfully
                if (!committed) {
                    Log.d(TAG, "Could not create game: Game rejected by server");
                    blackboard.gameState().set(GameState.UNINITIALIZED);
                    return;
                }

                // finish creation by linking players to the game
                String newGameId = newGame.getKey();
                for (String player : players) {
                    database.child("users").child(player).child("games").child(newGameId)
                            .setValue(true);
                }
                // store the new game's id as the currentGameId on the blackboard
                blackboard.currentGameId().set(newGameId);
                // finally, set the game state to JOINING
                blackboard.gameState().set(GameState.JOINING);
            }
        });
    }

}

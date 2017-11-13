package ca.uwaterloo.ece.ece651projectclient;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class FirebaseCommunication {

    private static final String TAG = "FirebaseComm";

    /**
     * Initializes a firebase communication instance and attaches it to the given blackboard.
     *
     * @param blackboard a blackboard
     */
    public FirebaseCommunication(Blackboard blackboard) {
        // store the blackboard
        this.blackboard = blackboard;
        // get access to the firebase database
        database = FirebaseDatabase.getInstance().getReference();
        // observe the blackboard for when the user name is set
        blackboard.userName().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                userNameSet();
            }
        });
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
     * Handles when the user name is set on the blackboard. If the user name is not already
     * associated with a user, creates a new user on the server. If the user name is null, this
     * method does nothing.
     */
    void userNameSet() {
        // get the new user name
        String userName = blackboard.userName().value();
        // check that it is non-null
        if (userName == null) {
            return;
        }
        // if the user does not exist, add the user atomically via a transaction
        database.child("users").child(userName).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Object currentData = mutableData.getValue();
                // if the user does not already exist, add the user
                if (currentData == null) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("thisIsAPlaceHolder", true);
                    mutableData.setValue(user);
                    return Transaction.success(mutableData);
                }
                // otherwise, do not attempt to re-add the user
                return Transaction.abort();
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed,
                                   DataSnapshot dataSnapshot) {}
        });
    }

    /**
     * Handles when the game state is set on the blackboard. If the game state is null, this
     * method does nothing.
     */
    void gameStateSet() {
        // get the new game state
        GameState gameState = blackboard.gameState().value();
        // check that it is non-null
        if (gameState == null) {
            return;
        }
        // invoke the appropriate handler method
        switch (gameState) {
            case CREATING:
                createGame();
                break;
        }
    }

    /**
     * Creates a new game using available data from the blackboard. The blackboard must (1) provide
     * at least one opponent via othersNames or have the numberOfPlayers set to at least 2 and (2)
     * provide either a non-CUSTOM visibility matrix type or a pre-defined custom visibility matrix.
     * If creation is successful, sets the game state to JOINING. If available data is insufficient
     * or if the creation failsfor any additional reason, returns the game state to UNINITIALIZED.
     */
    void createGame() {
        // check that the user name is set
        String userName = blackboard.userName().value();
        if (userName == null) {
            Log.d(TAG, "Could not create game: User name not set");
            blackboard.gameState().set(GameState.UNINITIALIZED);
            return;
        }

        // get the other participating players' names
        Set<String> players = blackboard.othersNames().value();
        // check that it is non-null
        if (players == null) {
            players = new HashSet<>();
        }
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
        if (visibilityMatrixType == VisibilityMatrixType.CUSTOM &&
                !visibilityMatrix.isValid(numberOfPlayers)) {
            Log.d(TAG, "Could not create game: Invalid visibility matrix");
            blackboard.gameState().set(GameState.UNINITIALIZED);
            return;
        }
        // otherwise, generate a visibility matrix
        else {
            visibilityMatrix = new VisibilityMatrix(visibilityMatrixType, numberOfPlayers);
        }

        // finally, create the game atomically via a transaction
        final List<String> finalOthersNames = new LinkedList<>(players);
        final int finalNumberOfPlayers = numberOfPlayers;
        final Map<String, List<String>> finalVisibilityMatrix = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : visibilityMatrix.asMap().entrySet()) {
            finalVisibilityMatrix.put(entry.getKey(), new LinkedList<String>(entry.getValue()));
        }
        DatabaseReference newGame = database.child("games").push();
        BlockingTransactionHandler handler = new BlockingTransactionHandler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Object currentData = mutableData.getValue();
                // if the game does not already exist, create the game
                if (currentData == null) {
                    Map<String, Object> game = new HashMap<>();
                    game.put("players", finalOthersNames);
                    game.put("numberOfPlayers", finalNumberOfPlayers);
                    game.put("visibilityMatrix", finalVisibilityMatrix);
                    mutableData.setValue(game);
                    return Transaction.success(mutableData);
                }
                // otherwise, do not attempt to re-create the game
                return Transaction.abort();
            }
        };
        newGame.runTransaction(handler);
        // wait and check that the new game is created successfully
        if (!handler.isCommitted()) {
            Log.d(TAG, "Could not create game: Game rejected by server");
            blackboard.gameState().set(GameState.UNINITIALIZED);
            return;
        }

        // if creation succeeded, join the new game
        String newGameId = newGame.getKey();
        blackboard.currentGameId().set(newGameId);
        blackboard.gameState().set(GameState.JOINING);
    }

}

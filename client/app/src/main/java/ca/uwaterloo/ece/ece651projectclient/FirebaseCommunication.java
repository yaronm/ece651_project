package ca.uwaterloo.ece.ece651projectclient;

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
import java.util.HashSet;
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
    private void userNameSet() {
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
    private void gameStateSet() {
        // get the new game state
        GameState gameState = blackboard.gameState().value();
        // check that it is non-null
        if (gameState == null) {
            return;
        }
        // invoke the appropriate handler method
        switch (gameState) {
            case CREATING:
                // attempt to create a game
                if (createGame()) {
                    // if game is created successfully, attempt to join it
                    blackboard.gameState().set(GameState.JOINING);
                } else {
                    // otherwise, reset game state to UNINITIALIZED
                    blackboard.gameState().set(GameState.UNINITIALIZED);
                }
                break;
            case JOINING:
                // attempt to join a game
                if (joinGame()) {
                    // if game is joined successfully, wait until it is ready to be played
                    waitToRunGame();
                } else {
                    // otherwise, reset game state to UNINITIALIZED
                    blackboard.gameState().set(GameState.UNINITIALIZED);
                }
                break;
        }
    }

    /**
     * Creates a new game using available data from the blackboard. The blackboard must (1) provide
     * at least one opponent via othersNames or have the numberOfPlayers set to at least 2 and (2)
     * provide either a non-CUSTOM visibility matrix type or a pre-defined custom visibility matrix.
     * On success, sets the currentGameId to contain the unique game identifier of the new game.
     * Returns whether creation is successful.
     */
    boolean createGame() {
        // check that the user name is set
        String userName = blackboard.userName().value();
        if (userName == null) {
            Log.d(TAG, "Could not create game: User name not set");
            return false;
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
            return false;
        }

        // if the visibility matrix is provided, check that it is valid
        if (visibilityMatrixType == VisibilityMatrixType.CUSTOM &&
                !visibilityMatrix.isValid(numberOfPlayers)) {
            Log.d(TAG, "Could not create game: Invalid visibility matrix");
            return false;
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
        DatabaseReference newGame = database.child("games").push();
        BlockingTransactionHandler handler = new BlockingTransactionHandler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Object currentData = mutableData.getValue();
                // if the game does not already exist, create the game
                if (currentData == null) {
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
        };
        newGame.runTransaction(handler);
        // wait and check that the new game is created successfully
        if (!handler.isCommitted()) {
            Log.d(TAG, "Could not create game: Game rejected by server");
            return false;
        }
        // finish creation by linking players to the game
        String newGameId = newGame.getKey();
        for (String player : players) {
            database.child("users").child(player).child("games").child(newGameId).setValue(true);
        }

        // on successful creation, store the new game's id as the currentGameId on the blackboard
        blackboard.currentGameId().set(newGameId);
        return true;
    }

    /**
     * Joins an already existing game. The currentGameId on the blackboard must be set to the game
     * id of the game that is intended to be joined. Joining may be rejected if the game is full.
     * Returns whether joining succeeded.
     */
    boolean joinGame() {
        // check that the user name is set
        final String userName = blackboard.userName().value();
        if (userName == null) {
            Log.d(TAG, "Could not join game: User name not set");
            return false;
        }

        // check that the current game id is set
        String currentGameId = blackboard.currentGameId().value();
        if (currentGameId == null) {
            Log.d(TAG, "Could not join game: Current game id not set");
            return false;
        }

        // join the game atomically via a transaction
        BlockingTransactionHandler handler = new BlockingTransactionHandler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                // verify that the game exists
                Object currentData = mutableData.getValue();
                if (currentData == null) {
                    return Transaction.abort();
                }
                // retrieve player data
                Map<String, Object> players = mutableData.child("players").getValue(
                        new GenericTypeIndicator<Map<String, Object>>() {});
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
                            mutableData.child("visibility").getValue(
                                    new GenericTypeIndicator<Map<String, Object>>() {
                                    }));
                    Log.d(TAG, visibility.asFirebaseSerializableMap().toString());
                    visibility.assignPlayer(userName);
                    Log.d(TAG, visibility.asFirebaseSerializableMap().toString());
                    mutableData.child("visibility").setValue(
                            visibility.asFirebaseSerializableMap());
                    return Transaction.success(mutableData);
                }
                // otherwise, there's no room in the game for the player
                return Transaction.abort();
            }
        };
        // invoke the game joining transaction only while the server game data is cached in the
        // local firebase client by an outstanding listener; a necessary workaround to avoid the
        // game joining transaction getting applied to a local null value
        // see: https://groups.google.com/forum/#!topic/firebase-talk/tyj-5G6Fzgs
        BlockingValueEventListener listener = new BlockingValueEventListener() {};
        database.child("games").child(currentGameId).addValueEventListener(listener);
        listener.getSnapshot();
        database.child("games").child(currentGameId).runTransaction(handler, false);
        database.child("games").child(currentGameId).removeEventListener(listener);
        // wait and check that the game has been joined successfully
        if (!handler.isCommitted()) {
            Log.d(TAG, "Could not join game: Joining rejected by server");
            return false;
        }
        return true;
    }

    /**
     * Schedules the game to move to a RUNNING state when the required number of players join the
     * current game. Changing currentGameId between the scheduling event and the point at which
     * the game becomes ready to play cancels this scheduled game state transition. Returns whether
     * scheduling was successful.
     */
    boolean waitToRunGame() {
        // check that the current game id is set
        String currentGameId = blackboard.currentGameId().value();
        if (currentGameId == null) {
            Log.d(TAG, "Error while waiting to run game: Current game id not set");
            return false;
        }

        // wait until the required number of players have joined
        final DatabaseReference game = database.child("games").child(currentGameId);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // retrieve game data
                String gameId = dataSnapshot.getKey();
                Map<String, Boolean> players = dataSnapshot.child("players").getValue(
                        new GenericTypeIndicator<Map<String, Boolean>>() {});
                long numberOfPlayers = dataSnapshot.child("numberOfPlayers").getValue(Long.class);
                // check that the game we are waiting to play is still the current game
                if (gameId != blackboard.currentGameId().value()) {
                    game.removeEventListener(this);
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
        return true;
    }

}

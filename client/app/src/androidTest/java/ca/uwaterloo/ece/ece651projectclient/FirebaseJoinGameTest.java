package ca.uwaterloo.ece.ece651projectclient;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link FirebaseJoinGame}.
 */
public class FirebaseJoinGameTest {

    private static final int WAIT = 3000;

    private static final String USER_A = "userA";
    private static final String USER_B = "userB";
    private static final String USER_C = "userC";

    @BeforeClass
    public static void setUpClass() {
        // initialize the firebase client
        database = FirebaseDatabase.getInstance().getReference().child("FirebaseJoinGame").push();
    }

    static DatabaseReference database;

    @Before
    public void setUp() {
        // initialize the blackboard
        blackboard = new ConcreteBlackboard();
        // initialize the firebase create game module
        createGame = new FirebaseJoinGame(blackboard, database);
    }

    Blackboard blackboard;
    FirebaseJoinGame createGame;

    /**
     * Creates a game on the server.
     *
     * @param open whether the game should be open
     * @return the created game's id
     */
    private String createGame(boolean open) throws InterruptedException {
        Blackboard tempBlackboard = new ConcreteBlackboard();
        FirebaseCreateGame tempCreateGame = new FirebaseCreateGame(tempBlackboard, database);
        tempBlackboard.userName().set(USER_A);
        if (open) {
            tempBlackboard.numberOfPlayers().set(2);
        } else {
            tempBlackboard.othersNames().set(new HashSet<String>(Arrays.asList(USER_B)));
        }
        tempBlackboard.gameState().set(GameState.CREATING);
        Thread.sleep(WAIT);
        assertEquals(GameState.JOINING, tempBlackboard.gameState().value());
        return tempBlackboard.currentGameId().value();
    }

    @Test
    public void testGameJoinNoUserName() throws InterruptedException {
        // set the username to null
        blackboard.userName().set(null);
        // check that game joining is rejected
        blackboard.gameState().set(GameState.JOINING);
        Thread.sleep(WAIT);
        assertEquals(GameState.UNINITIALIZED, blackboard.gameState().value());
    }

    @Test
    public void testGameJoinNoGameId() throws InterruptedException {
        // set username to a returning user
        blackboard.userName().set(USER_A);
        // set the current game id to null
        blackboard.currentGameId().set(null);
        // check that game joining is rejected
        blackboard.gameState().set(GameState.JOINING);
        Thread.sleep(WAIT);
        assertEquals(GameState.UNINITIALIZED, blackboard.gameState().value());
    }

    @Test
    public void testGameJoinNoGame() throws InterruptedException {
        // set username to a returning user
        blackboard.userName().set(USER_A);
        // set the current game id to the id of a non-existent game
        blackboard.currentGameId().set("iDoNotExist");
        // check that game joining is rejected
        blackboard.gameState().set(GameState.JOINING);
        Thread.sleep(WAIT);
        assertEquals(GameState.UNINITIALIZED, blackboard.gameState().value());
    }

    @Test
    public void testGameJoinClosed() throws InterruptedException {
        // create a closed game
        String gameId = createGame(false);
        // set username to a playing user
        blackboard.userName().set(USER_B);
        // check that game joining is accepted
        blackboard.currentGameId().set(gameId);
        blackboard.gameState().set(GameState.JOINING);
        Thread.sleep(WAIT);
        assertEquals(GameState.RUNNING, blackboard.gameState().value());
    }

    @Test
    public void testGameJoinOpen() throws InterruptedException {
        // create an open game
        String gameId = createGame(true);
        // set username to a new user
        blackboard.userName().set(USER_B);
        // check that game joining is accepted
        blackboard.currentGameId().set(gameId);
        blackboard.gameState().set(GameState.JOINING);
        Thread.sleep(WAIT);
        assertEquals(GameState.RUNNING, blackboard.gameState().value());
    }

    @Test
    public void testGameJoinFull() throws InterruptedException {
        // create a closed game
        String gameId = createGame(false);
        // set username to a new user
        blackboard.userName().set(USER_C);
        // check that game joining is rejected
        blackboard.gameState().set(GameState.JOINING);
        Thread.sleep(WAIT);
        assertEquals(GameState.UNINITIALIZED, blackboard.gameState().value());
    }

    @Test
    public void testGameWaitChangeGameId() throws InterruptedException {
        // create an open game
        String gameId = createGame(true);
        // set username to a playing user
        blackboard.userName().set(USER_A);
        // join the game
        blackboard.currentGameId().set(gameId);
        blackboard.gameState().set(GameState.JOINING);
        // change the current game id
        String savedGameId = blackboard.currentGameId().value();
        blackboard.currentGameId().set("anotherGameId");
        Thread.sleep(1000);
        // check that the game joining is rejected
        assertEquals(GameState.UNINITIALIZED, blackboard.gameState().value());
    }

}

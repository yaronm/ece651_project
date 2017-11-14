package ca.uwaterloo.ece.ece651projectclient;

import android.support.test.runner.AndroidJUnit4;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * FirebaseCommunication unit testing.
 */
@RunWith(AndroidJUnit4.class)
public class FirebaseCommunicationTest {

    @Before
    public void setUp() {
        // initialize the firebase client
        database = FirebaseDatabase.getInstance().getReference();
        // initialize the blackboard
        blackboard = new ConcreteBlackboard();
        // initialize the firebase communication module
        communication = new FirebaseCommunication(blackboard);
        // use the firebase connection to setup test users
        blackboard.userName().set("userA");
        blackboard.userName().set("userB");
        blackboard.userName().set("userC");
    }

    DatabaseReference database;
    Blackboard blackboard;
    FirebaseCommunication communication;

    @Test
    public void testUserNull() {
        // test that the program does not crash on a null username
        blackboard.userName().set(null);
    }

    @Test
    public void testUserNew() {
        // get the current number of users
        BlockingValueEventListener listener = new BlockingValueEventListener();
        database.child("users").addListenerForSingleValueEvent(listener);
        long numberOfUsers = listener.getSnapshot().getChildrenCount();
        // set the username to a new user
        String username = "userD";
        blackboard.userName().set(username);
        listener = new BlockingValueEventListener();
        database.child("users").addListenerForSingleValueEvent(listener);
        // test that a new user exists
        assertEquals(numberOfUsers + 1, listener.getSnapshot().getChildrenCount());
        // test that the correct new user was added
        assertNotNull(listener.getSnapshot().child(username));
    }

    @Test
    public void testUserReturning() {
        // get the current number of users
        BlockingValueEventListener listener = new BlockingValueEventListener();
        database.child("users").addListenerForSingleValueEvent(listener);
        long numberOfUsers = listener.getSnapshot().getChildrenCount();
        // set the username to a returning user
        String username = "userA";
        blackboard.userName().set(username);
        listener = new BlockingValueEventListener();
        database.child("users").addListenerForSingleValueEvent(listener);
        // test that a no new users have been created
        assertEquals(numberOfUsers, listener.getSnapshot().getChildrenCount());
        // test that the returning user still exists
        assertNotNull(listener.getSnapshot().child(username));
    }

    @Test
    public void testGameCreateNoUserName() {
        // set the username to null
        blackboard.userName().set(null);
        // check that game creation is rejected
        assertFalse(communication.createGame());
    }

    @Test
    public void testGameCreateNotEnoughPlayers() {
        // set username to a returning user
        blackboard.userName().set("userA");
        // check that game creation is rejected
        assertFalse(communication.createGame());
    }

    @Test
    public void testGameCreateInvalidMatrix() {
        // set username to a returning user
        blackboard.userName().set("userA");
        // add another player
        Set<String> othersNames = blackboard.othersNames().value();
        othersNames.add("userB");
        blackboard.othersNames().set(othersNames);
        // create invalid visibility matrix
        blackboard.visibilityMatrixType().set(VisibilityMatrixType.CUSTOM);
        blackboard.visibilityMatrix().set(new
                VisibilityMatrix(VisibilityMatrixType.HIDE_N_SEEK, 3));
        // check that game creation is rejected
        assertFalse(communication.createGame());
    }

    @Test
    public void testGameCreateClosed() {
        // set username to a returning user
        blackboard.userName().set("userA");
        // add another player
        Set<String> othersNames = blackboard.othersNames().value();
        othersNames.add("userB");
        blackboard.othersNames().set(othersNames);
        // select visibility matrix type
        blackboard.visibilityMatrixType().set(VisibilityMatrixType.HIDE_N_SEEK);
        // check that game creation is accepted
        assertTrue(communication.createGame());
    }

    @Test
    public void testGameCreateOpen() {
        // set username to a returning user
        blackboard.userName().set("userA");
        // add another open player
        blackboard.numberOfPlayers().set(2);
        // select visibility matrix type
        blackboard.visibilityMatrixType().set(VisibilityMatrixType.HIDE_N_SEEK);
        // check that game creation is accepted
        assertTrue(communication.createGame());
    }

    @Test
    public void testGameJoinNoUserName() {
        // set the username to null
        blackboard.userName().set(null);
        // check that game joining is rejected
        assertFalse(communication.joinGame());
    }

    @Test
    public void testGameJoinNoGameId() {
        // set username to a returning user
        blackboard.userName().set("userA");
        // set the current game id to null
        blackboard.currentGameId().set(null);
        // check that game joining is rejected
        assertFalse(communication.joinGame());
    }

    @Test
    public void testGameJoinNoGame() {
        // set username to a returning user
        blackboard.userName().set("userA");
        // set the current game id to the id of a non-existent game
        blackboard.currentGameId().set("iDoNotExist");
        // check that game joining is rejected
        assertFalse(communication.joinGame());
    }

    @Test
    public void testGameJoinClosed() {
        // create a closed game
        testGameCreateClosed();
        // set username to a playing user
        blackboard.userName().set("userB");
        // check that game joining is accepted
        assertTrue(communication.joinGame());
    }

    @Test
    public void testGameJoinOpen() {
        // create an open game
        testGameCreateOpen();
        // set username to a new user
        blackboard.userName().set("userC");
        // check that game joining is accepted
        assertTrue(communication.joinGame());
    }

    @Test
    public void testGameJoinFull() {
        // create a closed game
        testGameCreateClosed();
        // set username to a new user
        blackboard.userName().set("userC");
        // check that game joining is rejected
        assertFalse(communication.joinGame());
    }

    @Test
    public void testGameWaitNoGameId() {
        // set the current game id to null
        blackboard.currentGameId().set(null);
        // check that game waiting is rejected
        assertFalse(communication.waitToRunGame());
    }

    @Test
    public void testGameWaitChangeGameId() {
        // create an open game
        testGameCreateOpen();
        // set username to a playing user
        blackboard.userName().set("userA");
        // join the game
        assertTrue(communication.joinGame());
        // store the game state
        GameState savedState = blackboard.gameState().value();
        // check that game waiting is accepted
        assertTrue(communication.waitToRunGame());
        assertEquals(savedState, blackboard.gameState().value());
        // change the current game id
        String savedGameId = blackboard.currentGameId().value();
        blackboard.currentGameId().set("anotherGameId");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // another user joins the game
        Blackboard blackboard2 = new ConcreteBlackboard();
        FirebaseCommunication communication2 = new FirebaseCommunication(blackboard2);
        blackboard2.userName().set("userB");
        blackboard2.currentGameId().set(savedGameId);
        assertTrue(communication2.joinGame());
        // check that the game state has not changed
        assertEquals(savedState, blackboard.gameState().value());
    }

    @Test
    public void testGameWait() {
        // create an open game
        testGameCreateOpen();
        // set username to a playing user
        blackboard.userName().set("userA");
        // join the game
        assertTrue(communication.joinGame());
        // store the game state
        GameState savedState = blackboard.gameState().value();
        // check that game waiting is accepted
        assertTrue(communication.waitToRunGame());
        assertEquals(savedState, blackboard.gameState().value());
        // another user joins the game
        Blackboard blackboard2 = new ConcreteBlackboard();
        FirebaseCommunication communication2 = new FirebaseCommunication(blackboard2);
        blackboard2.userName().set("userB");
        blackboard2.currentGameId().set(blackboard.currentGameId().value());
        assertTrue(communication2.joinGame());
        // check that the game state has changed to RUNNING
        assertNotEquals(savedState, blackboard.gameState().value());
        assertEquals(GameState.RUNNING, blackboard.gameState().value());
    }

}

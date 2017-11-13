package ca.uwaterloo.ece.ece651projectclient;

import android.support.test.runner.AndroidJUnit4;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        String username = "userC";
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
        // attempt to create game
        blackboard.gameState().set(GameState.CREATING);
        // check that game creation was rejected
        assertEquals(GameState.UNINITIALIZED, blackboard.gameState().value());
    }

    @Test
    public void testGameCreateNotEnoughPlayers() {
        // set username to a returning user
        blackboard.userName().set("userA");
        // attempt to create game
        blackboard.gameState().set(GameState.CREATING);
        // check that game creation was rejected
        assertEquals(GameState.UNINITIALIZED, blackboard.gameState().value());
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
        blackboard.visibilityMatrix().set(new
                VisibilityMatrix(VisibilityMatrixType.HIDE_N_SEEK, 3));
        // attempt to create game
        blackboard.gameState().set(GameState.CREATING);
        // check that game creation was rejected
        assertEquals(GameState.UNINITIALIZED, blackboard.gameState().value());
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
        // attempt to create game
        blackboard.gameState().set(GameState.CREATING);
        // check that game creation was accepted
        assertEquals(GameState.JOINING, blackboard.gameState().value());
    }

    @Test
    public void testGameCreateOpen() {
        // set username to a returning user
        blackboard.userName().set("userA");
        // add another open player
        blackboard.numberOfPlayers().set(2);
        // select visibility matrix type
        blackboard.visibilityMatrixType().set(VisibilityMatrixType.HIDE_N_SEEK);
        // attempt to create game
        blackboard.gameState().set(GameState.CREATING);
        // check that game creation was accepted
        assertEquals(GameState.JOINING, blackboard.gameState().value());
    }

}

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
        // use the firebase connection to setup a test user
        blackboard.userName().set("testuser");
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
        // set the username to a new user
        String username = "newuser";
        blackboard.userName().set(username);
        BlockingValueEventListener listener = new BlockingValueEventListener();
        database.child("users").addListenerForSingleValueEvent(listener);
        // test that a new user exists
        assertEquals(2, listener.getSnapshot().getChildrenCount());
        // test that the correct new user was added
        assertNotNull(listener.getSnapshot().child(username));
    }

    @Test
    public void testUserReturning() {
        // set the username to a returning user
        String username = "testuser";
        blackboard.userName().set(username);
        BlockingValueEventListener listener = new BlockingValueEventListener();
        database.child("users").addListenerForSingleValueEvent(listener);
        // test that a no new users have been created
        assertEquals(1, listener.getSnapshot().getChildrenCount());
        // test that the returning user still exists
        assertNotNull(listener.getSnapshot().child(username));
    }

}

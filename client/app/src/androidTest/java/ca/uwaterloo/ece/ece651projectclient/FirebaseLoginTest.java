package ca.uwaterloo.ece.ece651projectclient;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link FirebaseLogin}.
 */
public class FirebaseLoginTest {

    private static final String USER_A = "userA";
    private static final String USER_B = "userB";

    @BeforeClass
    public static void setUpClass() {
        // initialize the firebase client
        database = FirebaseDatabase.getInstance().getReference().child("FirebaseLoginTest").push();
    }

    static DatabaseReference database;

    @Before
    public void setUp() {
        // initialize the blackboard
        blackboard = new ConcreteBlackboard();
        // initialize the firebase login module
        FirebaseLogin login = new FirebaseLogin(blackboard, database);
        // setup test users
        blackboard.userName().set(USER_A);
    }

    Blackboard blackboard;

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
        blackboard.userName().set(USER_B);
        listener = new BlockingValueEventListener();
        database.child("users").addListenerForSingleValueEvent(listener);
        // test that a new user exists
        assertEquals(numberOfUsers + 1, listener.getSnapshot().getChildrenCount());
        // test that the correct new user was added
        assertNotNull(listener.getSnapshot().child(USER_B));
    }

    @Test
    public void testUserReturning() {
        // get the current number of users
        BlockingValueEventListener listener = new BlockingValueEventListener();
        database.child("users").addListenerForSingleValueEvent(listener);
        long numberOfUsers = listener.getSnapshot().getChildrenCount();
        // set the username to a returning user
        blackboard.userName().set(USER_A);
        listener = new BlockingValueEventListener();
        database.child("users").addListenerForSingleValueEvent(listener);
        // test that a no new users have been created
        assertEquals(numberOfUsers, listener.getSnapshot().getChildrenCount());
        // test that the returning user still exists
        assertNotNull(listener.getSnapshot().child(USER_A));
    }

}
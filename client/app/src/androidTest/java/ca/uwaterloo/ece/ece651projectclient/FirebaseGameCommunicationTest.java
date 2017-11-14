package ca.uwaterloo.ece.ece651projectclient;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * FirebaseGameCommunication unit testing.
 */
public class FirebaseGameCommunicationTest {

    @Before
    public void setUp() {
        // create first player
        blackboard1 = new ConcreteBlackboard();
        FirebaseCommunication communication1 = new FirebaseCommunication(blackboard1);
        gameCommunication1 = new FirebaseGameCommunication(blackboard1);
        blackboard1.userName().set("gameUserA");
        // create a game using that player
        blackboard1.othersNames().set(new HashSet<String>(Arrays.asList("gameUserB")));
        blackboard1.visibilityMatrixType().set(VisibilityMatrixType.CUSTOM);
        Map<String, Set<String>> matrix = new HashMap<>();
        matrix.put("gameUserA", new HashSet<String>(Arrays.asList("gameUserB")));
        blackboard1.visibilityMatrix().set(new VisibilityMatrix(matrix));
        communication1.createGame();
        communication1.joinGame();

        // create a second player
        blackboard2 = new ConcreteBlackboard();
        FirebaseCommunication communication2 = new FirebaseCommunication(blackboard2);
        gameCommunication2 = new FirebaseGameCommunication(blackboard2);
        blackboard2.userName().set("gameUserB");
        // join the previously created game
        blackboard2.currentGameId().set(blackboard1.currentGameId().value());
        communication2.joinGame();

        // get a reference to the firebase database
        database = FirebaseDatabase.getInstance().getReference();
    }

    DatabaseReference database;
    Blackboard blackboard1, blackboard2;
    FirebaseGameCommunication gameCommunication1, gameCommunication2;

    @Test
    public void testEnableSynchronization() {
        // enable synchronization for the second player
        assertTrue(gameCommunication2.enableSynchronization());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // verify that otherNames is correctly updated
        assertEquals(new HashSet<>(Arrays.asList("gameUserA")),
                blackboard2.othersNames().value());
        // verify that visibilityMatrix is correctly updated
        assertNotNull(blackboard1.visibilityMatrix().value());
        assertNotNull(blackboard2.visibilityMatrix().value());
        assertEquals(blackboard1.visibilityMatrix().value().asMap(),
                blackboard2.visibilityMatrix().value().asMap());
    }

}
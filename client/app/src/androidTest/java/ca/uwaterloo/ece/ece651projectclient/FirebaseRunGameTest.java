package ca.uwaterloo.ece.ece651projectclient;

import android.location.Location;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.BeforeClass;
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
 * FirebaseRunGame unit testing.
 */
public class FirebaseRunGameTest {

    @BeforeClass
    public static void setUpClass() {
        // initialize the firebase client
        database = FirebaseDatabase.getInstance().getReference().child("FirebaseRunGame").push();
    }

    static DatabaseReference database;

    @Before
    public void setUp() throws InterruptedException {
        // create first player
        blackboard1 = new ConcreteBlackboard();
        new FirebaseLogin(blackboard1, database);
        new FirebaseCreateGame(blackboard1, database);
        new FirebaseJoinGame(blackboard1, database);
        blackboard1.userName().set("gameUserA");
        // create a game using that player
        blackboard1.othersNames().set(new HashSet<String>(Arrays.asList("gameUserB")));
        blackboard1.visibilityMatrixType().set(VisibilityMatrixType.CUSTOM);
        Map<String, Set<String>> matrix = new HashMap<>();
        matrix.put("gameUserA", new HashSet<String>(Arrays.asList("gameUserB")));
        blackboard1.visibilityMatrix().set(new VisibilityMatrix(matrix));
        blackboard1.gameState().set(GameState.CREATING);
        Thread.sleep(1000);

        // create a second player
        blackboard2 = new ConcreteBlackboard();
        new FirebaseLogin(blackboard2, database);
        new FirebaseCreateGame(blackboard2, database);
        new FirebaseJoinGame(blackboard2, database);
        blackboard2.userName().set("gameUserB");
        // join the previously created game
        blackboard2.currentGameId().set(blackboard1.currentGameId().value());
        blackboard2.gameState().set(GameState.JOINING);
        Thread.sleep(1000);

        // attach the game running handlers
        runGame1 = new FirebaseRunGame(blackboard1, database);
        runGame2 = new FirebaseRunGame(blackboard2, database);
    }

    Blackboard blackboard1, blackboard2;
    FirebaseRunGame runGame1, runGame2;

    @Test
    public void testEnableSynchronization() {
        // enable synchronization for the second player
        assertTrue(runGame2.enableSynchronization());
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

    @Test
    public void testEnableLocationSynchronization() {
        // enable synchronization for both players
        assertTrue(runGame1.enableSynchronization());
        assertTrue(runGame2.enableSynchronization());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // set the location of the second player
        Location location = new Location("FirebaseRunGameTest");
        location.setLatitude(0);
        location.setLongitude(0);
        blackboard2.userLocation().set(location);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // verify that othersLocations has been correctly updated
        assertTrue(blackboard1.othersLocations().value().containsKey("gameUserB"));
        assertEquals(blackboard2.userLocation().value().getLatitude(),
                blackboard1.othersLocations().value().get("gameUserB").getLatitude(), 0.001);
        assertEquals(blackboard2.userLocation().value().getLongitude(),
                blackboard1.othersLocations().value().get("gameUserB").getLongitude(), 0.001);
        assertTrue(blackboard2.othersLocations().value().isEmpty());
    }

    @Test
    public void testTagSynchronization() {
         // enable synchronization for both players
        assertTrue(runGame1.enableSynchronization());
        assertTrue(runGame2.enableSynchronization());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // set the second player to be tagged
        blackboard2.userTaggedBy().set("gameUserA");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // verify the the second player has been tagged out
        BlockingValueEventListener listener = new BlockingValueEventListener();
        database.child("games").child(blackboard1.currentGameId().value()).child("out")
                .child("gameUserB").addListenerForSingleValueEvent(listener);
        assertTrue(listener.getSnapshot().getValue(Boolean.class));
        assertEquals(GameState.OUT, blackboard2.gameState().value());
    }

}
package ca.uwaterloo.ece.ece651projectclient;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link FirebaseCreateGame}.
 */
public class FirebaseCreateGameTest {

    private static final int WAIT = 3000;

    private static final String USER_A = "userA";
    private static final String USER_B = "userB";

    @BeforeClass
    public static void setUpClass() {
        // initialize the firebase client
        database = FirebaseDatabase.getInstance().getReference().child("FirebaseCreateGame").push();
    }

    static DatabaseReference database;

    @Before
    public void setUp() {
        // initialize the blackboard
        blackboard = new ConcreteBlackboard();
        // initialize the firebase create game module
        createGame = new FirebaseCreateGame(blackboard, database);
    }

    Blackboard blackboard;
    FirebaseCreateGame createGame;

    @Test
    public void testGameCreateNoUserName() throws InterruptedException {
        // set the username to null
        blackboard.userName().set(null);
        // check that game creation is rejected
        blackboard.gameState().set(GameState.CREATING);
        Thread.sleep(WAIT);
        assertEquals(GameState.UNINITIALIZED, blackboard.gameState().value());
    }

    @Test
    public void testGameCreateNotEnoughPlayers() throws InterruptedException {
        // set username to a returning user
        blackboard.userName().set(USER_A);
        // check that game creation is rejected
        blackboard.gameState().set(GameState.CREATING);
        Thread.sleep(WAIT);
        assertEquals(GameState.UNINITIALIZED, blackboard.gameState().value());
    }

    @Test
    public void testGameCreateInvalidMatrix() throws InterruptedException {
        // set username to a returning user
        blackboard.userName().set(USER_A);
        // add another player
        Set<String> othersNames = blackboard.othersNames().value();
        othersNames.add(USER_B);
        blackboard.othersNames().set(othersNames);
        // create invalid visibility matrix
        blackboard.visibilityMatrixType().set(VisibilityMatrixType.CUSTOM);
        blackboard.visibilityMatrix().set(new
                VisibilityMatrix(VisibilityMatrixType.HIDE_N_SEEK, 3));
        // check that game creation is rejected
        blackboard.gameState().set(GameState.CREATING);
        Thread.sleep(WAIT);
        assertEquals(GameState.UNINITIALIZED, blackboard.gameState().value());
    }

    @Test
    public void testGameCreateClosed() throws InterruptedException {
        // set username to a returning user
        blackboard.userName().set(USER_A);
        // add another player
        Set<String> othersNames = blackboard.othersNames().value();
        othersNames.add(USER_B);
        blackboard.othersNames().set(othersNames);
        // select visibility matrix type
        blackboard.visibilityMatrixType().set(VisibilityMatrixType.HIDE_N_SEEK);
        // check that game creation is accepted
        blackboard.gameState().set(GameState.CREATING);
        Thread.sleep(WAIT);
        assertEquals(GameState.JOINING, blackboard.gameState().value());
    }

    @Test
    public void testGameCreateOpen() throws InterruptedException {
        // set username to a returning user
        blackboard.userName().set(USER_A);
        // add another open player
        blackboard.numberOfPlayers().set(2);
        // select visibility matrix type
        blackboard.visibilityMatrixType().set(VisibilityMatrixType.HIDE_N_SEEK);
        // check that game creation is accepted
        blackboard.gameState().set(GameState.CREATING);
        Thread.sleep(WAIT);
        assertEquals(GameState.JOINING, blackboard.gameState().value());
    }

}
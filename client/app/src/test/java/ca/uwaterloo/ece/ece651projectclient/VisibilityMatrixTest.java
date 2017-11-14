package ca.uwaterloo.ece.ece651projectclient;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * VisibilityMatrix unit testing
 */
public class VisibilityMatrixTest {

    @Before
    public void setUp() {
        matrix = new VisibilityMatrix(VisibilityMatrixType.HIDE_N_SEEK, 2);
    }

    VisibilityMatrix matrix;

    @Test
    public void testAssignPlayer() {
        matrix.assignPlayer("@0", "player1");
        matrix.assignPlayer("@1", "player2");
        assertFalse(matrix.getPlayers().contains("@0"));
        assertFalse(matrix.getPlayers().contains("@1"));
        assertTrue(matrix.getPlayers().contains("player1"));
        assertTrue(matrix.getPlayers().contains("player2"));
    }

    @Test
    public void testFirebaseSerialization() {
        matrix.assignPlayer("player");
        VisibilityMatrix serialized = VisibilityMatrix.fromFirebaseSerializableMap
                (matrix.asFirebaseSerializableMap());
        assertEquals(matrix.asMap(), serialized.asMap());
        assertTrue(matrix.getPlayers().contains("player"));
    }

}
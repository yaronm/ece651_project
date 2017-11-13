package ca.uwaterloo.ece.ece651projectclient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A visibility matrix class.
 */
public class VisibilityMatrix {

    /**
     * Constructs a visibility matrix of the given type and with the given number of players. If
     * the given type is CUSTOM, an empty visibility matrix will be created.
     *
     * @param type            a visibility matrix type
     * @param numberOfPlayers the number of players to include in the visibility matrix
     * @throws IllegalArgumentException if numberOfPlayers < 2
     */
    public VisibilityMatrix(VisibilityMatrixType type, int numberOfPlayers) {
        if (numberOfPlayers < 2) {
            throw new IllegalArgumentException("Too few players");
        }
        switch (type) {
            case ASSASSIN:
                initializeAssassin(numberOfPlayers);
                break;
            case HIDE_N_SEEK:
                initializeHideNSeek(numberOfPlayers);
                break;
        }
    }

    private void initializeAssassin(int numberOfPlayers) {
        for (int i = 0; i < numberOfPlayers; ++i) {
            String assasin = Integer.toString(i);
            Set<String> target = new HashSet<>(1);
            target.add(Integer.toString((i + 1) % numberOfPlayers));
            matrix.put(assasin, target);
        }
    }

    private void initializeHideNSeek(int numberOfPlayers) {
        String seeker = Integer.toString(0);
        Set<String> hiders = new HashSet<>(numberOfPlayers - 1);
        for (int i = 1; i < numberOfPlayers; ++i) {
            hiders.add(Integer.toString(i));
        }
        matrix.put(seeker, hiders);
    }

    Map<String, Set<String>> matrix = new HashMap<>();

    /**
     * Gets a map representing this visibility matrix. The returned map is backed by this
     * visibility matrix; changes to one will affect the other.
     *
     * @return a map respresenting this visibility matrix
     */
    public Map<String, Set<String>> asMap() {
        return matrix;
    }

    /**
     * Validates that this visibility matrix can be used in a game with the given number of players.
     *
     * @param numberOfPlayers the number of players
     * @return whether this visibility matrix is valid
     */
    public boolean isValid(int numberOfPlayers) {
        // collect all the players in this visibility matrix
        Set<String> players = new HashSet<>();
        players.addAll(matrix.keySet());
        for (Set<String> targets: matrix.values()) {
            players.addAll(targets);
        }
        // compare the number of players in this matrix to the validation target
        return numberOfPlayers == players.size();
    }

}

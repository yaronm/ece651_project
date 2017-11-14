package ca.uwaterloo.ece.ece651projectclient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * A visibility matrix class.
 */
public class VisibilityMatrix {

    /**
     * Constructs a visibility matrix of the given type and with the given number of players.
     *
     * @param type            a visibility matrix type
     * @param numberOfPlayers the number of players to include in the visibility matrix
     * @throws IllegalArgumentException if type is CUSTOM or numberOfPlayers < 2
     */
    public VisibilityMatrix(VisibilityMatrixType type, int numberOfPlayers) {
        if (type == VisibilityMatrixType.CUSTOM) {
            throw new IllegalArgumentException("Invalid visibility matrix type: " +
                    VisibilityMatrixType.CUSTOM);
        }
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
            String assasin = unassignedName(i);
            Set<String> target = new HashSet<>(1);
            target.add(unassignedName((i + 1) % numberOfPlayers));
            matrix.put(assasin, target);
            unassigned.add(assasin);
        }
    }

    private void initializeHideNSeek(int numberOfPlayers) {
        String seeker = unassignedName(0);
        Set<String> hiders = new HashSet<>(numberOfPlayers - 1);
        for (int i = 1; i < numberOfPlayers; ++i) {
            String hider = unassignedName(i);
            hiders.add(hider);
            unassigned.add(hider);
        }
        matrix.put(seeker, hiders);
        unassigned.add(seeker);
    }

    private String unassignedName(int i) {
        return "@" + i;
    }

    /**
     * Constructs a visibility matrix with the given mapping of visibilities with no unassigned
     * players.
     *
     * @param matrix a mapping of visibilities
     */
    public VisibilityMatrix(Map<String, Set<String>> matrix) {
        this(matrix, null);
    }

    /**
     * Constructs a visibility matrix with the given mapping of visibilities with the given
     * unassigned players.
     *
     * @param matrix     a mapping of visibilities
     * @param unassigned a set of names that have not yet been assigned players
     */
    public VisibilityMatrix(Map<String, Set<String>> matrix, Set<String> unassigned) {
        this.matrix = matrix;
        if (unassigned != null) {
            this.unassigned.addAll(unassigned);
        }
    }

    private Map<String, Set<String>> matrix = new HashMap<>();
    private Set<String> unassigned = new HashSet<>();

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
     * @return the players in this visibility matrix
     */
    Set<String> getPlayers() {
        Set<String> players = new HashSet<>();
        players.addAll(matrix.keySet());
        for (Set<String> targets : matrix.values()) {
            players.addAll(targets);
        }
        return players;
    }

    /**
     * Assigns a player to one of this visibility matrix's unassigned slots at random. If the player
     * is already in the visibility matrix or if there are no unassigned slots, this method has
     * no effect.
     *
     * @param player the player to be assigned
     */
    public void assignPlayer(String player) {
        if (!unassigned.isEmpty() && !getPlayers().contains(player)) {
            // choose a random slot to assign to the player
            Random random = new Random();
            int i = random.nextInt(unassigned.size());
            String replaced = unassigned.toArray(new String[unassigned.size()])[i];
            unassigned.remove(replaced);
            assignPlayer(replaced, player);
        }
    }

    void assignPlayer(String replaced, String player) {
        // iterate over the visibility matrix and replace occurrences of the unassigned
        // placeholder with the given player
        if (matrix.containsKey(replaced)) {
            matrix.put(player, matrix.remove(replaced));
        }
        for (Set<String> players : matrix.values()) {
            if (players.contains(replaced)) {
                players.add(player);
                players.remove(replaced);
            }
        }
    }

    /**
     * Transfers one player's targets to another player's. Upon completion, the source player
     * will have no targets.
     *
     * @param src the player from which targets should be transferred
     * @param dst the player to which the targets should be transferred
     */
    public void transferTargets(String src, String dst) {
        Set<String> toTransfer = matrix.remove(src);
        if (toTransfer != null) {
            matrix.get(dst).addAll(toTransfer);
        }
    }

    /**
     * Validates that this visibility matrix can be used in a game with the given number of players.
     *
     * @param numberOfPlayers the number of players
     * @return whether this visibility matrix is valid
     */
    public boolean isValid(int numberOfPlayers) {
        return numberOfPlayers == getPlayers().size();
    }

    /**
     * Gets a map representing this visibility matrix in a way that can be serialize and stored
     * by firebase.
     *
     * @return a map respresenting this visibility matrix
     */
    public Map<String, Object> asFirebaseSerializableMap() {
        // construct the serializable data structure
        Map<String, Object> matrix = new HashMap<>(asMap().size());
        for (Map.Entry<String, Set<String>> entry : asMap().entrySet()) {
            matrix.put(entry.getKey(), FirebaseUtils.setToMap(entry.getValue(), true));
        }
        Map<String, Object> visibility = new HashMap<>();
        visibility.put("matrix", matrix);
        visibility.put("unassigned",
                unassigned.isEmpty() ? null : FirebaseUtils.setToMap(unassigned, true));
        return visibility;
    }

    /**
     * Gets a VisibilityMatrix that corresponds to the given firebase serialization.
     *
     * @return a VisibilityMatrix
     */
    public static VisibilityMatrix fromFirebaseSerializableMap(Map<String, Object> map) {
        Map<String, Set<String>> matrix = new HashMap<>(map.size());
        for (Map.Entry<String, Map<String, ?>> entry :
                ((Map<String, Map<String, ?>>) map.get("matrix")).entrySet()) {
            matrix.put(entry.getKey(), new HashSet<String>(entry.getValue().keySet()));
        }
        Set<String> unassigned = null;
        if (map.containsKey("unassigned")) {
            unassigned = ((Map<String, ?>) map.get("unassigned")).keySet();
        }
        return new VisibilityMatrix(matrix, unassigned);
    }

}

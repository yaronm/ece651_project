package ca.uwaterloo.ece.ece651projectclient;

/**
 * A class representing the type of a visibility matrix. Facilitates the generation of a concrete
 * visibility matrix from an abstract type description. Used by {@link Blackboard} instances to
 * communicate a description of the visibility matrix before a concrete visibility matrix has
 * been defined.
 *
 * @see Blackboard#visibilityMatrixType()
 */
public enum VisibilityMatrixType {

    /**
     * A visibility matrix type where each player is visible to one other player such that the set
     * of all directed visibilities form a single cycle.
     */
    ASSASSIN,

    /**
     * A visibility matrix type where every player is visible to a single player, or "seeker",
     * while no other players are visible to each non-seeker player.
     */
    HIDE_N_SEEK,

    /**
     * A visibility matrix type that represents an unconstrained visibility matrix. For use when
     * the concrete visibility matrix is defined ad hoc by the game creator.
     */
    CUSTOM

}

package ca.uwaterloo.ece.ece651projectclient;

/**
 * A class representing the state of a game. Used by {@link Blackboard} instances to communicate
 * changes of game state to registered observers.
 *
 * @see Blackboard#gameState()
 */
public enum GameState {

    /**
     * The default game state indicating that user and game have not yet been configured.
     */
    UNINITIALIZED,

    /**
     * The game state indicating that the user is configured, but that a new game must be created
     * and joined.
     */
    CREATING,

    /**
     * The game state indicating that the user is configured and the game has been selected, but
     * that the connection to the game server must be established.
     */
    JOINING,

    /**
     * The game state indicating that the user and game are fully configured, that the game is
     * communicating with the game server, and that the game logic is operating to keep the
     * necessary game data up-to-date.
     */
    RUNNING,

    /**
     * The game state indicating that the user and game are fully configured, but that the game is
     * currently disconnected from the game server and the game logic is not necessarily keeping
     * all game data up-to-date.
     */
    PAUSED,

    /**
     * The game state indicating that the user is connected to a game but is no longer playing,
     * because they have been tagged out.
     */
    OUT,

    /**
     * The game state indicating that the game has ended.
     */
    ENDED

}

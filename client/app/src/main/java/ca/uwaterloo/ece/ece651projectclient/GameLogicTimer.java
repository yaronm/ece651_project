package ca.uwaterloo.ece.ece651projectclient;

import android.util.Log;

import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Game logic class for handling game end time.
 */
public class GameLogicTimer {

    private static final String TAG = "GLTimer";

    /**
     * Creates a end time watching game logic component.
     *
     * @param blackboard a blackboard
     */
    public GameLogicTimer(Blackboard blackboard) {
        // store the blackboard
        this.blackboard = blackboard;
        // initialize the timer
        timer = new Timer();
        // configure component to listen for end time changes
        blackboard.gameEndTime().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                updateTimer();
            }
        });
    }

    private Blackboard blackboard;

    private Timer timer;

    /**
     * Updates the timer with the correct end time.
     */
    private void updateTimer() {
        // get the updated end time from the blackboard
        Date endTime = blackboard.gameEndTime().value();
        if (endTime == null) {
            Log.d(TAG, "Could not schedule null end time");
            return;
        }
        // schedule the game to end at the specified time
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // set the game state to ENDED
                Log.d(TAG, "The end time of the game has been reached");
                blackboard.gameState().set(GameState.ENDED);
            }
        }, endTime);
    }

}

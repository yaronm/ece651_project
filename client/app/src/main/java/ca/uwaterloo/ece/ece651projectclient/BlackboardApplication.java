package ca.uwaterloo.ece.ece651projectclient;

import android.app.Application;

/**
 * A extension of the Android application with an associated blackboard.
 */
public class BlackboardApplication extends Application {

    @Override
    public void onCreate(){
        super.onCreate();
        blackboard = new ConcreteBlackboard();
        GameLogicLocation gLLocation = new GameLogicLocation(this, blackboard);
        GameLogicOrientation gLOrientation = new GameLogicOrientation(this, blackboard);
        FirebaseCommunication communication = new FirebaseCommunication(blackboard);
    }

    private Blackboard blackboard;

    /**
     * @return the Blackboard instance associated with this application
     */
    public Blackboard getBlackboard(){
        return blackboard;
    }

}

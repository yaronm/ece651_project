package ca.uwaterloo.ece.ece651projectclient;

import android.app.Application;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A extension of the Android application with an associated blackboard.
 */
public class BlackboardApplication extends Application {

    @Override
    public void onCreate(){
        super.onCreate();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        blackboard = new ConcreteBlackboard();
        new FirebaseLogin(blackboard, database);
        new FirebaseCreateGame(blackboard, database);
        new FirebaseJoinGame(blackboard, database);
        new FirebaseRunGame(blackboard, database);
        new GameLogicOrientation(blackboard, this);
        new GameLogicLocation(blackboard, this);
    }

    private Blackboard blackboard;

    /**
     * @return the Blackboard instance associated with this application
     */
    public Blackboard getBlackboard(){
        return blackboard;
    }

}

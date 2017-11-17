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
        FirebaseLogin login = new FirebaseLogin(blackboard, database);
        FirebaseCreateGame createGame = new FirebaseCreateGame(blackboard, database);
        FirebaseJoinGame joinGame = new FirebaseJoinGame(blackboard, database);
        GameLogicLocation gLLocation = new GameLogicLocation(this, blackboard);
        GameLogicOrientation gLOrientation = new GameLogicOrientation(this, blackboard);
    }

    private Blackboard blackboard;

    /**
     * @return the Blackboard instance associated with this application
     */
    public Blackboard getBlackboard(){
        return blackboard;
    }

}

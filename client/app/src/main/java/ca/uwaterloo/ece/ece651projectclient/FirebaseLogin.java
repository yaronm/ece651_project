package ca.uwaterloo.ece.ece651projectclient;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * A class for handling firebase communication during the login process.
 */
public class FirebaseLogin {

    /**
     * Initializes a FirebaseLogin instance and attaches it to the given blackboard.
     *
     * @param blackboard a blackboard
     * @param database   a firebase database reference
     */
    public FirebaseLogin(Blackboard blackboard, DatabaseReference database) {
        // store the blackboard and database reference
        this.blackboard = blackboard;
        this.database = database;
        // observe the blackboard for when the user name is set
        blackboard.userName().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                userNameSet();
            }
        });
    }

    private Blackboard blackboard;
    private DatabaseReference database;

    /**
     * Handles when the user name is set on the blackboard. If the user name is not already
     * associated with a user, creates a new user on the server. If the user name is null, this
     * method does nothing.
     */
    private void userNameSet() {
        // get the new user name
        String userName = blackboard.userName().value();
        // check that it is non-null
        if (userName == null) {
            return;
        }
        // if the user does not exist, add the user atomically via a transaction
        database.child("users").child(userName).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Object currentData = mutableData.getValue();
                // if the user does not already exist, add the user
                if (currentData == null) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("thisIsAPlaceHolder", true);
                    mutableData.setValue(user);
                    return Transaction.success(mutableData);
                }
                // otherwise, do not attempt to re-add the user
                return Transaction.abort();
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed,
                                   DataSnapshot dataSnapshot) {
            }
        });
    }

}

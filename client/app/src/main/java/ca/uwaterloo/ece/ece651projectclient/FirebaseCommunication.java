package ca.uwaterloo.ece.ece651projectclient;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class FirebaseCommunication {

    /**
     * Initializes a firebase communication instance and attaches it to the given blackboard.
     *
     * @param blackboard a blackboard
     */
    public FirebaseCommunication(Blackboard blackboard) {
        // store the blackboard
        this.blackboard = blackboard;
        // get access to the firebase database
        database = FirebaseDatabase.getInstance().getReference();
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
    void userNameSet() {
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
                Map<String, Object> user =
                        mutableData.getValue(new GenericTypeIndicator<Map<String, Object>>() {
                        });
                // if the user does not exist, add the user
                if (user == null) {
                    user = new HashMap<>();
                    user.put("thisIsAPlaceHolder", true);
                    mutableData.setValue(user);
                    return Transaction.success(mutableData);
                }
                // otherwise, do not attempt to re-add the user
                return Transaction.abort();
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed,
                                   DataSnapshot dataSnapshot) {}
        });
    }

}

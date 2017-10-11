package ca.uwaterloo.ece.firebaseintegration;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // initialize the activity's GUI
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // get access to the Firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // configure the GUI to automatically update to present the most recent database state
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // when the database is updated, dump its data to the GUI
                EditText dataText = (EditText) findViewById(R.id.dataText);
                dataText.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // when the connection to the database is lost, display an error message
                EditText dataText = (EditText) findViewById(R.id.dataText);
                dataText.setText("No connection to database.");
            }
        });
    }

    /**
     * Handles retrieving the current key and value from the UI and storing them together into the
     * Firebase database.
     *
     * @param view the view passed by the UI thread on callback
     */
    public void onAddButtonClicked(View view) {
        // retrieve the key and value to store
        EditText keyText = (EditText) findViewById(R.id.keyText);
        String key = keyText.getText().toString();
        EditText valueText = (EditText) findViewById(R.id.valueText);
        String value = valueText.getText().toString();
        // store the key/value pair into the user data dictionary
        String userDataKey = getResources().getString(R.string.user_data_key);
        mDatabase.child(userDataKey).child(key).setValue(value);
    }

}

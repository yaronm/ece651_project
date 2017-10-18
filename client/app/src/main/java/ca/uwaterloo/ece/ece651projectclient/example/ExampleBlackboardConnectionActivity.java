package ca.uwaterloo.ece.ece651projectclient.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import ca.uwaterloo.ece.ece651projectclient.Blackboard;
import ca.uwaterloo.ece.ece651projectclient.ConcreteBlackboard;
import ca.uwaterloo.ece.ece651projectclient.PolarCoordinates;
import ca.uwaterloo.ece.ece651projectclient.R;

/**
 * An example activity demonstrating setting and observing blackboard data. In order to run this
 * activity, you may need to navigate to Run > Edit Configurations > app > General > Launch Options
 * > Activity and select this activity. Logging messages logged with the
 * {@link Log#i(String, String)} method can be viewed at while Android is running by navigating to
 * View > Tool Windows > Android Monitor.
 *
 * This example creates a blackboard and connects to it 3 components: (1) an instance of this
 * Activity (GUI), (2) an example game logic, and (3) and example database connection. Its purpose
 * is to show the flow of activity through the blackboard. Many of the values that are passed
 * through the blackboard are mock values and are not intended to represent real app data. When the
 * user enters a username and current game id and presses the update button, these values are
 * updated to the blackboard. The database connection observes this and updates the blackboard with
 * the locations of the other players. The game logic observes this second update performed by the
 * database connection and updates the blackboard with the deltas of the other players. Finally,
 * the GUI observes this third update performed by the game logic and display the names of the
 * other players whose deltas it has received. This string of updates and observances can be seen
 * in the aforementioned logging messages.
 *
 * @see Blackboard
 * @see ExampleBlackboardConnectionLogic
 * @see ExampleBlackboardConnectionDatabase
 */
public class ExampleBlackboardConnectionActivity extends AppCompatActivity {

    private static final String TAG = "ExampleBlkConActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example_blackboard_connection);

        // as the main activity, initialize the app architecture
        // first, create and store the blackboard
        blackboard = new ConcreteBlackboard();
        // second, create the game logic and connect it to the blackboard
        ExampleBlackboardConnectionLogic logic = new ExampleBlackboardConnectionLogic(blackboard);
        // finally, create the database connection and also connect it to the blackboard
        ExampleBlackboardConnectionDatabase database = new
                ExampleBlackboardConnectionDatabase(blackboard);

        // as the user interface, observe the blackboard for changes to the other players deltas
        blackboard.othersDeltas().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                Log.i(TAG, "Observed that othersDeltas blackboard field has been updated");
                // get the other players deltas from the blackboard
                Map<String, PolarCoordinates> othersDeltas = blackboard.othersDeltas().value();
                // display each one
                EditText textOthersDeltas = (EditText) findViewById(R.id.editOthersDeltas);
                textOthersDeltas.setText("Deltas:");
                for (String userName : othersDeltas.keySet())
                    textOthersDeltas.append("\n  " + userName);
            }
        });
    }

    private Blackboard blackboard;

    /**
     * Handles a click of the update button.
     *
     * @param view the view invoking this method
     */
    public void onButtonClick(View view) {
        // get the username and game id values entered by the user
        EditText textUserName = (EditText) findViewById(R.id.editUserName);
        EditText textCurrentGameId = (EditText) findViewById(R.id.editCurrentGameId);
        // update the blackboard with those new values
        Log.i(TAG, "Updated userName blackboard field");
        blackboard.userName().set(textUserName.getText().toString());
        Log.i(TAG, "Updated currentGameId blackboard field");
        blackboard.currentGameId().set(textCurrentGameId.getText().toString());
    }

}

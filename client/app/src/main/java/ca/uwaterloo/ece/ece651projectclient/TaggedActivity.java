package ca.uwaterloo.ece.ece651projectclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.Set;

public class TaggedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tagged);

        // get the others user names from the blackboard
        blackboard = ((BlackboardApplication) getApplication()).getBlackboard();
        Set<String> othersNames = blackboard.othersNames().value();

        // dynamically populate the activities radio group with the game's other users
        otherUsersRadioGroup = (RadioGroup) findViewById(R.id.othersNamesRadioGroup);
        for (String name : othersNames) {
            RadioButton button = new RadioButton(this);
            button.setText(name);
            otherUsersRadioGroup.addView(button);
        }
        otherUsersRadioGroup.check(otherUsersRadioGroup.getChildAt(0).getId());

        // disable the select button if the game is not in the running state
        if (blackboard.gameState().value() != GameState.RUNNING) {
            ((Button) findViewById(R.id.buttonSelect)).setEnabled(false);
        }
    }

    private Blackboard blackboard;
    private RadioGroup otherUsersRadioGroup;

    public void onSelectClick(View view) {
        // updated the blackboard with how you were tagged by
        RadioButton checked = (RadioButton) findViewById(otherUsersRadioGroup
                .getCheckedRadioButtonId());
        blackboard.userTaggedBy().set(checked.getText().toString());
        // set the game state to out
        blackboard.gameState().set(GameState.OUT);
        // close the current activity
        finish();
    }

}

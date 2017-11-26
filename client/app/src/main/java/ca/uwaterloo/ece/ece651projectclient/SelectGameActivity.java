package ca.uwaterloo.ece.ece651projectclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class SelectGameActivity extends AppCompatActivity {
    private RadioGroup radioGroup;
    private RadioButton radioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_game);
        application = (BlackboardApplication) getApplication();
        // populate the available games spinner
        application.getBlackboard().availableGames().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                List<String> availableGameList = new ArrayList<>(
                        application.getBlackboard().availableGames().value());
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(SelectGameActivity.this,
                        android.R.layout.simple_spinner_item, availableGameList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                Spinner availableGames = (Spinner) findViewById(R.id.spinnerAvailableGames);
                availableGames.setAdapter(adapter);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    BlackboardApplication application;

    public void onJoinClick(View view) {
        //Get GameId
        Spinner availableGames = (Spinner) findViewById(R.id.spinnerAvailableGames);
        // Update GameId to Blackboard
        application.getBlackboard().currentGameId().set(
                availableGames.getSelectedItem().toString());
        //Change Game state to Join Game
        application.getBlackboard().gameState().set(GameState.JOINING);
        //Start Game Activity
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);

    }

    public void onCreateGameClick(View view) {
        //Get otherNames
        EditText editOtherNames = (EditText) findViewById(R.id.editOtherNames);
        Set<String> names = new HashSet<String>(Arrays.asList(editOtherNames.getText().toString().split(",")));
        //Get NoOfPlayers
        EditText editNoOfPlayers = (EditText) findViewById(R.id.editNoOfPlayers);
        String value = editNoOfPlayers.getText().toString();
        //check if either of those two are given
        int check = 0;
        if (TextUtils.isEmpty(value) && !TextUtils.isEmpty(editOtherNames.getText().toString())) {
            //Update otherNames to blackboard
            application.getBlackboard().othersNames().set(names);
            check = 1;
        } else if (!TextUtils.isEmpty(value) && TextUtils.isEmpty(editOtherNames.getText().toString())) {
            // Update NumberOfPlayers to Blackboard
            application.getBlackboard().numberOfPlayers().set(Integer.parseInt(value));
            check = 1;
        } else if (!TextUtils.isEmpty(value) && !TextUtils.isEmpty(editOtherNames.getText().toString())) {
            //Update otherNames to blackboard
            application.getBlackboard().othersNames().set(names);
            // Update NumberOfPlayers to Blackboard
            application.getBlackboard().numberOfPlayers().set(Integer.parseInt(value));
            check = 1;
        } else if (TextUtils.isEmpty(value) && TextUtils.isEmpty(editOtherNames.getText().toString())) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Enter either other users of number of players", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }
        switch (radioButton.getId()) {
            case R.id.radioTypeAssassin:
                application.getBlackboard().visibilityMatrixType().set(VisibilityMatrixType.ASSASSIN);
                break;
            case R.id.radioTypeHideNSeek:
                application.getBlackboard().visibilityMatrixType().set(VisibilityMatrixType.HIDE_N_SEEK);
                break;
            case R.id.radioTypeCustom:
                application.getBlackboard().visibilityMatrixType().set(VisibilityMatrixType.CUSTOM);
                break;
        }
        //Change Game state to Create Game
        application.getBlackboard().gameState().set(GameState.CREATING);
        //Start Game Activity
        if (check == 1) {
            Intent intent = new Intent(this, GameActivity.class);
            startActivity(intent);
        }

    }

    public void onClick(View v) {
        radioGroup = (RadioGroup) findViewById(R.id.radioGroupSelectMatrixType);

        // get selected radio button from radioGroup
        int selectedId = radioGroup.getCheckedRadioButtonId();

        // find the radio button by returned id
        radioButton = (RadioButton) findViewById(selectedId);

    }
}


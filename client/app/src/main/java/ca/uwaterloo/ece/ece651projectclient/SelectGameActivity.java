package ca.uwaterloo.ece.ece651projectclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SelectGameActivity extends AppCompatActivity {
    private RadioGroup radioGroup;
    private RadioButton radioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_game);
        application = (BlackboardApplication) getApplication();

    }

    @Override
    public void onResume() {
        super.onResume();
        EditText editGameId = (EditText) findViewById(R.id.editGameId);
        editGameId.setText(application.getBlackboard().currentGameId().value());
    }

    BlackboardApplication application;

    public void onJoinClick(View view) {
        //Get GameId
        EditText editGameId = (EditText) findViewById(R.id.editGameId);
        // Update GameId to Blackboard
        application.getBlackboard().currentGameId().set(editGameId.getText().toString());
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
        application.getBlackboard().othersNames().set(names);
        //Get NoOfPlayers
        EditText editNoOfPlayers = (EditText) findViewById(R.id.editNoOfPlayers);
        String value = editNoOfPlayers.getText().toString();
        // Update NumberOfPlayers to Blackboard
        application.getBlackboard().numberOfPlayers().set(Integer.parseInt(value));
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
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);

    }

    public void onClick(View v) {
        radioGroup= (RadioGroup)findViewById(R.id.radioGroupSelectMatrixType);

        // get selected radio button from radioGroup
        int selectedId = radioGroup.getCheckedRadioButtonId();

        // find the radio button by returned id
        radioButton = (RadioButton) findViewById(selectedId);

    }
}


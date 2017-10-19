package ca.uwaterloo.ece.ece651projectclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SelectGameActivity extends AppCompatActivity {

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

    public void onPlayClick(View view){
        EditText editGameId = (EditText) findViewById(R.id.editGameId);
        application.getBlackboard().currentGameId().set(editGameId.getText().toString());
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

}

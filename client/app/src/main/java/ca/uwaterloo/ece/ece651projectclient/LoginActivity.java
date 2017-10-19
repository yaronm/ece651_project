package ca.uwaterloo.ece.ece651projectclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        application = (BlackboardApplication) getApplication();
    }

    @Override
    public void onResume() {
        super.onResume();
        EditText editUserName = (EditText) findViewById(R.id.editUserName);
        editUserName.setText(application.getBlackboard().userName().value());
    }

    BlackboardApplication application;

    public void onLoginClick(View view) {
        EditText editUserName = (EditText) findViewById(R.id.editUserName);
        application.getBlackboard().userName().set(editUserName.getText().toString());
        Intent intent = new Intent(this, SelectGameActivity.class);
        startActivity(intent);
    }

}

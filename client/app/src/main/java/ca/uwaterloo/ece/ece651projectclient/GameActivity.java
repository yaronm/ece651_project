package ca.uwaterloo.ece.ece651projectclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
    }

    public void onDebugClick(View view){
        Intent intent = new Intent(this, DataViewActivity.class);
        startActivity(intent);
    }

}
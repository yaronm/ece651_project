package ca.uwaterloo.ece.minimize_background;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.button);
        TextView text = (TextView) findViewById(R.id.textView);
        button.setOnClickListener(new MyOnClickListener(text));
    }

    /*this override method is used to keep program running in the background*/
    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
    }
}

    class MyOnClickListener implements View.OnClickListener{
        TextView text;
        MyOnClickListener(TextView text ){
            this.text=text;
        }
        @Override
        public void onClick(View v) {
            for(int i=1; i<=20; i++) {
                try {
                    Thread.sleep(1000);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        String message="Test Completed (20 seconds)";
        text.setText(message);
        }

    }




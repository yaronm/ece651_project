package ca.uwaterloo.ece.ece651projectclient;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

public class GameActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        application = (BlackboardApplication) getApplication();
        // configure this activity to be notified regarding the state of the drawing surface
        surfaceCompass = (SurfaceView) findViewById(R.id.surfaceCompass);
        surfaceCompass.getHolder().addCallback(this);
        // configure the compass to be redrawn when the blackboard otherDeltas field is updated or
        // if the blackboard userOrientation field changes
        Observer observer = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                onCompassDraw();
            }
        };
        application.getBlackboard().othersDeltas().addObserver(observer);
        application.getBlackboard().userOrientation().addObserver(observer);
    }

    @Override
    public void onResume() {
        super.onResume();
        application.getBlackboard().currentActivity().set(this);
    }

    BlackboardApplication application;
    SurfaceView surfaceCompass;

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawable = true;
        onCompassDraw();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        drawable = false;
    }

    private boolean drawable = false;

    public void onCompassDraw() {
        // don't bother drawing the compass if the drawing surface is not currently drawable
        if (!drawable)
            return;
        // calculate the dimensions of the compass based upon the drawing canvas
        Canvas canvas = surfaceCompass.getHolder().lockCanvas();
        float centerX = canvas.getWidth() / 2, centerY = canvas.getHeight() / 2;
        // get the data to be rendered from the blackboard
        Map<String, PolarCoordinates> othersDeltas =
                application.getBlackboard().othersDeltas().value();
        Float userOrientation = application.getBlackboard().userOrientation().value();
        // clear the canvas
        canvas.drawColor(Color.WHITE);
        // and we're off!
        if (othersDeltas != null && userOrientation != null) {
            for (String name : othersDeltas.keySet()) {
                // configure the drawing paint
                Paint paint = new Paint();
                Random random = new Random(name.hashCode());
                paint.setColor(random.nextInt() | 0xff000000);

                // get the relative distance to that player
                float distance = othersDeltas.get(name).getRho();
                // interpolate the distance between the lower and upper distance bounds
                float lowerBound = 100, upperBound = 1000;
                float interpolation;
                if (distance < lowerBound) {
                    interpolation = 0;
                } else if (distance > upperBound) {
                    interpolation = 1;
                } else {
                    interpolation = (distance - lowerBound) / (upperBound - lowerBound);
                }

                // calculate the angle of the compass arc
                float minTheta = 5, maxTheta = 360;
                float theta = (maxTheta - minTheta) * (1 - interpolation) + minTheta;
                // calculate the radius of the compass arc
                float minRadius = Math.min(centerX, centerY) * 3 / 10,
                        maxRadius = Math.min(centerX, centerY) * 9 / 10;
                float radius = (maxRadius - minRadius) * interpolation + minRadius;

                // calculate the relative angle to the other player (in radians)
                float bearing = othersDeltas.get(name).getPhi();
                float relativeAngleDegrees = bearing - userOrientation - 90;
                // draw the compass arc pointing towards the other player
                RectF bounds = new RectF(centerX - radius, centerY - radius,
                        centerX + radius, centerY + radius);
                canvas.drawArc(bounds, relativeAngleDegrees - theta / 2, theta, true, paint);
            }
        }
        // release the canvas
        surfaceCompass.getHolder().unlockCanvasAndPost(canvas);
    }

    public void onPauseClick(View view){
        // get whether the pause switch is checked
        boolean checked = ((Switch) view).isChecked();
        // get the game state from the blackboard
        GameState gameState = application.getBlackboard().gameState().value();

        if (checked && gameState == GameState.RUNNING) {
            application.getBlackboard().gameState().set(GameState.PAUSED);
            Toast toast = Toast.makeText(getApplicationContext(), "GAME PAUSED", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        } else if (!checked && gameState == GameState.PAUSED) {
            Toast toast = Toast.makeText(getApplicationContext(), "GAME RESUMED", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
            application.getBlackboard().gameState().set(GameState.RUNNING);
        }
    }

    public void onTaggedClick(View view) {
        Intent intent = new Intent(this, TaggedActivity.class);
        startActivity(intent);
    }

    public void onDebugClick(View view) {
        Intent intent = new Intent(this, DataViewActivity.class);
        startActivity(intent);
    }

}

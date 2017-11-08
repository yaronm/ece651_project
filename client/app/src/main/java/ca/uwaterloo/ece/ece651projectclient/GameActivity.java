package ca.uwaterloo.ece.ece651projectclient;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

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
        float radius = Math.min(centerX, centerY) * 3 / 4;
        // configure the drawing paint
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        // get the data to be rendered from the balckboard
        Map<String, PolarCoordinates> othersDeltas =
                application.getBlackboard().othersDeltas().value();
        Float userOrientation = application.getBlackboard().userOrientation().value();
        // clear the canvas
        canvas.drawColor(Color.WHITE);
        // and we're off!
        if (othersDeltas != null && userOrientation != null) {
            for (String name : othersDeltas.keySet()) {
                // calculate the relative angle to the other player (in radians)
                float bearing = othersDeltas.get(name).getPhi();
                double relativeAngleRadians = Math.toRadians(90 - (bearing - userOrientation));
                // draw the compass line pointing towards the other player
                float endX = (float) (centerX + radius * Math.cos(relativeAngleRadians));
                float endY = (float) (centerY - radius * Math.sin(relativeAngleRadians));
                canvas.drawLine(centerX, centerY, endX, endY, paint);
                //canvas.drawText(name, endX, endY, paint);
            }
        }
        // release the canvas
        surfaceCompass.getHolder().unlockCanvasAndPost(canvas);
    }

    public void onDebugClick(View view) {
        Intent intent = new Intent(this, DataViewActivity.class);
        startActivity(intent);
    }

}

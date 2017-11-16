package ca.uwaterloo.ece.ece651projectclient;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

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
        //create the rectF object used to draw compass
        final RectF oval = new RectF();
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
                int randomColor=getRandomColor();
                paint.setColor(randomColor);
                //get the relative distance to that player
                double distance = othersDeltas.get(name).getRho();
                //normalize distance to within 1000 metres
                float normalized_distance;
                if (distance<1000){
                    normalized_distance=(float) distance/1000;
                }
                else{
                    normalized_distance=1;
                }
                //calculate the angle of the compass arc
                float theta=0;
                if (normalized_distance==0){
                    theta=2;
                }
                else if (normalized_distance==1){
                    theta=360;
                }
                else if (normalized_distance<=0.5){
                    theta=360*(1-(normalized_distance/2));
                }
                else if (normalized_distance<1){
                    theta=360*(1-(2*normalized_distance));
                }
                // calculate the relative angle to the other player (in radians)
                float bearing = othersDeltas.get(name).getPhi();
                float relativeAngleDegrees = 90 - (bearing - userOrientation);
                // draw the compass arc pointing towards the other player
                oval.set((float)(centerX-normalized_distance)*(3/4),(float)(centerY-normalized_distance)*(3/4),(float)(centerX+normalized_distance)*(3/4),(float)(centerY+normalized_distance)*(3/4));
                canvas.drawArc(oval, relativeAngleDegrees-theta, 2*theta, true, paint);
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

    //generate a random color
    public int getRandomColor(){
        Random rand = new Random();

        int r = rand.nextInt(255);
        int g = rand.nextInt(255);
        int b = rand.nextInt(255);
        int randomColor = Color.rgb(r,g,b);

        return randomColor;
    }

}

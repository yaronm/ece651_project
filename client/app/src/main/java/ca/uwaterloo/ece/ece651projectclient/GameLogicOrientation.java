package ca.uwaterloo.ece.ece651projectclient;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Observable;
import java.util.Observer;

/**
 * Game logic class for handling changes in device orientation.
 */
public class GameLogicOrientation {

    private static final String TAG = "GLOrient";

    /**
     * Creates an orientation game logic component.
     *
     * @param blackboard a blackboard
     * @param context    a application context for accessing device sensors; if null, component will
     *                   expect to receive sensor updates via direct method invocation
     */
    GameLogicOrientation(final Blackboard blackboard, Context context) {
        // store blackboard
        this.blackboard = blackboard;
        // get the sensor manager from the given context
        manager = context == null ? null :
                (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        // configure component to listen for game state changes
        blackboard.gameState().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                // get the game state
                GameState state = blackboard.gameState().value();
                // enable sensors only when the game is in the RUNNING state
                switch (state) {
                    case RUNNING:
                        enableOrientation();
                        break;
                    default:
                        disableOrientation();
                        break;
                }
            }
        });
    }

    private Blackboard blackboard;

    private SensorManager manager;
    private SensorEventListener listener;

    private float[] accelerometerValues = new float[3], magneticFieldValues = new float[3];

    /**
     * Sets the accelerometer values for orientation calculation.
     */
    void setAccelerometerValues(float[] accelerometerValues) {
        this.accelerometerValues = accelerometerValues;
    }

    /**
     * Sets the magnetic field values for orientation calculation.
     */
    void setMagneticFieldValues(float[] magneticFieldValues) {
        this.magneticFieldValues = magneticFieldValues;
    }

    /**
     * Enables orientation sensor updates to the blackboard.
     */
    private void enableOrientation() {
        // check that the sensor manager is available
        if (manager == null) {
            Log.d(TAG, "Could not enable orientation sensors: sensor manager is null");
            return;
        }
        // clear previous listeners
        disableOrientation();
        // create a new listener
        listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    accelerometerValues = event.values;
                }
                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    magneticFieldValues = event.values;
                }
                updateOrientation();
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };
        // get accelerometer sensor
        Sensor accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
            Log.d(TAG, "Could not enable orientation sensors: could not get accelerometer");
            return;
        }
        // get magnetic sensor
        Sensor magnetic = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magnetic == null) {
            Log.d(TAG, "Could not enable orientation sensors: could not get magnetic field");
            return;
        }
        // register listeners
        manager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        manager.registerListener(listener, magnetic, SensorManager.SENSOR_DELAY_UI);
    }

    /**
     * Disables orientation sensor updates.
     */
    private void disableOrientation() {
        // check that the sensor manager is available
        if (manager == null) {
            Log.d(TAG, "Could not disable orientation sensors: sensor manager is null");
            return;
        }
        // disable sensor event listening
        if (listener != null) {
            manager.unregisterListener(listener);
        }
    }

    /**
     * Updates the device's orientation from the stored accelerometer and magnetic field values
     * and updated the results to the blackboard.
     */
    void updateOrientation() {
        // calculate the new orientation
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);
        float newOrientation = (float) Math.toDegrees(values[0]);
        // check that the new orientation an update on the previous orientation
        float previousOrientation = blackboard.userOrientation().value();
        if (Math.abs(newOrientation - previousOrientation) >= 5) {
            // if it is, update the new orientation to the blackboard
            blackboard.userOrientation().set(newOrientation);
        }
    }

}

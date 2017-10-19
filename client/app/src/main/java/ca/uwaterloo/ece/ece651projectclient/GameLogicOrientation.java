package ca.uwaterloo.ece.ece651projectclient;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created by Terry on 2017/10/19.
 * @author Chen XU and Haoyuan Zhang
 * This class is for getting and updating orientation of user
 *
 * * Methods:
 * 1. setOrientation(); //initialize sensor manger and listener
 * This method can start monitor changes of user orientation
 * Create a instance of sensor manager and orientation listener;
 *
 * 2. void updateOrientation(); //update our orientation
 * Update orientation of user to the blackboard;
 *
 * 3. void calculateOrientation(); //toos for calculating orientation
 *
 * 4.delete Listener();
 * close listener service;
 */

public class GameLogicOrientation {

    //variables for initialization
    private Blackboard blackboard;
    private Context userContext;

    //variables for orientation sensor
    private float orientation;
    private SensorManager mSensorManager;
    private Sensor accelerometer; // accelerate sensor
    private Sensor magnetic; // magnetic sensor
    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];

    //set parameters
    /**
     * This is constructor of class GameLogicOrientation
     * */
    public GameLogicOrientation(Context userContext, Blackboard blackboard) {
        this.userContext = userContext;
        this.blackboard = blackboard;
        setOrientation();
    }


    /**
     * initialize orientation
     * */
    public void setOrientation() {

        //create instance for sensor manager
        mSensorManager = (SensorManager) userContext.getSystemService(Context.SENSOR_SERVICE);
        //initialize accelerate sensor
        accelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //initialize magnetic sensor
        magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // register listener
        mSensorManager.registerListener(new MySensorEventListener(),
                accelerometer, Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(new MySensorEventListener(), magnetic,
                Sensor.TYPE_MAGNETIC_FIELD);
        calculateOrientation();
    }

    /**
     * This method is for calculating orientation
     *
     */
    public void calculateOrientation() {
        //calculate orientation
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues,
                magneticFieldValues);
        SensorManager.getOrientation(R, values);
        orientation = (float) Math.toDegrees(values[0]);
        Log.i("orientation", "orientation: " + orientation);
        updateOrientation();
    }


    /**
     * update orientation
     * */
    public void updateOrientation() {

        blackboard.userOrientation().set(orientation);
    }

    /**
    * delete Listener
    */
    public void deleteListener() {
        mSensorManager.unregisterListener(new MySensorEventListener());
    }

    /**
     * tools for sensor listener
     * */
    class MySensorEventListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = event.values;
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticFieldValues = event.values;
            }
            calculateOrientation();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

}






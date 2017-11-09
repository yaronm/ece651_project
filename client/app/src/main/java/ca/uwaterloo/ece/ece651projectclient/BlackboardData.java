package ca.uwaterloo.ece.ece651projectclient;

import android.util.Log;

import java.util.Observable;
import java.util.Observer;

/**
 * A class for storing observable data for inclusion in a blackboard. To register an observer
 * object to be notified when the data is updated, invoke the {@link #addObserver(Observer)}
 * method.
 *
 * @param <T> the type of the stored data
 * @see Blackboard
 */
public class BlackboardData<T> extends Observable {

    private static final String TAG = "BlackboardData";

    public BlackboardData(T value) {
        set(value);
    }

    private T value;

    /**
     * Gets the stored data. If the stored data is mutable, it is the responsibility of the caller
     * to invoke the {@link #set(Object)} method after mutating the data in order to notify all
     * observers that the data has been updated.
     *
     * @return the stored value of the data
     */
    public T value() {
        return value;
    }

    /**
     * Sets the stored data and notifies all observers that the data has been updated.
     *
     * @param value the new value of the data
     */
    public void set(T value) {
        this.value = value;
        Log.d(TAG, value != null ? value.toString() : "null");
        setChanged();
        notifyObservers();
    }

}

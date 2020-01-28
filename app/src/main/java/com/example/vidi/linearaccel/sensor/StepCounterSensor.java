package com.example.vidi.linearaccel.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.vidi.linearaccel.sensor.observer.StepCounterSensorObserver;

import java.util.ArrayList;

public class StepCounterSensor implements SensorEventListener
{

    private static final String tag = StepCounterSensor.class.getSimpleName();

    // Keep track of observers.
    private ArrayList<StepCounterSensorObserver> observersAcceleration;

    // We need the Context to register for Sensor Events.
    private Context context;

    // Keep a local copy of the acceleration values that are copied from the
    // sensor event.
    private float[] step= new float[3];

    // The time stamp of the most recent Sensor Event.
    private long timeStamp = 0;

    // We need the SensorManager to register for Sensor Events.
    private SensorManager sensorManager;

    /**
     * Initialize the state.
     *
     * @param context
     *            the Activities context.
     */
    public StepCounterSensor(Context context)
    {
        super();

        this.context = context;


        observersAcceleration = new ArrayList<StepCounterSensorObserver>();

        sensorManager = (SensorManager) this.context
                .getSystemService(Context.SENSOR_SERVICE);

    }

    /**
     * Register for Sensor.TYPE_STEP_COUNTER measurements.
     *
     * @param observer
     *            The observer to be registered.
     */
    public void registerStepCounterObserver(StepCounterSensorObserver observer)
    {
        // If there are currently no observers, but one has just requested to be
        // registered, register to listen for sensor events from the device.
        if (observersAcceleration.size() == 0)
        {
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                    SensorManager.SENSOR_DELAY_FASTEST);
        }

        // Only register the observer if it is not already registered.
        int i = observersAcceleration.indexOf(observer);
        if (i == -1)
        {
            observersAcceleration.add(observer);
        }
    }

    /**
     * Remove Sensor.TYPE_STEP_COUNTERY measurements.
     *
     * @param observer
     *            The observer to be removed.
     */
    public void removeStepCounterObserver(StepCounterSensorObserver observer)
    {
        int i = observersAcceleration.indexOf(observer);
        if (i >= 0)
        {
            observersAcceleration.remove(i);
        }

        // If there are no observers, then don't listen for Sensor Events.
        if (observersAcceleration.size() == 0)
        {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // Do nothing.
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER)
        {
            System.arraycopy(event.values, 0, step, 0,
                    event.values.length);

            timeStamp = event.timestamp;

            notifyStepCounterObserver();
        }
    }

    /**
     * Notify observers with new measurements.
     */
    private void notifyStepCounterObserver()
    {
        for (StepCounterSensorObserver a : observersAcceleration)
        {
            a.onStepCounterSensorChanged(this.step, this.timeStamp);
        }
    }
}

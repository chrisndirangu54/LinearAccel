package com.example.vidi.linearaccel.sensor.observer;

public interface StepCounterSensorObserver
{
	/**
	 * Notify observers when new acceleration measurements are available.
	 * @param acceleration the acceleration values (x, y, z)
	 * @param timeStamp the time of the sensor update.
	 */
	public void onStepCounterSensorChanged(float[] step,
                                            long timeStamp);
}

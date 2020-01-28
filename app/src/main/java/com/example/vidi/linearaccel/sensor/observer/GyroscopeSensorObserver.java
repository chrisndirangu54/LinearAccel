package com.example.vidi.linearaccel.sensor.observer;

public interface GyroscopeSensorObserver
{
	/**
	 * Notify observers when new gyroscope measurements are available.
	 * @param gyroscope the rotation values (x, y, z)
	 * @param timeStamp the time of the sensor update.
	 */
	public void onGyroscopeSensorChanged(float[] gyroscope, long timeStamp);
}

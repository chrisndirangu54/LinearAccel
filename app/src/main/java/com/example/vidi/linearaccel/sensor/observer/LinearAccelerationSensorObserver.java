package com.example.vidi.linearaccel.sensor.observer;

public interface LinearAccelerationSensorObserver
{
	/**
	 * Notify observers when new angular velocity measurements are available.
	 * @param angularVelocity the angular velocity of the device (x,y,z).
	 * @param timeStamp the time stamp of the measurement.
	 */
	public void onLinearAccelerationSensorChanged(float[] linearAcceleration, long timeStamp);
}

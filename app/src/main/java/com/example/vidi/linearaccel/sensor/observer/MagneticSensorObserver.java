package com.example.vidi.linearaccel.sensor.observer;

public interface MagneticSensorObserver
{
	/**
	 * Notify observers when new magnetic measurements are available.
	 * 
	 * @param magnetic
	 *            the magnetic measurements (x, y, z).
	 * @param timeStamp
	 *            the time stamp of the measurement.
	 */
	public void onMagneticSensorChanged(float[] magnetic, long timeStamp);
}

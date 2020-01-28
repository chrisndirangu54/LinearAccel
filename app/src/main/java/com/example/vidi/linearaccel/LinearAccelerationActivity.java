package com.example.vidi.linearaccel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.vidi.linearaccel.filters.MeanFilter;
import com.example.vidi.linearaccel.sensor.AccelerationSensor;
import com.example.vidi.linearaccel.sensor.GravitySensor;
import com.example.vidi.linearaccel.sensor.GyroscopeSensor;
import com.example.vidi.linearaccel.sensor.LinearAccelerationSensor;
import com.example.vidi.linearaccel.sensor.MagneticSensor;
import com.example.vidi.linearaccel.sensor.StepCounterSensor;
import com.example.vidi.linearaccel.sensor.observer.AccelerationSensorObserver;
import com.example.vidi.linearaccel.sensor.observer.LinearAccelerationSensorObserver;
import com.example.vidi.linearaccel.sensor.observer.MagneticSensorObserver;
import com.example.vidi.linearaccel.sensor.observer.StepCounterSensorObserver;


public class LinearAccelerationActivity extends AppCompatActivity implements Runnable, LinearAccelerationSensorObserver,
        MagneticSensorObserver, AccelerationSensorObserver, StepCounterSensorObserver
{

	private static final String tag = LinearAccelerationActivity.class
			.getSimpleName();

	// Indicate if the output should be logged to a .csv file
	private boolean logData = false;

	// Outputs for the acceleration and LPFs
	private float[] linearAcceleration = new float[3];
    private float[] step = new float[3];
    private float[] magnetometer = new float[3];
    private float[] Acceleration = new float[3];
    private float[] mR = new float[9];
    private float[] mI = new float[9];
    private float[] mOrientation = new float[3];
    float azimuthInDegress = 0 ;

	// The generation of the log output
	private int generation = 0;

	// Log output time stamp
	private long logTime = 0;

	//Log output step
	private float logStep = 0;

	//acceleration variable
	private double accel = 0;

	private AccelerationSensor accelerationSensor;
	private GravitySensor gravitySensor;
	private GyroscopeSensor gyroscopeSensor;
	private MagneticSensor magneticSensor;
	private LinearAccelerationSensor linearAccelerationSensor;
    private StepCounterSensor stepCounterSensor;

    //add low pass filter
    private MeanFilter meanFilterMagnetic;
    private MeanFilter meanFilterAcceleration;

	// Output log
	private String log;

	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		linearAccelerationSensor = new LinearAccelerationSensor();
		accelerationSensor = new AccelerationSensor(this);
		gravitySensor = new GravitySensor(this);
		gyroscopeSensor = new GyroscopeSensor(this);
		magneticSensor = new MagneticSensor(this);
        stepCounterSensor = new StepCounterSensor(this);

        meanFilterMagnetic = new MeanFilter();
        meanFilterMagnetic.setWindowSize(10);

        meanFilterAcceleration = new MeanFilter();
        meanFilterAcceleration.setWindowSize(10);


		// Initialize the logger
        initStartButton();

		handler = new Handler();
	}

	@Override
	public void onPause()
	{
		super.onPause();
        accelerationSensor.removeAccelerationObserver(this);
        magneticSensor.removeMagneticObserver(this);
        stepCounterSensor.removeStepCounterObserver(this);
		accelerationSensor.removeAccelerationObserver(linearAccelerationSensor);
		gravitySensor.removeGravityObserver(linearAccelerationSensor);
		gyroscopeSensor.removeGyroscopeObserver(linearAccelerationSensor);
		magneticSensor.removeMagneticObserver(linearAccelerationSensor);
		linearAccelerationSensor.removeLinearAccelerationObserver(this);

		if (logData)
		{
			writeLogToFile();
		}

		handler.removeCallbacks(this);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		handler.post(this);

        accelerationSensor.registerAccelerationObserver(this);
        magneticSensor.registerMagneticObserver(this);
        stepCounterSensor.registerStepCounterObserver(this);
		accelerationSensor.registerAccelerationObserver(linearAccelerationSensor);
		gravitySensor.registerGravityObserver(linearAccelerationSensor);
		gyroscopeSensor.registerGyroscopeObserver(linearAccelerationSensor);
		magneticSensor.registerMagneticObserver(linearAccelerationSensor);
		linearAccelerationSensor.registerLinearAccelerationObserver(this);
	}

	@Override
	public void onLinearAccelerationSensorChanged(float[] linearAcceleration,
			long timeStamp)
	{
		// Get a local copy of the sensor values
		System.arraycopy(linearAcceleration, 0, this.linearAcceleration, 0,
				linearAcceleration.length);
	}

    @Override
    public void onStepCounterSensorChanged(float[] step,
                                                  long timeStamp)
    {
        // Get a local copy of the sensor values
        System.arraycopy(step, 0, this.step, 0,
                step.length);
    }

	@Override
	public void onMagneticSensorChanged(float[] magnetometer,
												  long timeStamp)
	{
		// Get a local copy of the sensor values
        System.arraycopy(Acceleration, 0, this.Acceleration, 0,
                Acceleration.length);
        this.Acceleration = meanFilterAcceleration.filterFloat(this.Acceleration);
		System.arraycopy(magnetometer, 0, this.magnetometer, 0,
				magnetometer.length);
        this.magnetometer = meanFilterAcceleration.filterFloat(this.magnetometer);
        if(SensorManager.getRotationMatrix(mR, mI, Acceleration, magnetometer))
        {
            float[] mR2 = new float[9];
            SensorManager.remapCoordinateSystem(mR,
                    SensorManager.AXIS_X, SensorManager.AXIS_Z,
                    mR2);
            SensorManager.getOrientation(mR2, mOrientation);
            float azimuthInRadians = mOrientation[0];
            azimuthInDegress = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;
        }
	}

    @Override
    public void onAccelerationSensorChanged(float[] Acceleration,
                                        long timeStamp)
    {
        // Get a local copy of the sensor values
        System.arraycopy(Acceleration, 0, this.Acceleration, 0,
                Acceleration.length);
        this.Acceleration = meanFilterAcceleration.filterFloat(this.Acceleration);
        System.arraycopy(magnetometer, 0, this.magnetometer, 0,
                magnetometer.length);
        this.magnetometer = meanFilterAcceleration.filterFloat(this.magnetometer);
        if(SensorManager.getRotationMatrix(mR, mI, Acceleration, magnetometer))
        {
            float[] mR2 = new float[9];
            SensorManager.remapCoordinateSystem(mR,
                    SensorManager.AXIS_X, SensorManager.AXIS_Z,
                    mR2);
            SensorManager.getOrientation(mR2, mOrientation);
            float azimuthInRadians = mOrientation[0];
            azimuthInDegress = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;
        }
    }

	@Override
	public void run()
	{
		handler.postDelayed(this, 100);

		logData();
	}

	/**
	 * Begin logging data to an external .csv file.
	 */
	private void startDataLog()
	{
		if (!logData)
		{
			CharSequence text = "Logging Data";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(this, text, duration);
			toast.show();

			String headers = "Generation" + ",";

			headers += "Timestamp" + ",";

			headers += "X,";

			headers += "Y,";

			headers += "Z,";

            headers += "XYZ,";

            headers += "MX,";

            headers += "MY,";

            headers += "MZ,";

            headers += "Degrees,";

            headers += "Steps,";

			headers += "Distance";

			headers += "Direction";

			log = headers + "\n";

			logData = true;
		}
		else
		{

			logData = false;
			writeLogToFile();
		}
	}

	/**
	 * Log output data to an external .csv file.
	 */
	private void logData()
	{
		if (logData)
		{
			if (generation == 0)
			{
				logTime = System.currentTimeMillis();
				logStep = step[0];
			}

			log += System.getProperty("line.separator");
			log += generation++ + ",";
			log += System.currentTimeMillis() - logTime + ",";

			log += linearAcceleration[0] + ",";
			log += linearAcceleration[1] + ",";
			log += linearAcceleration[2] + ",";
			accel = Math.sqrt((linearAcceleration[0]*linearAcceleration[0])+(linearAcceleration[1]*linearAcceleration[1])+(linearAcceleration[2]*linearAcceleration[2]));
			log += accel  + ",";
            log += magnetometer[0] + ",";
            log += magnetometer[1] + ",";
            log += magnetometer[2] + ",";
            log += azimuthInDegress + ",";

            log += step[0] + ",";

			//calculate distance in meter
			if(step[0] > logStep)
			{
				logStep = step[0];
				log += 0.5 * (Math.sqrt((linearAcceleration[0]*linearAcceleration[0])+(linearAcceleration[1]*linearAcceleration[1])+(linearAcceleration[2]*linearAcceleration[2]))) + ",";
			}
			else
			{
				log += 0.000001;
			}

			//put direction into clusters based on point of compass for filtering
			if(azimuthInDegress >= 337.5 && azimuthInDegress <= 22.5)
			{
				log += 0.000001;
			}
			else if(azimuthInDegress > 22.5 && azimuthInDegress < 67.5)
			{
				log += 45;
			}
			else if(azimuthInDegress >= 67.5 && azimuthInDegress <= 112.5)
			{
				log += 90;
			}
			else if(azimuthInDegress > 112.5 && azimuthInDegress < 157.5)
			{
				log += 135;
			}
			else if(azimuthInDegress >= 157.5 && azimuthInDegress <= 202.5)
			{
				log += 180;
			}
			else if(azimuthInDegress > 202.5 && azimuthInDegress < 247.5)
			{
				log += 225;
			}
			else if(azimuthInDegress >= 247.5 && azimuthInDegress <= 292.5)
			{
				log += 270;
			}
			else if(azimuthInDegress > 292.5 && azimuthInDegress < 337.5)
			{
				log += 315;
			}

		}
	}

	/**
	 * Write the logged data out to a persisted file.
	 */
	private void writeLogToFile()
	{
		Calendar c = Calendar.getInstance();
		String filename = "LinearAccel-" + c.get(Calendar.YEAR)
				+ "-" + c.get(Calendar.DAY_OF_WEEK_IN_MONTH) + "-"
				+ c.get(Calendar.DAY_OF_MONTH) + "-" + c.get(Calendar.HOUR) + "-"
				+ c.get(Calendar.MINUTE) + "-" + c.get(Calendar.SECOND)
				+ ".csv";

		File dir = new File(Environment.getExternalStorageDirectory()
				+ File.separator + "LinearAccel" + File.separator
				+ "Logs" + File.separator + "Acceleration");
		if (!dir.exists())
		{
			dir.mkdirs();
		}

		File file = new File(dir, filename);

		FileOutputStream fos;
		byte[] data = log.getBytes();
		try
		{
			fos = new FileOutputStream(file);
			fos.write(data);
			fos.flush();
			fos.close();

			CharSequence text = "Log Saved";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(this, text, duration);
			toast.show();
		}
		catch (FileNotFoundException e)
		{
			CharSequence text = e.toString();
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(this, text, duration);
			toast.show();
		}
		catch (IOException e)
		{
			// handle exception
		}
		finally
		{
			// Update the MediaStore so we can view the file without rebooting.
			// Note that it appears that the ACTION_MEDIA_MOUNTED approach is
			// now blocked for non-system apps on Android 4.4.
			MediaScannerConnection.scanFile(this, new String[]
			{ "file://" + Environment.getExternalStorageDirectory() }, null,
					new MediaScannerConnection.OnScanCompletedListener()
					{
						@Override
						public void onScanCompleted(final String path,
								final Uri uri)
						{
							Log.i(tag, String.format(
									"Scanned path %s -> URI = %s", path));
						}
					});
		}
	}

	private void initStartButton()
	{
		final Button button = (Button) findViewById(R.id.button_start);

		button.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if (!logData)
				{
					button.setBackgroundResource(R.drawable.stop_button_background);
					button.setText("Stop Log");
				}
				else
				{
					button.setBackgroundResource(R.drawable.start_button_background);
					button.setText("Start Log");
				}
                startDataLog();
			}
		});
	}
}
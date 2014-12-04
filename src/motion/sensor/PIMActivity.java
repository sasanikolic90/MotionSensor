package motion.sensor;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class PIMActivity extends Activity implements LocationListener {
	private static final String TAG = "PIMActivity";
	private Button launchButton;
	public static Context con;
	public static int heightP, widthP, rates;
	static Mail m;
	static String mail;
	SharedPreferences sharedPreferences;
	static LocationManager locationManager;
	static LocationListener listener;
	static double lat, lon;
	static String provider;
	static Criteria criteria;
	static float percentage, treshold;
	static int delay;
	static PowerManager.WakeLock wl;
	static PowerManager pm;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.meni, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item1:     
			Toast.makeText(this, "You pressed about!", Toast.LENGTH_LONG).show();
			break;
		case R.id.item2:    
			Intent i = new Intent(PIMActivity.this, Settings.class);
			startActivity(i);
			break;
		}
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		con = this;

		sharedPreferences = con.getSharedPreferences("MOTION", MODE_WORLD_READABLE);
		mail = sharedPreferences.getString("email", null);
		percentage = sharedPreferences.getFloat("percentage", 0)/100;
		treshold = sharedPreferences.getFloat("treshold", 0)*255/100;
		delay = sharedPreferences.getInt("delay", 0);

		if (mail == null || percentage <= 0|| treshold <= 0 || delay < 1) {
			Toast.makeText(PIMActivity.con, "Set all parameters.", Toast.LENGTH_LONG).show();
			Intent i = new Intent(PIMActivity.this, Settings.class);
			startActivity(i);
		}

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		Camera cam = Camera.open();
		List<Size> sizes = cam.getParameters().getSupportedPreviewSizes();

		for (int i = 0; i<sizes.size();i++) {
			if (sizes.get(i).height == 480 || sizes.get(i).width == 640)  {
				heightP = sizes.get(i).height;
				widthP = sizes.get(i).width;
				break;
			} else {
				heightP = sizes.get(i).height;
				widthP = sizes.get(i).width;
			}
		}
		//		heightP = sizes.get(0).height;
		//		widthP = sizes.get(0).width;
		rates = cam.getParameters().getSupportedPreviewFrameRates().get(0).intValue();

		Log.i("data", "Chosen size"+widthP+"x"+heightP);
		Log.i("data", "Supported sizes"+sizes.toString());

		cam.stopPreview();
		cam.release();

		m = new Mail("motionsensor0@gmail.com", "detectmotion");

		lat = 0;
		lon = 0;
		
		criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);

		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		String provider = locationManager.getBestProvider(criteria, true);
		locationManager.requestLocationUpdates(provider, 1000, 50, this);

		pm= (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MOTION_SENSOR");



		launchButton = (Button) findViewById(R.id.launch_button);

		launchButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				Log.i(TAG, "Starting CameraActivity");

				final Handler handler = new Handler(); 
				Timer t = new Timer(); 
				t.schedule(new TimerTask() { 

					@Override
					public void run() { 
						handler.post(new Runnable() { 
							public void run() { 
								Intent i = new Intent(PIMActivity.this, CameraActivity.class);
								startActivity(i);
								wl.acquire();
							} 
						}); 
					} 
				}, 4000); 

				//				sendEmail(v);
			}
		});
	}
	
	public static void sendEmail(Double lat, Double lon, String picture1, String picture2, String bigPicture){

		Log.i("mail", "Sending");

		Calendar c = Calendar.getInstance(); 
		System.out.println("Current time => "+c.getTime());

		String[] toArr = {mail}; 
		m.setTo(toArr); 
		m.setFrom("motionsensor0@gmail.com"); 
		m.setSubject("Motion Sensor");
		
		m.setBody("There was movement on "+c.getTime()+" at location "+lat+", "+lon+".\n\nThis was sent from Android application Motion Sensor."); 

		try {
			m.addAttachment(picture1);  
			m.addAttachment(picture2);
			m.addAttachment(bigPicture); 
			if(m.send()) {
				Toast.makeText(PIMActivity.con, "Email was sent successfully.", Toast.LENGTH_LONG).show();
				m = new Mail("motionsensor0@gmail.com", "detectmotion");
			} else {
				Toast.makeText(PIMActivity.con, "Email was not sent.", Toast.LENGTH_LONG).show();
			}
		} catch(Exception e) {
			Toast.makeText(PIMActivity.con, "There was a problem sending the email.", Toast.LENGTH_LONG).show();
		} 

		Log.i("mail", "sent");

	}

	public void onLocationChanged(Location location) {
		lat = location.getLatitude();
		lon = location.getLongitude();
	}

	public void onProviderDisabled(String provider) {
		String providers = locationManager.getBestProvider(criteria, true);
		locationManager.requestLocationUpdates(providers, 1000, 50, this);
	}

	public void onProviderEnabled(String provider) {
		String providers = locationManager.getBestProvider(criteria, true);
		locationManager.requestLocationUpdates(providers, 1000, 50, this);
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {}
}
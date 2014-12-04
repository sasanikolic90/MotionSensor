package motion.sensor;

import java.io.File;
import java.io.FileOutputStream;

import motion.sensor.image.AndroidImage;
import motion.sensor.image.AndroidImageFactory;
import motion.sensor.image.Size;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class MotionDetection {

	private static final String TAG = "MotionDetection";
	static String picture1 = "";
	static String picture2 = "";

	/* Datoteka za shranjevanje nastavitev */
	public static final String PREFS_NAME = "prefs_md";

	/* Nadzira mejno vrednost nad katero sta dva pixla razlicna
	 * 25 means 10% pixel difference 
	 * */
	private static final KeyValue<String,Integer> mPixelThreshold = 
			new KeyValue<String,Integer>("pim.md.pixel_threshold", ((int) PIMActivity.treshold));

	/* Nadzira mejno vrednost nad katero sta dve sliki razlicni 
	 * 9216 = 3% od 640x480 
	 * */
	private static final KeyValue<String,Integer> mThreshold = 
			new KeyValue<String,Integer>("pim.md.threshold", (int) (PIMActivity.widthP*PIMActivity.heightP*(PIMActivity.percentage)));

	/* erosion level  */
	private static final KeyValue<String,Integer> mErosionLevel = 
			new KeyValue<String,Integer>("pim.md.erosion_level", 10);

	/* Procenti pixlov nove slike, katere je treba združiti z ozadjem */
	private static final KeyValue<String,Integer> mMorphLevel = 
			new KeyValue<String, Integer>("pim.md.morph_level", 80);

	private static final KeyValue<String,Size<Integer,Integer>> mSize = 
			new KeyValue<String,Size<Integer,Integer>>("pim.md.size", 
					new Size<Integer,Integer>(PIMActivity.widthP,PIMActivity.heightP)); 

	/* Format predogleda */
	private static final KeyValue<String,Integer> mPixelFormat = 
			new KeyValue<String, Integer>("pim.md.pixel_format", AndroidImageFactory.IMAGE_FORMAT_NV21);

	// Slika ozadja
	private AndroidImage mBackground;

	// Slika za detekcijo gibanja
	private AndroidImage mAndroidImage;

	private SharedPreferences mPrefs;

	public MotionDetection(SharedPreferences prefs) {
		mPrefs = prefs;
		mPixelThreshold.value = mPrefs.getInt(mPixelThreshold.key, mPixelThreshold.value);
		mThreshold.value = mPrefs.getInt(mThreshold.key, mThreshold.value);
		mErosionLevel.value = mPrefs.getInt(mErosionLevel.key, mErosionLevel.value);
		mMorphLevel.value = mPrefs.getInt(mMorphLevel.key, mMorphLevel.value);
		mPixelFormat.value = mPrefs.getInt(mPixelFormat.key, mPixelFormat.value);
	}

	int counter;

	public boolean detect(byte[] data, Camera camera) {

		//		Log.i("data", light+" "+counter+""+light/counter);

		if(mBackground == null) {
			mBackground = AndroidImageFactory.createImage(data, mSize.value, 
					mPixelFormat.value);
			Log.i(TAG, "Creating background image");
			return false;
		}

		boolean motionDetected = false;

		String time = System.currentTimeMillis()+"";

		mAndroidImage = AndroidImageFactory.createImage(data, mSize.value, 
				mPixelFormat.value);

		motionDetected = mAndroidImage.isDifferent(mBackground, 
				mPixelThreshold.value, mThreshold.value);

		Log.i(TAG, "Image is different ? " + motionDetected);

		if (motionDetected) {

			String pictureNameBack = time+"-background.jpg";
			String pictureNameImage = time+"-front.jpg";

			//			Toast.makeText(PIMActivity.con, "lat "+PIMActivity.lat+" lon "+PIMActivity.lon+" "+mPixelThreshold.value+" "+mThreshold.value, Toast.LENGTH_SHORT).show();

			//			Vibrator vibrator = (Vibrator)PIMActivity.con.getSystemService(Context.VIBRATOR_SERVICE);
			//			vibrator.vibrate(300);

			try {
				Camera.Parameters parameters = camera.getParameters();
				android.hardware.Camera.Size size = parameters.getPreviewSize();
				YuvImage image1 = new YuvImage(mBackground.get(), ImageFormat.NV21,
						size.width, size.height, null);
				YuvImage image2 = new YuvImage(mAndroidImage.get(), ImageFormat.NV21,
						size.width, size.height, null);

				File root = Environment.getExternalStorageDirectory();
				if (root.canWrite()){
					
					File dir = new File(root+"/Pictures/pim/");
					if (!dir.exists())
						dir.mkdir();
					
					File gpxfile1 = new File(root+"/Pictures/pim/", pictureNameBack);
					File gpxfile2 = new File(root+"/Pictures/pim/", pictureNameImage);


					FileOutputStream filecon1 = new FileOutputStream(gpxfile1);
					FileOutputStream filecon2 = new FileOutputStream(gpxfile2);
					image1.compressToJpeg(new Rect(0, 0, image1.getWidth(), image1.getHeight()), 90, filecon1);
					image2.compressToJpeg(new Rect(0, 0, image2.getWidth(), image2.getHeight()), 90, filecon2);
					picture1 = gpxfile1.toString();
					picture2 = gpxfile2.toString();
				}

			} catch (Exception e) {
				Toast toast = Toast
						.makeText(PIMActivity.con, e.getMessage(), 1000);
				toast.show();
			}

			mBackground = null;

			//						Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			//						Ringtone r = RingtoneManager.getRingtone(PIMActivity.con.getApplicationContext(), notification);
			//						r.play();
		} else {
			mBackground = mAndroidImage;
		}
		return motionDetected;		
	}
}
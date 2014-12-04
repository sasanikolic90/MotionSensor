package motion.sensor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import motion.sensor.io.DataSink;
import motion.sensor.io.DataWriter;
import motion.sensor.io.FileUtils;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Preview extends SurfaceView implements SurfaceHolder.Callback {

	private static final String TAG = "CameraView";
	private static final String PREFS_NAME = "prefs_camera";
	private final static String MOTION_DETECTION_KEY = "motion_detection_active";

	private static final String FOCUS_MODE_CONTINUOS_VIDEO = "continuos-video";

	private SurfaceHolder mHolder;
	static Camera mCamera;
	private Context mContext;
	static boolean flash = true;

	private CameraCallback mCameraCallback;
	private boolean mMotionDetectionActive;
	public static boolean inTime = true;

	public Preview(Context context) {
		super(context);

		mContext = context;

		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		@SuppressWarnings("static-access")
		SharedPreferences prefs = mContext.getSharedPreferences(PREFS_NAME,
				mContext.MODE_PRIVATE);
		mMotionDetectionActive = prefs.getBoolean(MOTION_DETECTION_KEY, true);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(PIMActivity.widthP, PIMActivity.heightP);
		parameters.setRotation(90);
		parameters.setPreviewFrameRate(PIMActivity.rates);
		mCamera.setParameters(parameters);

		mCamera.startPreview();
	}

	public void surfaceCreated(SurfaceHolder holder) {
		mCamera = Camera.open();
		if(mCamera == null) // TODO show Toast
			throw new RuntimeException("Camera is null");

		configure(mCamera);

		if(mMotionDetectionActive) {
			mCameraCallback = new CameraCallback(mContext, mCamera);
			mCamera.setPreviewCallback(mCameraCallback);
			//						try {
			//							mCamera.setPreviewDisplay(holder);
			//						} catch (IOException exception) {
			//							Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
			//							closeCamera();
			//						}
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		closeCamera();
	}

	private void closeCamera() {
		Log.i(TAG, "Closing camera and freeing its resources");

		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	private void configure(Camera camera) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
		Camera.Parameters params = camera.getParameters();

		params.set("jpeg-quality", prefs.getInt("pim.image-quality", 75));

		// Nastavitve formata slike
		List<Integer> formats = params.getSupportedPictureFormats();
		if (formats.contains(PixelFormat.RGB_565))
			params.setPictureFormat(PixelFormat.RGB_565);
		else
			params.setPictureFormat(PixelFormat.JPEG);

		List<Size> sizes = params.getSupportedPreviewSizes();
		//		Log.i
		//		Camera.Size sizePreview = sizes.get(0);// sizes.get(sizes.size()-1);
		Camera.Size sizePicture = params.getSupportedPictureSizes().get(0); 
		// sizes.get(sizes.size()-1);
		params.setPictureSize(sizePicture.width, sizePicture.height);
		params.setPreviewSize(PIMActivity.widthP, PIMActivity.heightP);

		//		Log.i("data", "Supported preview sizes "+sizes.size());
		//		Log.i("data", "Supported preview size max "+sizePreview.width+"x"+sizePreview.height);
		//		Log.i("data", "Supported preview size min "+sizes.get(sizes.size()-1).width+"x"+sizes.get(sizes.size()-1).height);
		//		Log.i("data", "Supported picture size "+sizePicture.width+"x"+sizePicture.height);
 

		List<String> flashModes = params.getSupportedFlashModes();

		//		Log.i("preview", flashModes.size()+"");

		if (flashModes != null && flashModes.size() > 0)
			params.setFlashMode(prefs.getString("pim.camera.flash",
					Camera.Parameters.FLASH_MODE_OFF));
		else
			flash = false;

		if (params.getSupportedFocusModes()
				.contains(FOCUS_MODE_CONTINUOS_VIDEO))
			params.setFocusMode(prefs.getString("pim.camera.focus_mode",
					FOCUS_MODE_CONTINUOS_VIDEO));
		else
			params.setFocusMode(prefs.getString("pim.camera.focus_mode",
					Camera.Parameters.FOCUS_MODE_INFINITY));

		camera.setParameters(params);
	}
}

final class CameraCallback implements Camera.PreviewCallback, 
Camera.PictureCallback {

	private final String PICTURE_PREFIX = "/Pictures/pim/";
	private static final int PICTURE_DELAY = 4000;

	private static final String TAG = "CameraCallback";
	private MotionDetection mMotionDetection;
	static Camera mCamera;
	static CameraCallback listenerer;

	private long mReferenceTime = (4000);
	private DataWriter mDataWriter;
	int night = 1;

	public CameraCallback(Context ct, Camera camera) {
		mDataWriter = new DataWriter();

		listenerer = this;
		mCamera = camera;

		mMotionDetection = new MotionDetection(ct.getSharedPreferences(
				MotionDetection.PREFS_NAME, Context.MODE_PRIVATE));
	}

	public void onPictureTaken(byte[] data, Camera camera) {
		Log.i(TAG, "Picture Taken");

		String pictureName = PICTURE_PREFIX+System.currentTimeMillis()+"-bigPicture.jpg";
		File root = Environment.getExternalStorageDirectory();
		File f = new File(root+"/Pictures/pim/", pictureName);
		if (root.canWrite()) {
			//		File f = new File(
			//				Environment.getExternalStorageDirectory(),pictureName);
			FileOutputStream fos = null;
			try {
				FileUtils.touch(f);
				fos = new FileOutputStream(f);
			} catch (IOException e) {
				Log.e(TAG, "Cannot write picture to disk");
				e.printStackTrace();
			}

			DataSink<FileOutputStream>df = new DataSink<FileOutputStream>(data,fos);
			mDataWriter.writeAsync(df);
		}
		PIMActivity.sendEmail(PIMActivity.lat, PIMActivity.lon, MotionDetection.picture1,MotionDetection.picture2, f.toString());
		//				PIMActivity.sendEmail(PIMActivity.lat, PIMActivity.lon, f.toString());

		final Handler handler = new Handler(); 
		Timer t = new Timer(); 
		t.schedule(new TimerTask() { 

			@Override
			public void run() { 
				handler.post(new Runnable() { 
					public void run() { 
						Log.i("mail", "StartPreview");
						if (CameraActivity.preview)
							mCamera.startPreview();
					} 
				}); 
			} 
		}, 4000); 

	}

	static int time = 1;
	int counter;

	public void onPreviewFrame(byte[] data, Camera camera) {

		//		int i = 0;
		//		int light = 0;
		//
		//		while (i<data.length) {
		//			light += data[i];
		//			i++;
		//		}
		//
		//		Calendar c = Calendar.getInstance(); 
		//		if (Preview.flash && (c.get(Calendar.HOUR_OF_DAY) < 21 ||c.get(Calendar.HOUR_OF_DAY) > 6) && (light/i == (-42) || light/i == (-35))) {
		//			if (counter > 100) {
		//				Parameters p = Preview.mCamera.getParameters();
		//				p.setFlashMode(Parameters.FLASH_MODE_TORCH);
		//				Preview.mCamera.setParameters(p);
		//				Log.i("data", "flash on");
		//				final Handler handler = new Handler(); 
		//				Timer t = new Timer(); 
		//				t.schedule(new TimerTask() { 
		//
		//					public void run() { 
		//						handler.post(new Runnable() { 
		//							public void run() { 
		//								Parameters p = Preview.mCamera.getParameters();
		//								p.setFlashMode(Parameters.FLASH_MODE_OFF);
		//								Preview.mCamera.setParameters(p);
		//								counter=0;
		//								Log.i("data", "flash off");
		//							} 
		//						}); 
		//					} 
		//				}, 4000); 
		//			}
		//			counter++;
		//		} else {
		//			//			Parameters p = Preview.mCamera.getParameters();
		//			//			p.setFlashMode(Parameters.FLASH_MODE_OFF);
		//			//			Preview.mCamera.setParameters(p);
		//		}
		//
		//		Log.i("data", light+" "+i+""+light/i);

		//		Log.i("data", data[0]+" "+data[1]);

		//		String picture = "";

		//				for (int i = 0; i<20;i++) {
		//					picture += data[i]+" ";
		//					Log.i("data", i+" "+data[i]);
		//				}
		//				picture += "end";

		//		Calendar c = Calendar.getInstance();
		//		Log.i("data", camera.getParameters().getPreviewSize().height+"*"+camera.getParameters().getPreviewSize().width+" "+data.length+" "+picture);
		Log.i("data", time+"");
		//				if (c.get(Calendar.HOUR_OF_DAY) > 19) {
		//					night = 300;
		//					Parameters p = Preview.mCamera.getParameters();
		//					p.setFlashMode(Parameters.FLASH_MODE_OFF);
		//					Preview.mCamera.setParameters(p);
		//				} else {
		//					night = 1;
		//					Parameters p = Preview.mCamera.getParameters();
		//					p.setFlashMode(Parameters.FLASH_MODE_OFF);
		//					Preview.mCamera.setParameters(p);
		//				}

		if (time > PIMActivity.delay*night*camera.getParameters().getPreviewFrameRate() && mMotionDetection.detect(data, camera)) {
			// ta delay je potreben, da ne bi prišlo do zajemanja slik medtem ko se druga že zajema 
			long now = System.currentTimeMillis();
			if (now > mReferenceTime + PICTURE_DELAY) {
				mReferenceTime = now + PICTURE_DELAY;
				Log.i(TAG, "Taking picture");

				//				if (Preview.flash && c.get(Calendar.HOUR_OF_DAY) < 6) {
				//					Parameters p = camera.getParameters();
				//					p.setFlashMode(Parameters.FLASH_MODE_TORCH);
				//					camera.setParameters(p);
				//				}
				camera.takePicture(null , null, this);
				time = 0;
			} else {
				Log.i(TAG, "Not taking picture because not enough time has "
						+ "passed since the creation of the Surface");
			}
		}
		time++;

	}
}
package motion.sensor;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

public class CameraActivity extends Activity {

	/* Obmoèje za preview */
	private SurfaceView mCameraView;
	Context con;
	static boolean preview = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		con = this;

		// Skrij naslov aplikacije in zaženi full-screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		mCameraView= new Preview(this);
		setContentView(mCameraView);
	}

	@Override
	public void onBackPressed() {
		if (Preview.mCamera != null) {
			Preview.mCamera.stopPreview();
			Preview.mCamera.release();
			Preview.mCamera = null;
		}
		preview = false;
		PIMActivity.wl.release();
		Log.v("Provider", "back pressed");
		finish();
	}

}
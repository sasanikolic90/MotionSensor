package motion.sensor.image;

import android.graphics.PixelFormat;
import android.util.Log;

public final class AndroidImageFactory {

	public static final int IMAGE_FORMAT_NV21 = 0x00000011;
	
	public static final int IMAGE_FORMAT_YUV2 = 0x00000014;
	
	public static final int IMAGE_FORMAT_NV16 = 0x00000010;

	private static final String TAG = "AndroidImageFactory";
			
	public static AndroidImage createImage(byte[] data, Size<Integer, Integer> size, int format) {
		AndroidImage im = null;
		
		String imgType = "";
		
		switch (format) {
		case IMAGE_FORMAT_NV21:
			im = new AndroidImage_NV21(data,size);
			imgType = "NV_21";
			break;
		case IMAGE_FORMAT_YUV2:
			imgType = "YUV2";
			break;
		case IMAGE_FORMAT_NV16:
			imgType = "NV_16";
			break;
		case PixelFormat.RGB_565:
			imgType = "RGB_565";
			break;
		case PixelFormat.JPEG:
			imgType = "JPEG";
			break;
		case PixelFormat.RGBA_5551:
			imgType = "RGBA_5551";
			break;
		case PixelFormat.RGB_888:
			imgType = "RGB_888";
			break;
		default:
			imgType = "default";
			break;
		}
		
		Log.i(TAG, "Creating an image of type: " + imgType );
		
		return im;
	}
}
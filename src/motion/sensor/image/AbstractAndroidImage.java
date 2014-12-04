package motion.sensor.image;

import android.util.Log;


public abstract class AbstractAndroidImage implements AndroidImage {
	
	private final String TAG = "AbstractAndroidImage";
	protected byte[] mData;
	protected Size<Integer, Integer> mSize;

	public AbstractAndroidImage(byte[] data, Size<Integer, Integer> size) {
		mData = data;
		mSize = size;
	}

	protected boolean assertImage(AndroidImage other) {
		boolean result = true;
		
		byte[] otherData = other.get();
		
		if(mData.length != otherData.length) {
			Log.e(TAG, "Data length between images to compare is different");
			result = false;
		}
		
		otherData = null;
		
		Log.d(TAG, "Images are compatible: " + result);
		
		return result;
	}
	
	public abstract AndroidImage toGrayscale();

	public AndroidImage erode(int erosionLevel) {
		return this;
	}

	public AndroidImage morph(AndroidImage other, int value) {
		Log.v(TAG, "Beginning morph operation with value: " + value);
		
		byte[] otherData = other.get();

		assert value <= 100 && value >= 0;
		
		if(value == 0) return this;
		
		float thisValue = (100 - value) / 100; 
		float otherValue = value / 100;
		
		for (int i = 0; i < mData.length; i++) {
			mData[i] = (byte) Math.round((mData[i] * thisValue) + (otherData[i] * otherValue));
		}
		
		return this;
	}
	
	public byte[] get() {
		return mData;
	}
}

package motion.sensor.image;

public final class Size<T extends Number,V extends Number> {
	public T mWidth;
	public V mHeight;

	public Size(T width, V height) {
		mWidth = width;
		mHeight = height;
	}
}
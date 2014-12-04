package motion.sensor.image;

public interface AndroidImage {

	public abstract AndroidImage toGrayscale();

	public abstract AndroidImage erode(int erosionLevel);
	
	public abstract AndroidImage morph(AndroidImage background, int value);

	public abstract boolean isDifferent(AndroidImage background, int pixel_threshold, 
			int threshold);
	
	public abstract byte[] get();

}
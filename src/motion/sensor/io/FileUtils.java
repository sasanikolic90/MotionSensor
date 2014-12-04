package motion.sensor.io;

import java.io.File;
import java.io.IOException;

public class FileUtils {

	public static void touch(File file) throws IOException {
		if(!file.exists()) {
			File parent = file.getParentFile();
			if(parent != null) 
				if(!parent.exists())
					if(!parent.mkdirs())
						throw new IOException("Cannot create parent directories for file: " + file);
			
				file.createNewFile();
		}
		
		boolean success = file.setLastModified(System.currentTimeMillis());
        if (!success)
            throw new IOException("Unable to set the last modification time for " + file);
	}
	
	public static void touch(String path) throws IOException {
		touch(new File(path));
	}
}

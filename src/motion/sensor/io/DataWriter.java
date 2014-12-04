package motion.sensor.io;

import java.util.ArrayList;
import java.util.List;

import motion.sensor.os.AsyncTaskListener;
import motion.sensor.os.StreamDataTask;

import android.util.Log;

@SuppressWarnings("rawtypes")
public class DataWriter {

	private static final String TAG = DataWriter.class.getName();

	@SuppressWarnings({ "unchecked" })
	public void writeAsync(AsyncTaskListener<Integer, Long> listener, 
			DataSink ds, boolean flushOnWrite, boolean closeOnWrite) {
		
		List<AsyncTaskListener<Integer, Long>> listeners = 
			new ArrayList<AsyncTaskListener<Integer,Long>>(1);
		if(listener != null)
			listeners.add(listener);
		
		Log.d(TAG, "Writing asynchronously to " + ds.getSink().toString());
		
		new StreamDataTask(listeners, flushOnWrite, closeOnWrite).execute(ds);
	}
	
	public void writeAsync(DataSink ds) {
		writeAsync(null, ds, true, true);
	}
}

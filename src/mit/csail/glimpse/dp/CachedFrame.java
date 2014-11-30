package mit.csail.glimpse.dp;

import org.opencv.core.Mat;

public class CachedFrame {

	
	Mat frame;
	long timeStamp;
	
	public CachedFrame(Mat frame, long timeStamp){
		this.frame = frame;
		this.timeStamp = timeStamp;
	}
	
}

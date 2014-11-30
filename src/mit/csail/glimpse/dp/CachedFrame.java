package mit.csail.glimpse.dp;

import org.opencv.core.Mat;

public class CachedFrame {

	
	Mat frame;
	long timeStamp;
	int diff;
	
	public CachedFrame(Mat frame, long timeStamp, int diff){
		this.frame = frame;
		this.timeStamp = timeStamp;
		this.diff = diff;
	}
	
}

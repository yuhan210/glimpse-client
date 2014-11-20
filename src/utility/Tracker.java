package utility;


import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.video.Video;

public class Tracker {
	Rect faceRect;
	List<org.opencv.core.Point> prevPoints;
	public FaceClass fc;
	public Mat prevFrame;

	public Tracker(){
		prevFrame = new Mat();
		prevPoints = new ArrayList<Point>();
		faceRect = new Rect();
		fc = new FaceClass();
	}
	public void init(Mat curFrame, FaceClass fc){
		prevPoints = new ArrayList<Point>(fc.featuresPts);
		prevPoints.add(new Point(fc.faceRect.x, fc.faceRect.y));
		prevPoints.add(new Point(fc.faceRect.x + fc.faceRect.width, fc.faceRect.y));
		prevPoints.add(new Point(fc.faceRect.x, fc.faceRect.y + fc.faceRect.height));
		prevPoints.add(new Point(fc.faceRect.x + fc.faceRect.width, fc.faceRect.y + fc.faceRect.height));
		prevPoints.add(new Point(fc.centroid.x, fc.centroid.y));
		
		//prevFrame = curFrame.clone();
	}
	
	public void track(Mat curFrame, Rect faceRect){
		MatOfPoint2f nextPts = new MatOfPoint2f();	
		MatOfPoint2f prevPts = new MatOfPoint2f();
		prevPts.fromList(prevPoints);
		MatOfByte status = new MatOfByte();
		MatOfFloat err = new MatOfFloat();
		Size winSize = new Size(31,31);
		TermCriteria criteria = new TermCriteria(TermCriteria.MAX_ITER|TermCriteria.EPS, 20, 0.03);
		
		Video.calcOpticalFlowPyrLK(prevFrame, curFrame, 
				prevPts, nextPts, 
				status, err, winSize, 
				3, criteria, 
				0, 0.001);
		this.faceRect = faceRect;
		/**
		for (int i = 0; i < nextPts.size().width; ++i){
			if 
		}
		**/
	}
	
}

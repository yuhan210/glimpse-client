package mit.csail.glimpse.utility;


import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.video.Video;

public class Tracking {

	public static ObjectClass run(Mat prevFrame, Mat curFrame, ObjectClass oc){
		
		MatOfPoint2f nextPts = new MatOfPoint2f();	
		MatOfPoint2f prevPts = new MatOfPoint2f();
		MatOfByte status = new MatOfByte();
		MatOfFloat err = new MatOfFloat();
		Size winSize = new Size(31,31);
		TermCriteria criteria = new TermCriteria(TermCriteria.MAX_ITER|TermCriteria.EPS, 5, 0.03);
		
		prevPts.fromList(oc.featuresPts);
		long t = System.nanoTime();	
		Video.calcOpticalFlowPyrLK(prevFrame, curFrame, 
				prevPts, nextPts, 
				status, err, winSize, 
				3, criteria, 
				0, 0.001);
		double execTime = (System.nanoTime() - t)/ 1000000.0;
		System.out.println("execTime:" + execTime );
		List<Point> pts = nextPts.toList();
		
		oc.featuresPts = pts;
		
		return oc;
		
	}
	
	public static double run(ObjectClass oc, Mat prevFrame, Mat curFrame){
		
		MatOfPoint2f nextPts = new MatOfPoint2f();	
		MatOfPoint2f prevPts = new MatOfPoint2f();
		MatOfByte status = new MatOfByte();
		MatOfFloat err = new MatOfFloat();
		Size winSize = new Size(31,31);
		TermCriteria criteria = new TermCriteria(TermCriteria.MAX_ITER|TermCriteria.EPS, 5, 0.03);
		
		prevPts.fromList(oc.featuresPts);
		long t = System.nanoTime();	
		Video.calcOpticalFlowPyrLK(prevFrame, curFrame, 
				prevPts, nextPts, 
				status, err, winSize, 
				3, criteria, 
				0, 0.001);
		double execTime = (System.nanoTime() - t)/ 1000000.0;
		//System.out.println("execTime:" + execTime );
		List<Point> pts = nextPts.toList();
		
		oc.featuresPts = pts;
		
		return execTime;
		
	}
	public static boolean doesTrackingWork(ObjectClass oc){
		if (oc.featuresPts.size() < 4){
			return false;
		}else if(oc.faceRect.width < 25 || oc.faceRect.height < 25){
			return false;
		}
		return true;
	}
}

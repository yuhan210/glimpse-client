package nwkHelper;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import utility.FaceClass;
import utility.Global;



public class NwkResponse {
	public Mat frame;
	int shift_x;
	int shift_y;
	public List<FaceClass> faces = new ArrayList<FaceClass>();
	
	public void parseResponse(String response){
		faces.clear();
		
		String[] segs = response.split(":");
		
		if (segs.length == 0){
			System.out.println("Bad string.." + response);
		}
		
		int faceNumber = Integer.parseInt(segs[0]);
		
		for (int i = 4; i < (4 + faceNumber); ++i){
			
			String[] probStr = segs[i].split(";");
			String[] faceSegs = probStr[0].split(",");
			
			int label = Integer.parseInt(faceSegs[1]);
			int left = Integer.parseInt(faceSegs[2]);
			int top = Integer.parseInt(faceSegs[3]);
			int width = Integer.parseInt(faceSegs[4]);
			int height = Integer.parseInt(faceSegs[5]);
			double confidence = Double.parseDouble((faceSegs[6]));
			
			int featureNum = Integer.parseInt(faceSegs[8]);
			List<org.opencv.core.Point> featurePoints = new ArrayList<org.opencv.core.Point>();
			
			for (int j = 0; j < featureNum; ++j){
				int index = (2*j) + 9;
				featurePoints.add(new Point(Integer.parseInt(faceSegs[index]), Integer.parseInt(faceSegs[index+1])));
			}
			
			
			String[] probSegs = probStr[1].split(",");
			
			double[] probEstimate = new double[Global.CLASS_NUMBER+1];
			
			
			for (int j = 0; j < 5; ++j){
				int index = 2 * j;
				int l = Integer.parseInt(probSegs[index]);
				double p = Double.parseDouble(probSegs[index + 1]);
				probEstimate[l] = p;
			}
			
			FaceClass face = new FaceClass(label, left, top, width, height, featurePoints, probEstimate);
			faces.add(face);
		}

	}
}
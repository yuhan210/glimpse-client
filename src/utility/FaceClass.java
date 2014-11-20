package utility;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.opencv.core.Rect;
import android.annotation.SuppressLint;
import android.graphics.Point;


@SuppressLint("NewApi")
public class FaceClass{
	public Rect faceRect;
	public int label;
	public Point centroid = new Point();
	public double[] EWMA;
	List<org.opencv.core.Point> featuresPts = new ArrayList<org.opencv.core.Point>();
	
	public FaceClass() {
		faceRect = new Rect();
		EWMA = new double[Global.CLASS_NUMBER];
	}
	
	
	FaceClass(int _label, int x, int y, int width, int height, List<org.opencv.core.Point> _featuresPts, double[] _EWMA){
		this.label = _label;
		faceRect = new Rect(x,y, width, height);
		centroid.x = x + width/2;
		centroid.y = y + height/2;
		featuresPts = new ArrayList<org.opencv.core.Point>(_featuresPts);
		EWMA = Arrays.copyOf(_EWMA, _EWMA.length);
	}
	
	public void init(int _label, int x, int y, int width, int height, List<org.opencv.core.Point> _featuresPts, double[] _EWMA){
		this.label = _label;
		faceRect = new Rect(x,y, width, height);
		centroid.x = x + width/2;
		centroid.y = y + height/2;
		featuresPts = new ArrayList<org.opencv.core.Point>(_featuresPts);
		EWMA = Arrays.copyOf(_EWMA, _EWMA.length);
	}
}
package mit.csail.glimpse.utility;

import org.opencv.core.Core;
import org.opencv.core.Mat;

public class FrameDifferencing {

	public static int getDiffSize(Mat a, Mat b){
		
		Mat diff = new Mat (a.rows(), a.cols(), a.type());
		Core.absdiff(a, b, diff);
		int size = (int) a.total() * a.channels();
		byte[] buf = new byte[size];
		diff.get(0,0,buf);
		
		int d = 0;
		for (int i = 0; i < a.height(); ++i){
			for (int j = 0; j < a.width(); ++j){
				int ind = i * a.height() + j;
				if (buf[ind] > 35){
					++d;
				}
			}
		}
		return d;
	}
}

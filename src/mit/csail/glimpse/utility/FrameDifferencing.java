package mit.csail.glimpse.utility;

import org.opencv.core.Core;
import org.opencv.core.Mat;

public class FrameDifferencing {

	public static int getDiffSize(Mat a, Mat b){
		
		Mat diff = new Mat (a.rows(), a.cols(), a.type());
		Core.absdiff(a, b, diff);
		//int size = (int) a.total() * a.channels();
		//int size = 640 * 480;
		//byte[] buf = new byte[size];
		/**
		int height = a.height();
		int width = a.width();
		byte[] a_buf = new byte[size];
		byte[] b_buf = new byte[size];
		a.get(0, 0, a_buf);
		b.get(0, 0, b_buf);
		int d = 0;
		for (int i = 0; i < height; ++i){
			for (int j = 0; j < width; ++j){
				int ind = i * height + j;
				if (a_buf[ind] - b_buf[ind] > 35 || b_buf[ind] - a_buf[ind] > 35){
					++d;
				}
			}
		}
		**/
		return 0;
	}
}

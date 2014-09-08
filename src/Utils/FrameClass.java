package Utils;

import java.util.ArrayList;

public class FrameClass{
	public int frameNum;
	
	public ArrayList<FaceClass> faces;
	FrameClass(int _frameNum){
		this.frameNum = _frameNum;
		faces = new ArrayList<FaceClass>();
	}
	
}

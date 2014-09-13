package Utils;

import java.util.ArrayList;

public class FrameClass{
	public int frameNum;
	public long timeStamp;
	public ArrayList<FaceClass> faces;
	
	FrameClass(int _frameNum, long timeStamp){
		this.frameNum = _frameNum;
		this.timeStamp = timeStamp;
		faces = new ArrayList<FaceClass>();
	}
	
	public void push(FaceClass fc){
		faces.add(fc);		
	}
	
	public int getObjNum(){
		return faces.size();
	}
}

package mit.csail.glimpse.utility;

import java.util.ArrayList;

public class FrameClass{
	public long timeStamp;
	public ArrayList<ObjectClass> objects;
	
	public FrameClass(){
		this.timeStamp = -1;
		objects = new ArrayList<ObjectClass>();
	}
	
	public FrameClass(int _frameNum, long timeStamp){
		this.timeStamp = timeStamp;
		objects = new ArrayList<ObjectClass>();
	}
	
	public void push(ObjectClass oc){
		objects.add(oc);		
	}
	
	public int getObjNum(){
		return objects.size();
	}
}

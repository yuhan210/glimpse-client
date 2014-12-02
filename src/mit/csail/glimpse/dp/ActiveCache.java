package mit.csail.glimpse.dp;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;

import mit.csail.glimpse.utility.FrameClass;
import mit.csail.glimpse.utility.Tracking;
import android.os.Build;

public class ActiveCache {

	List<CachedFrame> activeCache = new ArrayList<CachedFrame>();
	
	public void add(CachedFrame cf){
		activeCache.add(cf);
	}

	public void clear(){
		activeCache.clear();
	}
	
	// run this in a separate thread
	public FrameClass subsample(FrameClass response){
		List<Integer> diffs = new ArrayList<Integer>();
		for (int i = 0; i < activeCache.size(); ++i){
			diffs.add(activeCache.get(i).diff);
		}
		int l = getFrameNumber();
		List<Integer> ind = DPFrameSelection.run(diffs, l, activeCache.size());
		for (int i = 0; i < ind.size()-1; ++i){
			// iterate through subsampled frames
			Mat curFrame = activeCache.get(i).frame;
			Mat nextFrame = activeCache.get(i+1).frame;
			for (int j = 0; j < response.objects.size(); ++j){
				response.objects.set(j, Tracking.run(curFrame, nextFrame, response.objects.get(j)));
			}
		}
		return response;
	}
	
	public int getFrameNumber(){
		int cachedFrameNum = activeCache.size();
		int l = cachedFrameNum/3;
		if (isRunningOnGlass()){
			l = cachedFrameNum/10;
		}
		return l;
	}
	
	/** Determine whethe the code is runnong on Google Glass
	 * @return True if and only if Manufacturer is Google and Model begins with Glass
	 */
	private boolean isRunningOnGlass() {
	    boolean result;
	    result = "Google".equalsIgnoreCase(Build.MANUFACTURER) && Build.MODEL.startsWith("Glass");	   
	    return result;
	}
}

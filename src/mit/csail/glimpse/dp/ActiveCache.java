package mit.csail.glimpse.dp;

import java.util.ArrayList;
import java.util.List;

public class ActiveCache {

	List<CachedFrame> activeCache = new ArrayList<CachedFrame>();
	
	public void add(CachedFrame cf){
		activeCache.add(cf);
	}

	public void clear(){
		activeCache.clear();
	}
	
}

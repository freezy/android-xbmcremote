package org.xbmc.android.remote.business;

import org.xbmc.android.util.ImportUtilities;

public class CacheManager {
	private static CacheManager instance;
	
	private CacheManager() {		
	}
	
	public static CacheManager get() {
		if(instance == null) {
			instance = new CacheManager();
		}
		
		return instance;
	}
	
	public void purgeCache() {
		ImportUtilities.purgeCache();
		MemCacheThread.purgeCache();
	}
}

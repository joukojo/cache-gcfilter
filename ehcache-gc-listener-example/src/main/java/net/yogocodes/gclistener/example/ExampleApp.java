package net.yogocodes.gclistener.example;

import java.util.HashMap;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.yogocodes.gclistener.ehcache.EhCacheGCListener;

public class ExampleApp {

	public static void main(String[] args) throws MalformedObjectNameException, InstanceNotFoundException {

		// enable the GC listening.
		EhCacheGCListener.register();

		/*
			Following code is just to get the Full GC running. 
			Nothing else. 
		*/
		final CacheManager cacheManager = CacheManager.create();
		cacheManager.addCache("exampleCache");
		final Cache cache = cacheManager.getCache("exampleCache");

		for (int j = 0; j < 50000; j++) {
			long start = System.currentTimeMillis(); 
			
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
			for (int i = 0; i < 9500000; i++) {
				Element element = new Element(Integer.valueOf(i), Integer.valueOf(i));
				cache.put(element );
				map.put(i, i);
			}
			long delta = System.currentTimeMillis() - start;  
			
			System.out.println("took " + delta + "msecs");

		}

	}

}

package net.yogocodes.gclistener.ehcache;

import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import com.sun.management.GarbageCollectionNotificationInfo;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// net.yogocodes.gclistener.ehcache.EhCacheGCListener
public class EhCacheGCListener implements NotificationListener {

	private final Logger logger = LoggerFactory
			.getLogger(EhCacheGCListener.class);

	private boolean analyzeMemoryConsumption = false;

	public void handleNotification(Notification notification, Object handback) {

		String notificationType = notification.getType();

		logger.trace("notificationType: {}", notificationType);

		if ("com.sun.management.gc.notification".equals(notificationType)) {
			// retrieve the garbage collection notification information

			CompositeData cd = (CompositeData) notification.getUserData();

			GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo
					.from(cd);

			logger.trace("{} : {} : {}", info.getGcName(), info.getGcAction(),
					info.getGcCause());

			if ("end of major GC".equals(info.getGcAction())) {
				List<CacheManager> cacheManagers = CacheManager.ALL_CACHE_MANAGERS;

				for (CacheManager cacheManager : cacheManagers) {
					String[] cacheNames = cacheManager.getCacheNames();
					for (String cacheName : cacheNames) {
						Cache cache = cacheManager.getCache(cacheName);
						if (isAnalyzeMemoryConsumption()) {
							long calculateInMemorySize = cache
									.calculateInMemorySize();
							logger.info("{} : {} bytes [{}Mb]", cacheName,
									calculateInMemorySize, (calculateInMemorySize/1024/1024));
						} else {
							logger.info(cache.toString());
						}

					}
				}

			}
		}

	}

	public static void register() throws MalformedObjectNameException,
			InstanceNotFoundException {
		EhCacheGCListener listener = new EhCacheGCListener();
		
		boolean analyzeMemoryConsumption = System.getProperty("gc.listener.analyze.memory") != null;
		listener.setAnalyzeMemoryConsumption(analyzeMemoryConsumption);
		
		ObjectName gcName = new ObjectName(
				ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*");
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		for (ObjectName name : server.queryNames(gcName, null)){
			server.addNotificationListener(name, listener, null, null);
		}
	}

	public boolean isAnalyzeMemoryConsumption() {
		return analyzeMemoryConsumption;
	}

	public void setAnalyzeMemoryConsumption(boolean analyzeMemoryConsumption) {
		this.analyzeMemoryConsumption = analyzeMemoryConsumption;
	}

}

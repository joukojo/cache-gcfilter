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

	private static final Logger LOGGER = LoggerFactory
			.getLogger(EhCacheGCListener.class);

	private boolean isGcLoopDetectionEnabled=false; 
	private boolean analyzeMemoryConsumption = false;
	private long fullGCPeriodicTime = 0L; 
	private long lastFullGCTime = 0L;

	public void handleNotification(Notification notification, Object handback) {

		String notificationType = notification.getType();

		LOGGER.trace("notificationType: {}", notificationType);

		if ("com.sun.management.gc.notification".equals(notificationType)) {
			// retrieve the garbage collection notification information

			final CompositeData cd = (CompositeData) notification.getUserData();

			final GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo
					.from(cd);

			LOGGER.trace("{} : {} : {}", info.getGcName(), info.getGcAction(),
					info.getGcCause());


			if ("end of major GC".equals(info.getGcAction())) {

				if( isGcLoopDetectionEnabled ) {
					long now = System.currentTimeMillis();
					long delta = (now - lastFullGCTime) ;
					boolean isOnFullGCLoop = delta < fullGCPeriodicTime; 

					lastFullGCTime = System.currentTimeMillis();
					if( !isOnFullGCLoop ) {
						LOGGER.trace("the last full gc was {} msecs ago, so ignoring this full gc", delta);
						return; 
					}
				}

				List<CacheManager> cacheManagers = CacheManager.ALL_CACHE_MANAGERS;

				for (final CacheManager cacheManager : cacheManagers) {
					final String[] cacheNames = cacheManager.getCacheNames();
					for (final String cacheName : cacheNames) {
						final Cache cache = cacheManager.getCache(cacheName);
						if (isAnalyzeMemoryConsumption()) {
							final long calculateInMemorySize = cache
									.calculateInMemorySize();
							LOGGER.info("{} : {} bytes [{}Mb]", cacheName,
									calculateInMemorySize, (calculateInMemorySize/1024/1024));
						} else {
							LOGGER.info(cache.toString());
						}

					}
				}

			}
		}

	}

	public static void register() throws MalformedObjectNameException,
			InstanceNotFoundException {
		
		boolean isListeningDisabled = "true".equals(System.getProperty("gc.listener.disable"));
		if( isListeningDisabled ) {
			return;
		}

		boolean isGcLoopDetectionEnabled = "true".equals(System.getProperty("gc.listener.loop.detect"));

		final EhCacheGCListener listener = new EhCacheGCListener();
		final boolean analyzeMemoryConsumption = System.getProperty("gc.listener.analyze.memory") != null;
		listener.setAnalyzeMemoryConsumption(analyzeMemoryConsumption);
		listener.isGcLoopDetectionEnabled = isGcLoopDetectionEnabled; 
		if( isGcLoopDetectionEnabled ) {
			try {
			listener.fullGCPeriodicTime = Long.valueOf(System.getProperty("gc.listener.loop.time"));
			}
			catch( NumberFormatException nfe ) {
				LOGGER.warn("check the gc.listener.loop.time value {}", System.getProperty("gc.listener.loop.time") );
			}
		}
		final ObjectName gcName = new ObjectName(
				ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*");
		final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		for (final ObjectName name : server.queryNames(gcName, null)){
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

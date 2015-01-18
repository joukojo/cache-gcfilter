cache-gcfilter
==============

Listens the GC events from the JVM, on Full GC, the ehcaches statistic is flushed on Full GC event

To enable the listening add following code in your application:

		import net.yogocodes.gclistener.ehcache.EhCacheGCListener;

		EhCacheGCListener.register();


See example use please see the ExampleApp.java file.


The events are logged via slf4j, so for example the log4j.xml: 

	<logger name="net.yogocodes.gclistener.ehcache.EhCacheGCListener">
 		<level value="info" />
 	</logger>


This jar supports the Oracle java7 and java8.

JVM-options
===========

gc.listener.disable - disable the full gc monitoring. Set this value to true

gc.listener.analyze.memory - set this true, if you want to calculate cache size. This consumes CPU

gc.listener.loop.detect - enable the full gc -loop detection. Monitor only if the full gc is happening too often

gc.listener.loop.time - how often the full gc is determined as a full gc -loop. Value users msecs.



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


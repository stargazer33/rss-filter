package org.dfotos.rssfilter.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

import org.dfotos.rssfilter.App;

public class LogHelper {
	
	/**
	 * 
	 * @param logConfig
	 */
	public static void initLogs( String logConfig ) 
	{
		InputStream logPropCfg=App.class.getResourceAsStream(logConfig);
    	if(logPropCfg!=null){
    		try {
				LogManager.getLogManager().readConfiguration( logPropCfg );
		    	//System.getProperties().setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s %2$s %5$s%6$s%n");
		    	System.getProperties().setProperty("java.util.logging.SimpleFormatter.format", "%4$s %2$s %5$s%6$s%n");
			} 
    		catch (SecurityException | IOException e) {
        		System.out.printf("Error %s reading log configuration from %s ", e.toString(), logConfig);
			}
    	}
    	else{
    		System.out.println("Not found: "+logConfig);
    	}
	}
	
	
}

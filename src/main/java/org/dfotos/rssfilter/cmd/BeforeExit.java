package org.dfotos.rssfilter.cmd;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dfotos.rssfilter.App;

/**
 * Code that runs just before the program exit 
 */
public class BeforeExit implements CommandIntf {

	private static final Logger log = Logger.getLogger( BeforeExit.class.getName() );	
	
	/**
	 * if "all data" were modified --> than save it
	 */
	@Override
	public void run(List<String> args) 
	throws Exception {
		log.log( Level.FINE,"begin");
		
		if(true==App.getData().isAllDataChanged()){
			App.getData().writeAllItems();
		}
		
		log.log( Level.FINE,"end");
	}

	/**
	 * this is an internal command - the end-user will never see this!
	 */
	@Override
	public String getHelpStr() {
		return "";
	}

}

package org.dfotos.rssfilter.cmd;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dfotos.rssfilter.App;
import org.dfotos.rssfilter.RssItem;
import org.dfotos.rssfilter.src.SrcIntf;

/**
 * 
 * Iterates over all configured data sources and 
 * retrieves the data (usually the RSS feeds).
 *
 */
public class GetCmd implements CommandIntf {

	private static final Logger log = Logger.getLogger( GetCmd.class.getName() );	
	
	@Override
	public void run(List<String> args) 
	throws Exception 
	{
    	log.log( Level.INFO, "begin");
    	
    	//initialization - this is what we doing here
    	App.getData().setAllItemsInitialized(true);
		App.getData().getAllItems().clear();
    	
		List<SrcIntf> sources=App.getConfig().getSources();
		for (SrcIntf src : sources) {
			try{
				List<RssItem> newList = src.doRead(); //read this from the source 
				for (RssItem rssItem : newList) { // add each item from the newList to the Data 
					App.getData().addItem(rssItem);
				}
				
				App.getData().commitItems(); //now commit all the changes
			}
			catch(Throwable e){
				//processing of one source fails, go to the next one
		    	log.log( Level.SEVERE, "Exception processing source: "+src.getName()+" :", e );
			}
		}//for
		
    	log.log( Level.INFO, "end");
	}

	@Override
	public String getHelpStr() {
		return "Iterates over all configured data sources and retrieves the data (usually the RSS feeds)";
	}

}

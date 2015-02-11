package org.dfotos.rssfilter.cmd;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dfotos.rssfilter.App;
import org.dfotos.rssfilter.RssItem;

/**
 * "tagclear" command
 * 
 * Just removes all tags from each RssItem
 *
 */
public class TagClearCmd 
implements CommandIntf 
{

	private static final Logger log = Logger.getLogger( TagClearCmd.class.getName() );	

	/**
	 * For every item in App.getData().getAllItems() we call .getTags().clear();
	 */
	@Override
	public void run(List<String> args) 
	throws Exception 
	{
		log.log( Level.INFO, "begin");
		
		List<RssItem> allItems=App.getData().getAllItems();
		
		App.getData().setAllDataChanged(true);
		for (RssItem rssItem : allItems) {
			rssItem.getTags().clear();
		}
		
		log.log( Level.INFO, "end");
	}

	@Override
	public String getHelpStr() {
		return "removes all tags attached to rss items (usefull when debugging taggers)";
	}
	
	
}

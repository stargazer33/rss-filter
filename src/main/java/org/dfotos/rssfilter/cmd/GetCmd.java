package org.dfotos.rssfilter.cmd;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dfotos.rssfilter.App;
import org.dfotos.rssfilter.RssItem;
import org.dfotos.rssfilter.src.SrcIntf;

/**
 * Iterates over all configured data sources and 
 * retrieves the data (usually the RSS feeds).
 * 
 * Than removes the duplicates and than store the data in the global lists,
 * see App.getData().addItem()
 * 
 */
public class GetCmd implements CommandIntf {

	private static final Logger log = Logger.getLogger( GetCmd.class.getName() );	
	
	@Override
	public void run(List<String> args) 
	throws Exception 
	{
    	log.log( Level.INFO, "begin");
    	
		//ensure the "all items" list initialized
    	App.getData().getAllItems();
    	
		List<SrcIntf> sources=App.getConfig().getSources();
		for (SrcIntf src : sources) {
			try{
				List<RssItem> newList = src.doRead(); //read this from the source 
				
				removeDuplicates(newList, src); //why should we add the items already in the global list?
				
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
	
	/**
	 * In case items from the newList already exists in
	 *   App.getData().getAllItems()
	 * -->remove those items from newList
	 * 
	 * @param newList
	 * @param src - used for logging
	 * 
	 * @throws IOException
	 */
	private void removeDuplicates(List<RssItem> newList, SrcIntf src) 
	throws IOException 
	{
		
		int nD=0;
		List<RssItem> allItems=App.getData().getAllItems();
		
		for (Iterator<RssItem> newIterator = newList.iterator(); newIterator.hasNext();) {
			RssItem newItem = (RssItem) newIterator.next();
			
			//for every item in the newList - look in the allItems list
			for (RssItem oldItem : allItems) {
				if( isSimilar(newItem, oldItem) ){
					//remove similar elements from the new list
					newIterator.remove();
					nD++;
					break; //duplicate found -> stop searching
				}
			}
			
		}

    	log.log( Level.INFO, "{0}, duplicates removed: {1}", new Object[]{src.getName(), nD} );

	}

	/**
	 * 
	 * @param newItem
	 * @param oldItem
	 * 
	 * @return true if both items from the same source and have same URL
	 */
	private boolean isSimilar(RssItem newItem, RssItem oldItem) {
		if( newItem==null || oldItem == null ){
			return false;
		}
		if (newItem.getSource()==null || oldItem.getSource()==null ){
			return false;
		}
		
		if( !newItem.getSource().equals( oldItem.getSource() ) ){
			//different sources -> not similar
			return false;
		}
		
		//we know, the items are from the same source -> than just compare the URL!
		return newItem.getUrl().equals( oldItem.getUrl() );
		
	}

	@Override
	public String getHelpStr() {
		return "Iterates over all configured data sources and retrieves the data (usually the RSS feeds)";
	}

}

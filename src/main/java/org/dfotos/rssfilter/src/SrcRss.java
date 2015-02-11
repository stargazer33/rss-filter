package org.dfotos.rssfilter.src;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dfotos.rssfilter.RssItem;

import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * RSS/ATOM feed as data source 
 *
 */
public class SrcRss 
extends SrcBase 
{
	
	private static final Logger log = Logger.getLogger( SrcRss.class.getName() );	

	/** 
	 * Perform HTTP GET, read the RSS/ATOM data, convert it to list of RssItem's
	 */
	@Override
	public List<RssItem> doRead()
	throws Exception
	{
    	log.log( Level.FINE, "begin");
		List<RssItem> result=new ArrayList<RssItem> (50);
		
		log.log( Level.INFO, "reading "+url+" ..." );
		URL feedUrl = new URL( url );
		SyndFeedInput input = new SyndFeedInput();
		input.setPreserveWireFeed(true);
        SyndFeed feed = input.build(new XmlReader(feedUrl));
    	log.log( Level.FINE, ""+feed.getEntries().size() + " RSS/ATOM items read");
        
        for (Object obj : feed.getEntries()) {
        	SyndEntryImpl src=(SyndEntryImpl)obj;
        	RssItem trg = new RssItem();
        	result.add(trg);
        	
        	trg.setUrl(src.getLink());
        	trg.setTitle( src.getTitleEx().getValue() );
        	
        	if( null==src.getDescription() || null==src.getDescription().getValue() ){
        		log.log(Level.SEVERE, getName()+" missing item description");
        	}
        	else{
            	trg.setDescription( src.getDescription().getValue() );
        	}
        	
        	//TODO: in case of ATOM feed from  stackoverflow.com
        	// the src.getPublishedDate() returns NULL !!!
        	trg.setPublished( src.getPublishedDate() );
        	trg.setSource(name);
		}
        
    	log.log( Level.FINE, "end");
		return result;
	}
	
}

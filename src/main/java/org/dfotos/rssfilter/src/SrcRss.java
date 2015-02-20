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
		log.log( Level.INFO, "{0}, reading...", new Object[]{ getName() } );
		URL feedUrl = new URL( url );
		SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(feedUrl));
    	log.log( Level.INFO, "{0}, {1} RSS/ATOM items read",  new Object[]{ getName(), feed.getEntries().size() } );
        
    	int numNoDescr=0;
        for (Object obj : feed.getEntries()) {
        	SyndEntryImpl src=(SyndEntryImpl)obj;
        	RssItem trg = new RssItem();
        	result.add(trg);
        	
        	trg.setUrl(src.getLink());
        	trg.setTitle( src.getTitleEx().getValue() );
        	
        	if( null==src.getDescription() || null==src.getDescription().getValue() ){
        		numNoDescr++;
        	}
        	else{
            	trg.setDescription( src.getDescription().getValue() );
        	}
        	
        	//TODO: in case of ATOM feed from  stackoverflow.com
        	// the src.getPublishedDate() returns NULL !!!
        	trg.setPublished( src.getPublishedDate() );
        	trg.setSource(name);
		}
        
        if( numNoDescr>0 ){
        	log.log( Level.INFO, "{0}, number of items without description: {1}",  new Object[]{ getName(), numNoDescr } );
        }
        
    	log.log( Level.FINE, "end");
		return result;
	}
	
}

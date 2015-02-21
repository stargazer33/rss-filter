package org.dfotos.rssfilter.cmd;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dfotos.rssfilter.App;
import org.dfotos.rssfilter.tag.Tagger;

/**
 * "tag" command
 * Run all configured "taggers" 
 *
 */
public class TagCmd 
implements CommandIntf 
{

	private static final Logger log = Logger.getLogger( TagCmd.class.getName() );	

	@Override
	public void run(List<String> args) 
	throws Exception 
	{
		log.log( Level.INFO, "begin");
		
		List<Tagger> allTaggers= App.getConfig().getTaggers();
		for (Tagger tagger : allTaggers) {
			tagger.setData( App.getData() );
			tagger.assignTags();
		}
		
		log.log( Level.INFO, "end");
	}

	@Override
	public String getHelpStr() {
		return "attaches the tags to the rss items (actually delegates this to \"taggers\")";
	}

	
}

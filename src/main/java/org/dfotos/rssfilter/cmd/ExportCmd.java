package org.dfotos.rssfilter.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dfotos.rssfilter.App;
import org.dfotos.rssfilter.RssItem;
import org.dfotos.rssfilter.exp.ExportFile;

/**
 * Implementation of "export" command
 * 
 * Runs all configured exports.
 * It writes the RssItem's from App.getData().getAllItems() into user-friendly 
 * (*.atom or something else) output format
 *  
 * The actual job delegated to the ExportFile instances
 */
public class ExportCmd 
implements CommandIntf 
{

	private static final Logger log = Logger.getLogger( ExportCmd.class.getName() );	

	/**
	 * 
	 */
	@Override
	public void run(List<String> args) 
	throws Exception 
	{
		log.log( Level.INFO, "begin");
		
		List<RssItem> allItems=App.getData().getAllItems();
		List<ExportFile> exports= App.getConfig().getExportFiles();
		
		for (ExportFile exportFile : exports) {
			
			//step1 - collect items in the "toExport" list
			List<RssItem> toExport=new ArrayList<RssItem> (50);
			for (RssItem rssItem : allItems) {
				exportFile.exportItem(rssItem, toExport);
			}
			
			//step2 - write items 
			exportFile.write( toExport );
		}
				
		log.log( Level.INFO, "end");
	}

	@Override
	public String getHelpStr() {
		return "goes through all configured \"exportFiles\" and write files (usually these are *.atom files in the current dir)";
	}
	
}

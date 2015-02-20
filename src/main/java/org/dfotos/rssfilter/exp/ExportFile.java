package org.dfotos.rssfilter.exp;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dfotos.rssfilter.RssItem;
import org.dfotos.rssfilter.util.Utils;

import com.sun.syndication.io.FeedException;

/**
 * Describes the "export file" for a list of RssItems.
 * Each ExportFile exports the RssItem (not)having tags as in "tagsNOT", "tagsOR", "tagsAND"
 * 
 * Export performed in 3 steps:
 * 
 * 1. The ExportFile instance created and initialized
 * (export name (=filename), conditions: tagsAND, tagsOR, tagsNOT)
 * 
 * 2. For each R
 *
 */
public class ExportFile {

	private static final Logger log = Logger.getLogger( ExportFile.class.getName() );	
	
	
	private String name;
	
	
	private Set<String> tagsAND=new HashSet<String> ();
	private Set<String> tagsOR=new HashSet<String> ();
	private Set<String> tagsNOT=new HashSet<String> ();
	
	
	
	/********************************************/
	public ExportFile(){
		
	}
	
	/**
	 * 
	 * @return (end-user) readable description of the object
	 */
	String getDescription(){
		String result="";
		if(!tagsNOT.isEmpty()){
			result=result + "NOT tags: ( "+tagsNOT.toString()+" ) ";
		}
		if(!tagsOR.isEmpty()){
			result=result + "OR tags: ( "+tagsOR.toString()+" ) ";
		}
		if(!tagsAND.isEmpty()){
			result=result + "AND tags: ( "+tagsAND.toString()+" )";
		}
		return result;
	}
	
	/**
	 * Does the specified "item" meets the conditions in "tagsNOT", "tagsOR", "tagsAND" ?
	 * This method checks all the conditions, and, 
	 * it can add the item to "toExport" list.
	 * 
	 * Check the source code of the method, it is self-explained
	 * 
	 * @param item
	 * @param toExport
	 */
	public void exportItem( RssItem item, List<RssItem> toExport) {

		Set<String> itemTags=item.getTags();
	
		if (itemTags.isEmpty()){
			return;
		}
		
		for (String tag : itemTags ) {
			if( tagsNOT.contains(tag) ){
				//any "NOT" tags in our RssItem? -> skip other checks and return
				return;
			}
		}
		
		for (String tag : itemTags ) {
			if( tagsOR.contains(tag) ){
				//any "OR" tags in our RssItem? -> export it, skip other checks
				toExport.add(item);
				return;
			}
		}
		
		if ( !tagsAND.isEmpty() && itemTags.containsAll(tagsAND) )
		{
			//last check
			toExport.add(item);
		}
	}
	
	/**
	 * Writes the given list to the (ATOM) file
	 * 
	 * @param toExport
	 */
	public void write(List<RssItem> toExport)
	//throws IOException
	{
		String fileName=name+".atom";
		
		try {
			
			Utils.sortItemsByDate(toExport, false);
			
			Utils.writeItemsToAtomFile(
				toExport, 
				name, //"title", 
				"file:///"+fileName, 
				getDescription(), 
				fileName
			);
			
		} 
		catch (IOException | FeedException e) {
			log.log( Level.SEVERE, "Error writing to" + fileName, e );
		} 
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<String> getTagsAND() {
		return tagsAND;
	}

	public void setTagsAND(Set<String> tagsAND) {
		this.tagsAND = tagsAND;
	}

	public Set<String> getTagsOR() {
		return tagsOR;
	}

	public void setTagsOR(Set<String> tagsOR) {
		this.tagsOR = tagsOR;
	}

	public Set<String> getTagsNOT() {
		return tagsNOT;
	}

	public void setTagsNOT(Set<String> tagsNOT) {
		this.tagsNOT = tagsNOT;
	}

	
}

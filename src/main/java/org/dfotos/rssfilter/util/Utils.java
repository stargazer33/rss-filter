package org.dfotos.rssfilter.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dfotos.rssfilter.Config;
import org.dfotos.rssfilter.RssItem;
import org.dfotos.rssfilter.exp.ExportFile;
import org.dfotos.rssfilter.src.SrcIntf;
import org.dfotos.rssfilter.src.SrcRss;
import org.dfotos.rssfilter.tag.LuceneTagger;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlWriter;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * Collection of (unrelated) static methods used across the project
 *  
 */
public class Utils {
	
	/**
	 * List of classes used in *.yml files
	 */
	@SuppressWarnings("rawtypes")
	private static final List<Class> YML_CLASSES = new ArrayList<Class> ();
	static{
		YML_CLASSES.add(Config.class);
		YML_CLASSES.add(SrcIntf.class);
		YML_CLASSES.add(ExportFile.class);
		YML_CLASSES.add(SrcRss.class);
		YML_CLASSES.add(LuceneTagger.class);
	}
	
	private static final Logger log = Logger.getLogger( Utils.class.getName() );	

	/**
	 * @param resultStrJson JSON as string
	 * @param collectionType type specification of items in the returned list
	 * 
	 * @return list of objects created from resultStrJson. 
	 * Type of each objects in the List: as specified by "collectionType" 
	 */
	
	@SuppressWarnings("rawtypes")
	public static List convertJsonToPojo(String resultStrJson, Type collectionType ) 
	{
		//convert JSON to list of our POJO,
		List<RssItem> result=new ArrayList<RssItem> ();
				
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new StringReader(resultStrJson));
		reader.setLenient(true);
		result=gson.fromJson(reader, collectionType);

		return result;
	}
	
	/**
	 * Store the listRssItem in the (YAML) fileName
	 * 
	 * @param listRssItem
	 * @param fileName
	 * @throws IOException
	 */
	public static void writeItemsToYamlFile(
			List<RssItem> listRssItem, 
			String fileName
	)
	throws IOException
	{
		log.log(Level.FINE, "begin");
		
		YamlWriter writer=null;
	    try {
			writer = new YamlWriter(new FileWriter(fileName));
			Utils.setClassTags( writer.getConfig() );
			log.log( Level.INFO, "writing " +listRssItem.size() + " items to file: " +fileName+"  ...");
			writer.write(listRssItem);
		} 
	    finally{
        	//if not null -- always close writer 
		    if(writer!=null){
		    	try {
					writer.close();
				} 
		    	catch (Throwable e) {}
		    }
	    }
		
		log.log(Level.FINE, "end");
	}
	
	/**
	 * Takes Set of Strings and returns it as a List<SyndCategoryImpl> 
	 *  
	 * @param set
	 * @return
	 */
	public static List<SyndCategoryImpl> convertSetToSyndCategories( Set<String> set ){
		
		List<SyndCategoryImpl> result= new ArrayList<SyndCategoryImpl> ();
		
		for (String str : set) 
		{
			SyndCategoryImpl cat=new SyndCategoryImpl();
			result.add( cat );
			cat.setName(str);
		}
		
		return result;
	}
	
	/**
	 * Writes specified listRssItem into (RSS/ATOM) file "fileName".
	 * Set title, link, feedDescription correspondingly
	 * 
	 * @param listRssItem
	 * @param title
	 * @param link
	 * @param feedDescription
	 * @param fileName
	 * 
	 * @throws IOException
	 * @throws FeedException
	 */
	public static void writeItemsToAtomFile(
			List<RssItem> listRssItem, 
			String title, 
			String link, 
			String feedDescription,
			String fileName
	) 
	throws IOException, FeedException 
	{
		SyndFeed feed = new SyndFeedImpl();
        List<SyndEntry> entries = new ArrayList<SyndEntry> ();
        feed.setEntries(entries);
        
        /*
        possible feed types:
         
        rss_0.91N
        rss_0.93
        rss_0.92
        rss_1.0
        rss_0.94
        rss_2.0
        rss_0.91U
        rss_0.9
        atom_1.0
        atom_0.3
        */
        
        feed.setFeedType("atom_1.0");
        feed.setLanguage("en");
        feed.setAuthor("rss-filter");
		
		feed.setTitle(title);
        feed.setLink(link);
        feed.setDescription(feedDescription);
        
        SyndEntry entry;
        SyndContent description;        
        for (RssItem rssItem : listRssItem) {
        	entry = new SyndEntryImpl();
        	description = new SyndContentImpl();
        	entry.setDescription(description);
        	entry.setPublishedDate( rssItem.getPublished() );
        	entry.setTitle( rssItem.getTitle() );
        	entry.setLink(  rssItem.getUrl() );
        	entry.setCategories( Utils.convertSetToSyndCategories(rssItem.getTags()) );
        	description.setType( "text/html" );
        	description.setValue( rssItem.getDescription() );
        	entries.add(entry);
		}

		Writer writer = null;
		try{
            writer = new FileWriter(fileName);
        	SyndFeedOutput output = new SyndFeedOutput();
    		log.log( Level.INFO, "writing " +listRssItem.size() + " items to file: " +fileName+"  ...");
        	output.output(feed,writer);
        }
        finally{
        	//if not null -- always close writer 
        	if(null!=writer){
        		try{
        			writer.close();        
        		}
        		catch(Throwable e){
        		}
        	}
        }
		log.log( Level.INFO, "done " +fileName);
	}

	/**
	 * RssItem comparator, "by published date", ascending
	 */
	static Comparator<RssItem> RssItemPublishedDateComparator=new Comparator<RssItem>() 
	{
		@Override
		public int compare(RssItem o1, RssItem o2) {
			
			if (o1.getPublished() == null || o2.getPublished() == null)
		        return 0;					
			
			return o1.getPublished().compareTo(o2.getPublished());
		}
	};
	
	/**
	 * RssItem comparator, "by published date", descending
	 */
	static Comparator<RssItem> RssItemPublishedDateComparatorReverse=new Comparator<RssItem>() 
	{
		@Override
		public int compare(RssItem o1, RssItem o2) {
			
			if (o1.getPublished() == null || o2.getPublished() == null)
		        return 0;					
			
			return ( o1.getPublished().compareTo(o2.getPublished()) ) * (-1);
		}
	};
	
	/**
	 * 
	 * @param listItems
	 * @param asc
	 */
	public static void sortItemsByDate( List<RssItem> listItems, boolean asc){
		
		if(asc){
			Collections.sort( listItems,  RssItemPublishedDateComparator);
		}
		else{
			Collections.sort( listItems,  RssItemPublishedDateComparatorReverse);
		}	
	}	
	
	private static String XML10_PATTERN = "[^"
            + "\u0009\r\n"
            + "\u0020-\uD7FF"
            + "\uE000-\uFFFD"
            + "\ud800\udc00-\udbff\udfff"
            + "]";
	
	private static String EOL_PATTERN="(\r\n|\n)";
	
	/**
	 * @param in
	 * @return "in" but without the invalid (from XML 1.0 spec point of view) characters
	 */
	public static String stripNonValidXMLCharacters(String in) {
		if(null==in){
			return "";
		}
		return in.replaceAll(XML10_PATTERN, "");
	}
	
	/**
	 * @param in
	 * @return "in" with all end-of-lines replaced with HTML tag "<br />" 
	 */
	public static String convertEOLtoHTML(String in){
		if(null==in){
			return "";
		}
		return in.replaceAll( EOL_PATTERN, "<br />");
	}
	
	/**
	 * @param in
	 * @return apply both stripNonValidXMLCharacters and convertEOLtoHTML to in, return "fixed" string
	 */
	public static String fixStringForRss(String in){
		return convertEOLtoHTML( Utils.stripNonValidXMLCharacters(in) );
	}

	/**
	 * @param c
	 * @return "Simple" (without package prefix) class name of "c"; in lower case
	 */
	@SuppressWarnings("rawtypes")
	public static String getSimpleClassName(Class c){
		return c.getSimpleName().toLowerCase();
	}
	
	
	/**
	 * Apply so-called "class tags" to the conf
	 * this replaces the the "full" class names in the YML with the "simple" class names.
	 * Replacement is done for all classes in the YML_CLASSES list
	 * 
	 * @param conf
	 * @throws IOException 
	 */
	@SuppressWarnings("rawtypes")
	public static void setClassTags(YamlConfig conf) 
	throws IOException
	{
		for (Class cl: YML_CLASSES) {
			conf.setClassTag( Utils.getSimpleClassName(cl), cl);
		}
	}
	
}

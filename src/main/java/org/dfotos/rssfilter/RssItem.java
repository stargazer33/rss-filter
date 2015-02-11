package org.dfotos.rssfilter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 *  Technology-independent container of text 
 *  Represents an <item> in the RSS feed or <entry> in the ATOM feed 
 *  
 *  Properties:
 *  
 *    title 		= RSS <title> tag
 *    description	= RSS <description> tag or ATOM <summary>
 *    url 		    = RSS <link> tag
 *    published		= RSS <pubDate> or ATOM <published>		
 *    tags			~ ATOM  <category>. these are the tags assigned to the item (Taggers doing the assignment)
 *    source		= internal, the name of the "data source" used to produce this item. 
 *    id			= internal, application-wide ID of the item. It will be assigned by Data.addItem(), used as key to store RssItem instances in Maps
 */

public class RssItem 
{

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger( RssItem.class.getName() );	
	
	public RssItem(){
	}
	
	private transient String _titleDescription;
	
	private long  id;
	
	private String  source="";
	
	private String 	description="";
	
	private String 	url="";
	
	private String 	title="";
	
	private Date   	published=new Date();
	
	private Set<String>	tags=new HashSet<String> ();
	
	

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}

	public String getDescription() {
		return description;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getPublished() {
		return published;
	}

	public void setPublished(Date published) {
		
		//silently ignore null!
		if ( published== null )
			return;
		
		this.published = published;
	}
	
	/**
	 * return string containing both "title" and "description" separated by " | " 
	 * @return
	 */
	public String getTitleDescription(){
		if ( _titleDescription!=null ) {
			return _titleDescription;
		}
		
		buildTitleDescription();
		return _titleDescription;
	}
	
	private synchronized void buildTitleDescription(){
		if ( _titleDescription!=null ) {
			return;
		}
		
		StringBuilder b=new StringBuilder();
		b.append( getTitle() );
		b.append( " | " );
		b.append( getDescription() );
		
		_titleDescription=b.toString();

	}
}
package org.dfotos.rssfilter.tag;

import java.util.HashSet;
import java.util.Set;

import org.dfotos.rssfilter.RssItem;

/**
 * Tagger - something that can attach tags to the RssItem's
 *  
 */
public abstract class Tagger {
	
	/**
	 * The tag to attach
	 */
	String tagName=new String();

	/**
	 * which part of the RssItem this tagger checks?
	 * 
	 * Domain:
	 * "t"	- check title
	 * "d"	- check description
	 * "td"	- check both title and description
	 */
	private String docPart=new String();
	
	private static Set<String> DOCPART_DOMAIN=new HashSet<String> ();
	static{
		DOCPART_DOMAIN.add("t");
		DOCPART_DOMAIN.add("d");
		DOCPART_DOMAIN.add("td");
	}
	
	/****************************************/

	public String toString(){
		String result=getClass().getSimpleName()+", name: "+tagName;
		return result;
	}
	
	public String getDocPart() {
		return docPart;
	}

	public void setDocPart(String docPart) {
		if( !DOCPART_DOMAIN.contains(docPart) ){
			throw new IllegalArgumentException( ""+docPart+" is wrong argument for Tagger.setDocPart. Use one of these arguments: 't', 'd', 'td'." );
		}
		
		this.docPart = docPart;
	}
	
	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	

	
	/**
	 * Assign tags
	 * 
	 * @throws Exception 
	 */
	public abstract void assignTags() throws Exception;

	/**
	 * 
	 * @param item
	 * 
	 * @return either title or description or both according to getDocPart() 
	 */
	public String getRssItemStr( RssItem item ){
		String result="";
		
		if( "t".equals( getDocPart() )) {
			result = item.getTitle();
		}		
		else if( "d".equals( getDocPart() )) {
			result = item.getDescription();
		}
		else if( "td".equals( getDocPart() )) {
			result = item.getTitleDescription();
			
		}
		
		return result;
	}
}

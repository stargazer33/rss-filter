package org.dfotos.rssfilter.src;

import java.util.List;

import org.dfotos.rssfilter.RssItem;

/**
 * All "sources" implements this interface
 * 
 */
public interface SrcIntf {

	/**
	 * get the data from some remote server and return as list of RssItem 
	 * @return
	 */
	public abstract List<RssItem> doRead() throws Exception;

	public abstract String getUrl();

	public abstract void setUrl(String url);

	public abstract String getName();

	public abstract void setName(String name);

	//public abstract Date getLastRead();

}
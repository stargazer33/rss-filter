package org.dfotos.rssfilter.src.custom;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dfotos.rssfilter.RssItem;
import org.dfotos.rssfilter.src.AbstractSrc;
import org.dfotos.rssfilter.util.Utils;

import com.google.gson.reflect.TypeToken;

/**
 * Site jobs.remotive.io as a data source 
 *
 */
public class SrcJobsRemotiveIo 
extends AbstractSrc {

	private static final Logger log = Logger.getLogger( SrcJobsRemotiveIo.class.getName() );	

	Pattern patternScript = Pattern.compile("<script>window.RJ = (.+?)</script>");
	
	/**
	 * read JSON data from jobs.remotive.io and convert to List<RssItem>  
	 */
	@Override
	public List<RssItem> doRead() 
	throws Exception
	{
    	log.log( Level.FINE, "begin");
    	List<RssItem> result= new ArrayList<RssItem> (60);
    	
    	//get data from the site
		String tmp = doHttpGet();
		
		//extract JSON we need from the web page
    	Matcher matcher = patternScript.matcher(tmp);
    	if (matcher.find()){
    		tmp=matcher.group(1);
    		tmp=tmp.substring( tmp.indexOf("\"entries\"")+10, tmp.length()-1 );
    		//log.log( Level.FINE,  tmp);
    	}
    	else{
    		log.log( Level.SEVERE,  "No JSON found here: "+url);
    		return result;
    	}
    	
		//create list of JsonItem's from JSON string 
		@SuppressWarnings("rawtypes")
		List listPojo=Utils.convertJsonToPojo(
			tmp, 
			new TypeToken<List<JsonItem>>(){}.getType() //provide the type info, JsonItem will be instantiated 
		);
    	log.log( Level.INFO, "{0}, {1} JSON items read", new Object[]{ getName(), listPojo.size()});
    	
		//copy JsonItem -> RssItem
		for (Object obj : listPojo) {
			JsonItem jsonItem=(JsonItem)obj;
			jsonItem.construct();
			
			RssItem rssItem=new RssItem();
			result.add(rssItem);
			
			rssItem.setSource( this.name );
			rssItem.setPublished( jsonItem._date );
			rssItem.setTitle( jsonItem._title );
			rssItem.setDescription( jsonItem._description );
			rssItem.setUrl( jsonItem._url );			
		}
		
		log.log( Level.FINE, "end");
		return result;
		
	}


	/*******************************************************************************/
	
	/**
	 * Temporary storage for one record obtained from JSON from jobs.remotive.io 
	 */
	@SuppressWarnings("unused")
	private static class JsonItem {
		
		public JsonItem(){};
		
		String _description;
		String _url;
		String _title;
		Date	_date; 
		
		
		// this date format used in the API:  2014-12-05 12:58:24
		private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		public void construct(){
			_title=role + " at " + company + ", " + region + ", " + category;
			_url=url;
			_date=new Date();
			_description="";
		}
			
		public String toString(){
			return "role: "+role+"region: "+region+" url: "+_url;
		}
		
		String company;
		String category;
		String companySlug;
		String partner;
		String region;
		String role;
		String url;
	}


}

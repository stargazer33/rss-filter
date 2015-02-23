/**
 * This file is part of Rss-filter.
 *
 * Rss-filter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rss-filter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Rss-filter.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dfotos.rssfilter.src.custom;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dfotos.rssfilter.RssItem;
import org.dfotos.rssfilter.src.AbstractSrc;
import org.dfotos.rssfilter.util.Utils;
import com.google.gson.reflect.TypeToken;

/**
 * Site workinstartups.com as a data source. 
 * @author stargazer33
 * @version $Id$
 */
public class SrcWorkinstartups 
extends AbstractSrc 
{
    /**
     * 
     */
	private static final Logger LOG = Logger.getLogger( SrcWorkinstartups.class.getName() );	
	
	@Override
	public List<RssItem> doRead() 
	throws Exception
	{
    	LOG.log( Level.FINE, "begin");
    	List<RssItem> result= new ArrayList<RssItem> (60);
    	
    	//get data from the site
    	String tmp = doHttpGet();
		
		//create list of JsonItem's from string 
		@SuppressWarnings("rawtypes")
		List listPojo=Utils.convertJsonToPojo(
			tmp.substring( 11, tmp.length() ), // cut the garbage in the first 11 chars! 
			new TypeToken<List<JsonItem>>(){}.getType() //provide the type info, JsonItem will be instantiated 
		);
    	LOG.log( Level.INFO, "{0}, {1} JSON items read", new Object[]{ getName(), listPojo.size()});
		
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
		
		LOG.log( Level.FINE, "end");
		return result;
	}

	/*******************************************************************************/
	
	/**
	 * Temporary storage for one record obtained from JSON from the workinstartups.com API 
	 */
	@SuppressWarnings("unused")
	private static class JsonItem {
		
		public JsonItem(){};
		
		String _description;
		String _url;
		String _title;
		Date	_date; //use mysql_date
		
		
		// this date format used in the API:  2014-12-05 12:58:24
		private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		@SuppressWarnings("deprecation")
		public void construct(){
			_description = Utils.fixStringForRss(description);
			_url = "http://workinstartups.com/job-board/job/"+id+"/"+url_title+"/";
			_title = Utils.fixStringForRss(title);
			
			try {
				_date = formatter.parse(mysql_date);
			} catch (ParseException e) {
				//can not parse -> set this dummy value
				_date = new Date( Date.UTC(1971, 1, 1, 0, 0, 0) );
			}
		}
			
		public String toString(){
			return "title: "+title+" url: "+_url;
		}
		
		String apply;
		String apply_online;
		String category_id;
		String category_name;
		String city_id;
		String closed_on;
		String company;
		String created;
		String created_on;
		String days_old;
		String description;	
		String expiration;
		String expiration_date;
		String id;
		String is_active;
		String is_location_anywhere;
		String is_spotlight;
		String location;
		String location_outside_ro;
		String mysql_date;
		String salary_frequency;
		String salary_frequency_label;
		String salary_from;
		String salary_to;
		String title;
		String type_id;
		String type_name;
		String type_var_name;
		String url;
		String url_title;
		String views_count;
	}


}

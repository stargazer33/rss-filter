package org.dfotos.rssfilter.tag;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.dfotos.rssfilter.RssItem;


/**
 *  Use Lucene query and index (index is in Data class, see Data.getLuceneIndexSearcher())
 *  to tag the RssItems.
 * 
 *  The assignTags() method of this class 
 *  performs the Lucene query (see "query" property) 
 *  and attaches the tag (see "tagName" property) to every RssIetm returned by the query. 
 *
 */
public class LuceneTagger extends Tagger {
	
	private static final Logger log = Logger.getLogger( LuceneTagger.class.getName() );	
	
	/**
	 * which (Lucene) fields should we load when retrieving a Lucene document from index?
	 */
	private static Set<String> fieldsToLoad=new HashSet<String>();
	static{
		fieldsToLoad.add("id");
	}
	
	/**
	 * Lucene query
	 */
	private String query="";
	
    /**********************************************************/
	
	public String toString(){
		String result=super.toString()+", query: "+query;
		return result;
	}

	
	@Override
	public void assignTags() 
	throws Exception
	{

		Map<Long, RssItem> idMap= getData().getAllItemsIdMap();
		IndexSearcher searcher = getData().getLuceneIndexSearcher();
	
		Analyzer an=new StandardAnalyzer();
		QueryParser queryParser = null;
		
		//IMPORTANT: be default we query ONLY the "all" field!
		//specific fields "title" and "description" should be explicitly specified
		//
		queryParser = new QueryParser( "all", an);
		
		Query q = queryParser.parse(query);
		log.log( Level.FINE, "tagName: "+getTagName()+",\ntext_query: "+query+",\nLucene_query: "+q.getClass().getSimpleName()+ ", " + q.toString() );
		
		//now search and iterate through search results (Lucene documents)
		TopDocs docsFound = searcher.search(q, Integer.MAX_VALUE);
		log.log( Level.FINE, "tagName: "+getTagName()+", Items found: "+docsFound.scoreDocs.length );		
		
		for ( ScoreDoc scoreDoc : docsFound.scoreDocs ) {
			//we need only the "id" field in the Lucene Document, we ignore other fields
		    Document doc = searcher.doc( scoreDoc.doc, fieldsToLoad );
		    //retrieve the RssItem by ID
		    String strID=doc.get("id");
		    long longID=Long.parseLong(strID);
		    RssItem rssItem=idMap.get(longID);
		    
		    //attach the tag
			rssItem.getTags().add(getTagName());
			getData().setAllDataChanged(true);
		}
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}

package org.dfotos.rssfilter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

/**
 * Place to store (un)processed data (the RssItem's) 
 * 
 * The most important things:
 *   -The "global list" returned by getAllItems() method as well as the addItem() method
 *   -The map returned by getAllItemsIdMap() helps to find items by ID.
 *   -The getLuceneIndexSearcher() provides access to the Lucene index
 * 
 * Persistense: the global list of RssItem can be load and stored 
 * in the "all-items.yml" file located in the current directory
 * See:
 *   readAllItems()
 *   writeAllItems()
 *  
 */
public class Data {
	
	private static final Logger log = Logger.getLogger( Data.class.getName() );	
	
	private AtomicLong counter = new AtomicLong(0);
	
	private boolean isAllDataChanged=false;
	private boolean isAllItemsInitialized=false;
	
	
	/**
	 * the global list of all items
	 */
	private List<RssItem> allItems= new ArrayList<RssItem> (2000);
	
	/**
	 * our map, to search for RssItem's by ID
	 * Holds all the items from the allItems list 
	 */
	private Map<Long, RssItem> allItemsIdMap = new HashMap<Long, RssItem> (2000);
	
	/**
	 * Lucene index, for queries. 
	 * Holds all the items from the allItems list 
	 */
	private Directory luceneIndex=null;
	
	/**
	 * for search/read
	 */
	private DirectoryReader luceneIndexReader=null;
	/**
	 * for search/read
	 */
	private IndexSearcher luceneIndexSearcher=null;
	
	/**
	 * for index add/update
	 */
	private StandardAnalyzer luceneAnalyzer;
	private IndexWriterConfig luceneIwConfig;
	private IndexWriter luceneIw;

	/********************************************************/
	public Data(){
	}
	
	/**
	 * initialization (mainly lucene fields)
	 * @throws IOException
	 */
	public void init()
	throws IOException
	{
		log.log( Level.FINE, "begin");
		luceneIndex=new RAMDirectory();
				
		//this is for add/update
		luceneAnalyzer = new StandardAnalyzer();
		luceneIwConfig = new IndexWriterConfig(Version.LATEST, luceneAnalyzer);
		luceneIw = new IndexWriter( luceneIndex, luceneIwConfig);
		log.log( Level.FINE, "end");
	}	
	
	/**
	 * First fill the index with data, 
	 * ONLY AFTER THIS this method can be called!
	 * (otherwise Lucene throws an exception)
	 * 
	 * @throws IOException
	 */
	private synchronized void initLuceneSearcher() 
	throws IOException 
	{
		//
		if (luceneIndexSearcher!=null)
			return;
		
		luceneIndexReader = DirectoryReader.open( luceneIndex );
		luceneIndexSearcher = new IndexSearcher( luceneIndexReader );
	}
	

	
	/********************************************************/
	

	/**
	 * 
	 * @return all items; It can be lazy initialized in this method, therefore the IOException
	 */
	public List<RssItem> getAllItems()
	throws IOException
	{
		if(false==isAllItemsInitialized){
			readAllItems();
		}
		
		return allItems;
	}
	
	
    /**
     * @return the map, used to search for RssItem's by ID; It can be lazy initialized in this method, therefore the IOException
     */
	public Map<Long, RssItem> getAllItemsIdMap() 
	throws IOException
	{
		if(false==isAllItemsInitialized){
			readAllItems();
		}
		return allItemsIdMap;
	}
	
	/**
	 * @return IndexSearcher. It can be lazy initialized in this method, therefore the IOException
	 * @throws IOException
	 */
	public IndexSearcher getLuceneIndexSearcher() 
	throws IOException
	{
		if(false==isAllItemsInitialized){
			readAllItems();
		}
		if(null==luceneIndexSearcher)
			initLuceneSearcher();
		
		return luceneIndexSearcher;
	}

	
	/**
	 * @return an application-wide unique ID (will be used as ID in AllItemsMap)
	 */
    public long getNextID() {
        return counter.incrementAndGet();     
    }	

    
	/**
	 * Add the item to the global list (AllItems ) and to the AllItems Map and the Lucene index:
	 * 
	 *  3 fields are stored in Lucene index:
	 *    title
	 *    description
	 *    all (contains both title+description)
	 *    
	 *  IMPORTANT: the commitItems() must be called at some moment!   
	 *  without this the data would not go to Lucene index!
	 *    
	 * @param item
	 * @throws IOException 
	 */
	public void addItem(RssItem item) 
	throws IOException
	{
		setAllDataChanged(true); //mark as "changed"
		
		long id=getNextID();	//get unique ID
		item.setId(id); 	 	//assign it to the item
		getAllItemsIdMap().put(id, item); //put the item into OUR map
		getAllItems().add(item); //add the item to OUR list
		
		//add item to LUCENE index
		Document doc=new Document();
		doc.add(new StringField("id", Long.toString( item.getId() ), Field.Store.YES));
		doc.add(new TextField("title", item.getTitle(), Field.Store.NO));
		doc.add(new TextField("description", item.getDescription(), Field.Store.NO));
		doc.add(new TextField("all", item.getTitleDescription(), Field.Store.NO));
		luceneIw.addDocument(doc);
	}

	/**
	 * Propagate commit to Lucene; set luceneIndexSearcher to null
	 * @throws IOException
	 */
	public void commitItems() 
	throws IOException
	{
		luceneIndexSearcher=null;
		luceneIw.commit();
		
	}

	/**************************************************************/
	
	/**
	 * Write given list of items into YML fila
	 * 
	 * @param items
	 * @param fileName
	 * @throws IOException
	 */
	private static void writeToYamlFile( List<RssItem> items, String fileName )
	throws IOException
	{
    	log.log( Level.INFO, "writing List<RssItem> to "+ fileName + " ...");
		
		YamlWriter writer=null;
	    try {
			writer = new YamlWriter(new FileWriter(fileName));
			writer.write(items);
		} 
	    finally{
		    if(writer!=null){
		    	try {
					writer.close();
				} 
		    	catch (Throwable e) {}
		    }
	    }		
    	log.log( Level.INFO, "done");
	}

	/**
	 * read from YML file, return list of items
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	private static List<RssItem> readFromYamlFile( String fileName )
	throws IOException
	{
    	log.log( Level.INFO, "reading List<RssItem> from "+ fileName + " ...");
    	
		YamlReader reader = new YamlReader(new FileReader(fileName));
		@SuppressWarnings("unchecked")
		List<RssItem> result = (List<RssItem>) reader.read();		
		
    	log.log( Level.INFO, "{0} items read from {1}", new Object[]{ result.size(), fileName,} );
		return result;
	}	
	
	/**
	 * Store the "all items" list of RssItems in the "all-items.yml" 
	 * @throws IOException
	 */
	public void writeAllItems() 
	throws IOException
	{
		writeToYamlFile(allItems, "all-items.yml");
	}	
	
	/**
	 * Read the "all items" list of RssItems from the "all-items.yml"
	 * 
	 * @throws IOException
	 */
	private synchronized void readAllItems()
	throws IOException
	{
		log.log( Level.FINE, "begin");
		List<RssItem> tmpItems=new ArrayList<RssItem>();
		try{
			tmpItems=readFromYamlFile("all-items.yml");
		}
		catch(FileNotFoundException f){
			log.log( Level.SEVERE, "all-items.yml not found - starting from scratch...");
		}
		
		setAllItemsInitialized(true); //should be before "addItem" to avoid endless loop
		for (RssItem rssItem : tmpItems) {
			addItem( rssItem );
		}
		App.getData().commitItems(); //now commit all the changes
		
		initLuceneSearcher();
		
		//data initialized and not changed
		setAllDataChanged(false);
		log.log( Level.FINE, "end");
	}
	
	/*********************************************************************/
	
	public boolean isAllDataChanged() {
		return isAllDataChanged;
	}
	
	/**
	 * Setting this to "true" triggers saving the global list at program end
	 * 
	 * @param isAllDataChanged
	 */
	public void setAllDataChanged(boolean isAllDataChanged) {
		this.isAllDataChanged = isAllDataChanged;
	}

	public boolean isAllItemsInitialized() {
		return isAllItemsInitialized;
	}

	public void setAllItemsInitialized(boolean isAllItemsInitialized) {
		this.isAllItemsInitialized = isAllItemsInitialized;
	}

	
}

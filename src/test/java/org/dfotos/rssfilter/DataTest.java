package org.dfotos.rssfilter;

import java.io.IOException;
import java.util.Date;

import org.dfotos.rssfilter.tag.LuceneTagger;

import junit.framework.TestCase;

public class DataTest extends TestCase {

	static private Data data;
	static private RssItem item;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		data=new Data();
        item=new RssItem();
		data.init();
		data.setAllItemsInitialized(true); //otherwise data.getAllItems() will read all-items.yml
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

    public void test1() 
    throws IOException
    {
        System.out.println("test1");
        assertTrue( data.getAllItems().size()==0 );
    }

    
    public void test2() 
    throws Exception
    {
        System.out.println("test2");
        assertTrue( data.isAllItemsInitialized() );
        assertFalse( data.isAllDataChanged() );
        
        //let's initialize the item
        item.setDescription("description1 abra cadabra secret word");
        item.setTitle("title1");
        item.setUrl("url1");
        item.setPublished( new Date() );
        item.getTags().add("tag1");        

        data.addItem(item);
        assertTrue( data.isAllDataChanged() );
        assertTrue( item.getId()>=1 );
        assertTrue( data.getAllItems().size()==1 );
        
        //we can search be ID in the AllItemsIdMap
        RssItem item2=data.getAllItemsIdMap().get( item.getId() );
        assertNotNull( item2 );
        assertEquals( item2.getUrl(), item.getUrl() );
        
        data.commitItems(); 
        //without "commited" items we can not work with index searcher!
    	assertNotNull( data.getLuceneIndexSearcher() );
        
    	//let us search for word "secret" and assign tag "tag2" if found
    	LuceneTagger tagger=new LuceneTagger();
    	tagger.setData(data);

    	tagger.setTagName("tag2");
    	tagger.setQuery("secret"); 
    	tagger.assignTags();
    	//now the "tag2" attached, let us check this
    	assertTrue( item.getTags().contains("tag1") ); //tag1 is still there
    	assertTrue( item.getTags().contains("tag2") ); //tag2 is attached
    	
    	//let us use the query that does not match the title or description
    	tagger.setTagName("tag3");
    	tagger.setQuery("bad query"); 
    	tagger.assignTags();
    	//tag3 is NOT attached
    	assertFalse( item.getTags().contains("tag3") ); 
    	
    }
    

}

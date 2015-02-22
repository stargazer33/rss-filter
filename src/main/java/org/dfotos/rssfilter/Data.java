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
 * Place to store (un)processed data (the RssItem's). The most important
 * elements of this class: -The "global list" returned by getAllItems() method
 * -The map returned by getAllItemsIdMap() helps to find items by ID. -The
 * getLuceneIndexSearcher() provides access to the Lucene index -the addItem()
 * method used to add an RssItem to the global list, the map and to the Lucene
 * index. Persistense: the global list of RssItem can be load and stored in the
 * "all-items.yml" file located in the current directory See: -readAllItems()
 * -writeAllItems()
 * @author stargazer33
 * @version $Id$
 */
public class Data {

    /**
     * We expect about 2000-10000 RssItem's. We do not expect 20GB of them!
     */
    private static final int DEFAULT_ALL_ITEMS_SIZE=2000;
    
    /**
     * YML file to (temporary) save our RssItem's.
     */
    private static final String DEFAULT_YML_FILE="all-items.yml";
    
    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(Data.class.getName());

    /**
     * Our ID generator. See addItem().
     */
    private final AtomicLong counter = new AtomicLong(0);

    /**
     * Flag indicating "there was change in data".
     */
    private boolean isAllDataChanged;

    /**
     * Is "all items list" initialized or not?
     */
    private boolean isAllItemsInitialized;

    /**
     * The list of all items.
     */
    private final List<RssItem> allItems = new ArrayList<RssItem>(DEFAULT_ALL_ITEMS_SIZE);

    /**
     * A map, to search for RssItem's by ID. Holds all the items from the
     * allItems list.
     */
    private final Map<Long, RssItem> allItemsIdMap =
            new HashMap<Long, RssItem>(DEFAULT_ALL_ITEMS_SIZE);

    /**
     * Lucene index, for queries. Holds all the items from the allItems list.
     */
    private Directory luceneIndex;

    /**
     * For read operations on Lucene index.
     */
    private DirectoryReader luceneIndexReader;

    /**
     * For search in Lucene index.
     */
    private IndexSearcher luceneIndexSearcher;

    /**
     * For Lucene index add/update operations.
     */
    private StandardAnalyzer luceneAnalyzer;

    /**
     * Configuration of Lucene index writer.
     */
    private IndexWriterConfig luceneIwConfig;

    /**
     * Lucene index writer.
     */
    private IndexWriter luceneIw;

    /**
     * Constructor.
     */
    public Data() {
    }

    /**
     * Initialization (mainly Lucene fields initialized here).
     * @throws IOException When something goes wrong.
     */
    public final void init() throws IOException {
        LOG.log(Level.FINE, "begin");
        this.luceneIndex = new RAMDirectory();
        this.luceneAnalyzer = new StandardAnalyzer();
        this.luceneIwConfig = new IndexWriterConfig(Version.LATEST, this.luceneAnalyzer);
        this.luceneIw = new IndexWriter(this.luceneIndex, this.luceneIwConfig);
        LOG.log(Level.FINE, "end");
    }

    /**
     * Returns the "all items" list.
     * @return All items; It can be lazy initialized in this method, therefore
     * the IOException.
     * @exception IOException Something went wrong during initialization.
     */
    public final List<RssItem> getAllItems() throws IOException {
        if (false == this.isAllItemsInitialized) {
            this.readAllItems();
        }
        return this.allItems;
    }

    /**
     * Returns the "ID-to-RssItem" map.
     * @return The map, used to search for RssItem's by ID; It can be lazy
     * initialized in this method, therefore the IOException
     * @exception IOException Something went wrong during initialization.
     */
    public final Map<Long, RssItem> getAllItemsIdMap() throws IOException {
        if (false == this.isAllItemsInitialized) {
            this.readAllItems();
        }
        return this.allItemsIdMap;
    }

    /**
     * Returns/initializes the IndexSearcher.
     * @return IndexSearcher. It can be lazy initialized in this method,
     * therefore the IOException
     * @throws IOException
     * @exception IOException Something went wrong during initialization.
     */
    public final IndexSearcher getLuceneIndexSearcher() throws IOException {
        if (false == this.isAllItemsInitialized) {
            this.readAllItems();
        }
        if (null == this.luceneIndexSearcher){
            this.initLuceneSearcher();
        }
        return this.luceneIndexSearcher;
    }

    /**
     * Returns the next unique ID.
     * @return The application-wide unique ID (will be used as ID in
     * AllItemsMap).
     */
    public final long getNextID() {
        return this.counter.incrementAndGet();
    }

    /**
     * Add the item to the global list (AllItems ) and to the AllItems Map and
     * to the Lucene index: 3 fields are stored in Lucene index: -title
     * -description -all (contains both title+description) IMPORTANT: the
     * commitItems() must be called at some moment! without this the data would
     * not go to Lucene index!
     * @param item The item to add.
     * @throws IOException Something went wrong.
     */
    public final void addItem(final RssItem item) throws IOException {
        this.setAllDataChanged(true);
        long id = this.getNextID();
        item.setId(id);
        this.getAllItemsIdMap().put(id, item);
        this.getAllItems().add(item);
        // now add the item to LUCENE index
        final Document doc = new Document();
        doc.add(new StringField("id", Long.toString(item.getId()),Field.Store.YES));
        doc.add(new TextField("title", item.getTitle(), Field.Store.NO));
        doc.add(new TextField("description", item.getDescription(),Field.Store.NO));
        doc.add(new TextField("all", item.getTitleDescription(), Field.Store.NO));
        this.luceneIw.addDocument(doc);
    }

    /**
     * Propagate commit to Lucene; set luceneIndexSearcher to null.
     * @throws IOException Something went wrong.
     */
    public final void commitItems() throws IOException {
        this.luceneIndexSearcher = null;
        this.luceneIw.commit();
    }

    /**
     * Store the "all items" list of RssItems in the "all-items.yml".
     * @throws IOException Something went wrong.
     */
    public final void writeAllItems() throws IOException {
        writeToYamlFile(this.allItems, DEFAULT_YML_FILE);
    }

    public final boolean isAllDataChanged() {
        return this.isAllDataChanged;
    }

    /**
     * Setting this to "true" triggers saving the global list at program end.
     * @param isAllDataChanged
     */
    public final void setAllDataChanged(final boolean isChanged) {
        this.isAllDataChanged = isChanged;
    }

    public final boolean isAllItemsInitialized() {
        return this.isAllItemsInitialized;
    }

    public final void setAllItemsInitialized(final boolean isInitialized) {
        this.isAllItemsInitialized = isInitialized;
    }

    /**
     * First fill the index with data (see addItem() and commitItems()). ONLY
     * AFTER THIS this method can be called! (otherwise Lucene throws an
     * exception)
     * @throws IOException Something went wrong
     */
    private synchronized void initLuceneSearcher() throws IOException {
        if (this.luceneIndexSearcher != null)
            return;
        this.luceneIndexReader = DirectoryReader.open(this.luceneIndex);
        this.luceneIndexSearcher = new IndexSearcher(this.luceneIndexReader);
    }

    /**
     * Write the given list of items into YML file.
     * @param items A list to write.
     * @param fileName A file to store the items.
     * @throws IOException Something went wrong.
     */
    private static void writeToYamlFile(final List<RssItem> items,
            final String fileName) throws IOException {
        LOG.log(Level.INFO, "writing List<RssItem> to " + fileName + " ...");
        YamlWriter writer = null;
        try {
            writer = new YamlWriter(new FileWriter(fileName));
            writer.write(items);
        } 
        finally {
            if (writer != null) {
                try {
                    writer.close();
                } 
                catch (Throwable ex) {
                }
            }
        }
        LOG.log(Level.INFO, "done");
    }

    /**
     * Read from the given YML file, 
     * return list of items as in file.
     * @param fileName A file to read.
     * @return A list of RssItems, from the fileName.
     * @throws IOException Something went wrong
     */
    private static List<RssItem> readFromYamlFile(final String fileName)
            throws IOException {
        LOG.log(Level.INFO, "reading List<RssItem> from " + fileName + " ...");
        YamlReader reader = new YamlReader(new FileReader(fileName));
        @SuppressWarnings("unchecked")
        List<RssItem> result = (List<RssItem>) reader.read();
        LOG.log(Level.INFO, "{0} items read from {1}", new Object[] {result.size(), fileName});
        return result;
    }

    /**
     * Read the "all items" list of RssItems from the "all-items.yml".
     * @throws IOException Something went wrong.
     */
    private synchronized void readAllItems() throws IOException {
        LOG.log(Level.FINE, "begin");
        List<RssItem> tmpItems = new ArrayList<RssItem>(0);
        try {
            tmpItems = Data.readFromYamlFile(DEFAULT_YML_FILE);
        } 
        catch (FileNotFoundException ex) {
            LOG.log(Level.SEVERE, "all-items.yml not found - starting from scratch...");
        }
        // MUST be before "addItem" to avoid endless loop!
        this.setAllItemsInitialized(true);
        for (RssItem rssItem : tmpItems) {
            this.addItem(rssItem);
        }
        this.commitItems();
        this.initLuceneSearcher();
        // data initialized and not changed
        this.setAllDataChanged(false);
        LOG.log(Level.FINE, "end");
    }
}

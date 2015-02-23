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
 * Use Lucene query and index (index is in Data class, see
 * Data.getLuceneIndexSearcher()) to tag the RssItems. The assignTags() method
 * of this class performs the Lucene query (see "query" property) and attaches
 * the tag (see "tagName" property) to every RssIetm returned by the query.
 * @author stargazer33
 * @version $Id$
 */
public class LuceneTagger extends AbstractTagger {
    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(LuceneTagger.class
            .getName());

    /**
     * Which (Lucene) fields should we load when retrieving a Lucene document
     * from index?
     */
    private static final Set<String> FIELDS_TO_LOAD = new HashSet<String>();
    static {
        FIELDS_TO_LOAD.add("id");
    }

    /**
     * Lucene query.
     */
    private String query = "";

    @Override
    public final String toString() {
        String result = super.toString() + ", query: " + query;
        return result;
    }

    @Override
    public final void assignTags() throws Exception {
        Map<Long, RssItem> idMap = getData().getAllItemsIdMap();
        IndexSearcher searcher = getData().getLuceneIndexSearcher();
        Analyzer an = new StandardAnalyzer();
        QueryParser queryParser = null;

        // IMPORTANT: be default we query ONLY the "all" field!
        // specific fields "title" and "description" should be explicitly
        // specified
        queryParser = new QueryParser("all", an);
        Query q = queryParser.parse(query);
        LOG.log(Level.FINE, "tagName: " + getTagName() + ",\ntext_query: "
                + query + ",\nLucene_query: " + q.getClass().getSimpleName()
                + ", " + q.toString());
        // now search and iterate through search results (Lucene documents)
        TopDocs docsFound = searcher.search(q, Integer.MAX_VALUE);
        LOG.log(Level.FINE, "tagName: " + getTagName() + ", Items found: "
                + docsFound.scoreDocs.length);

        for (ScoreDoc scoreDoc : docsFound.scoreDocs) {
            // we need only the "id" field in the Lucene Document, we ignore
            // other fields
            Document doc = searcher.doc(scoreDoc.doc, FIELDS_TO_LOAD);
            // retrieve the RssItem by ID
            String strID = doc.get("id");
            long longID = Long.parseLong(strID);
            RssItem rssItem = idMap.get(longID);
            // attach the tag
            rssItem.getTags().add(getTagName());
            getData().setAllDataChanged(true);
        }
    }

    /**
     * The query in plain text.
     * @return The query in plain text.
     */
    public final String getQuery() {
        return query;
    }

    /**
     * Set the plain text query.
     * @param pQuery The query.
     */
    public final void setQuery(final String pQuery) {
        this.query = pQuery;
    }
}

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
package org.dfotos.rssfilter.src;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dfotos.rssfilter.RssItem;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * RSS/ATOM feed as data source.
 * @author stargazer33
 * @version $Id$
 */
public class SrcRss extends AbstractSrc {
    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(SrcRss.class.getName());

    /**
     * Perform HTTP GET, read the RSS/ATOM data, convert it to a list of
     * RssItem's.
     * @return RssItem's from the RSS/ATOM feed.
     * @exception Exception Something went wrong.
     */
    @Override
    public final List<RssItem> doRead() throws Exception {
        LOG.log(Level.FINE, "begin");
        SyndFeed feed = readTheFeed();
        List<RssItem> result = new ArrayList<RssItem>(50);
        int numNoDescr = 0;
        for (Object obj : feed.getEntries()) {
            SyndEntryImpl src = (SyndEntryImpl) obj;
            RssItem trg = new RssItem();
            result.add(trg);
            trg.setUrl(src.getLink());
            trg.setTitle(src.getTitleEx().getValue());
            if (null == src.getDescription() || null == src.getDescription().getValue()) {
                numNoDescr++;
            } 
            else {
                trg.setDescription(src.getDescription().getValue());
            }
            // TODO: in case of ATOM feed from stackoverflow.com
            // the src.getPublishedDate() returns NULL !!!
            trg.setPublished(src.getPublishedDate());
            trg.setSource(name);
        }
        if (numNoDescr > 0) {
            LOG.log(Level.INFO, "{0}, number of items without description: {1}",
                    new Object[]{getName(), numNoDescr}
            );
        }
        LOG.log(Level.FINE, "end");
        return result;
    }

    /**
     * Do all the preparations, read the URL, return it as SyndFeed.
     * @return The SyndFeed object.
     * @throws FeedException Something was wrong.
     * @throws IOException Something was wrong.
     */
    private SyndFeed readTheFeed() 
    throws FeedException, IOException 
    {
        LOG.log(Level.INFO, "{0}, reading...", new Object[] { getName() });
        URL feedUrl = new URL(url);
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(feedUrl));
        LOG.log(Level.INFO, "{0}, {1} RSS/ATOM items read", 
                new Object[] {getName(), feed.getEntries().size()}
                );
        return feed;
    }
}

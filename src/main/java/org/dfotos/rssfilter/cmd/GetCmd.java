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
package org.dfotos.rssfilter.cmd;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dfotos.rssfilter.App;
import org.dfotos.rssfilter.Data;
import org.dfotos.rssfilter.RssItem;
import org.dfotos.rssfilter.src.SrcIntf;

/**
 * Iterates over all configured data sources and retrieves the data (usually the
 * RSS feeds). Than removes the duplicates and than store the data (RssItem's) 
 * in the global lists.
 * @see  Data#addItem(RssItem)
 * @author stargazer33
 * @version $Id$
 */
public class GetCmd implements CommandIntf {
    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(GetCmd.class.getName());

    @Override
    public final void run(final List<String> args) throws Exception {
        LOG.log(Level.INFO, "begin");
        App.getData().getAllItems();
        final List<SrcIntf> sources = App.getConfig().getSources();
        for (final SrcIntf src : sources) {
            try {
                final List<RssItem> newList = src.doRead();
                this.removeDuplicates(newList, src);
                for (final RssItem rssItem : newList) {
                    App.getData().addItem(rssItem);
                }
                App.getData().commitItems();
            } 
            catch (final Throwable ex) {
                LOG.log(Level.SEVERE, "Exception processing source {0}: {1} ", new Object[]{src.getName(), ex});
            }
        }
        LOG.log(Level.INFO, "end");
    }

    @Override
    public final String getHelpStr() {
        return "Iterates over all configured data sources and retrieves the data (usually the RSS feeds)";
    }
    
    /**
     * In case items from the lst already exists in
     * App.getData().getAllItems() -->remove those items from lst.
     * @param lst A list with duplicates.
     * @param src Used for logging only.
     * @throws IOException in case things goes wrong
     */
    private void removeDuplicates(final List<RssItem> lst, final SrcIntf src)
            throws IOException {
        int nD = 0;
        final List<RssItem> allItems = App.getData().getAllItems();
        for (final Iterator<RssItem> newIterator = lst.iterator(); newIterator
                .hasNext();) {
            final RssItem newItem = (RssItem) newIterator.next();
            for (final RssItem oldItem : allItems) {
                if (this.isSimilar(newItem, oldItem)) {
                    newIterator.remove();
                    nD = nD + 1;
                    break;
                }
            }
        }
        LOG.log(Level.INFO, "{0}, duplicates removed: {1}", new Object[] {src.getName(), nD });
    }

    /**
     * Are two items similar?
     * @param one Item1
     * @param two Item2
     * @return If both items from the same source and have same URL: true.
     */
    private boolean isSimilar(final RssItem one, final RssItem two) {
        if (one == null || two == null) {
            return false;
        }
        if (one.getSource() == null || two.getSource() == null) {
            return false;
        }
        if (!one.getSource().equals(two.getSource())) {
            return false;
        }
        return one.getUrl().equals(two.getUrl());
    }
}

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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dfotos.rssfilter.App;
import org.dfotos.rssfilter.RssItem;

/**
 * The "Tag clear" command. It removes all tags from each RssItem in the
 * "all items" list.
 * @author stargazer33
 * @version $Id$
 */
public final class TagClearCmd implements CommandIntf {
    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(TagClearCmd.class.getName());

    @Override
    public void run(final List<String> args) throws Exception {
        LOG.log(Level.INFO, "begin");
        final List<RssItem> allItems = App.getData().getAllItems();
        App.getData().setAllDataChanged(true);
        for (final RssItem rssItem : allItems) {
            rssItem.getTags().clear();
        }
        LOG.log(Level.INFO, "end");
    }

    @Override
    public String getHelpStr() {
        return "removes all tags attached to rss items (usefull when debugging taggers)";
    }

}

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

import java.util.List;
import org.dfotos.rssfilter.RssItem;

/**
 * All "data sources" implements this interface.
 * @author stargazer33
 * @version $Id$
 */
public interface SrcIntf {

    /**
     * Get the data from some remote server and return it as a list of
     * RssItem's.
     * @return List of RssItem
     * @throws Exception If something goes wrong.
     */
    List<RssItem> doRead() throws Exception;

    /**
     * URL.
     * @return The URL of this data source.
     */
    String getUrl();

    /**
     * Set the URL.
     * @param url The URL to set.
     */
    void setUrl(String url);

    /**
     * The name of the datasource.
     * @return The name.
     */
    String getName();

    /**
     * Set the name of the datasource.
     * @param name The name to set.
     */
    void setName(String name);
}

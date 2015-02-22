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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Technology-independent container of
 * title+description+url+[some-meta-information].
 * Represents an "item" in the RSS feed or an "entry" in the ATOM feed.
 * Properties (JavaBean style):
 * <ul>
 * <li>title = RSS "title" tag
 * <li>description = RSS "description" tag or ATOM "summary" tag
 * <li>url = RSS "link" tag
 * <li>published = RSS "pubDate" or ATOM "published"
 * <li>tags ~ ATOM "category". these are the tags assigned to the item (The 
 * taggers doing this assignment). 
 * <li>source = internal field, the name of the "data source" used to produce
 * this item.
 * <li>id = internal field, application-wide ID of the item. It will be
 * assigned by Data.addItem(), used as key to store RssItem instances in Maps
 * </ul>
 * @author stargazer33
 * @version $Id$
 */
public final class RssItem {
    /**
     * Logger.
     */
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(RssItem.class.getName());

    /**
     * Contains title + "|" + description. See {@link #buildTitleDescription()}.
     * This field is marked as transient - to avoid saving it in the YML file.
     */
    private transient String _titleDescription;

    /**
     * Internal JVM-unique ID.
     */
    private long id;

    /**
     * Internal field, the name of the "data source" used to produce this item.
     */
    private String source = "";

    /**
     * RSS <description> tag or ATOM <summary>.
     */
    private String description = "";

    /**
     * RSS <link> tag.
     */
    private String url = "";

    /**
     * RSS <title> tag.
     */
    private String title = "";

    /**
     * RSS <pubDate> or ATOM <published>.
     */
    private Date published = new Date();

    /**
     * ATOM <category>. these are the tags assigned to the item. (The taggers
     * doing the assignment).
     */
    private Set<String> tags = new HashSet<String>(4);

    /**
     * Default constructor.
     */
    public RssItem() {
    }

    public void setId(final long pId) {
        this.id = pId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(final String pSrc) {
        this.source = pSrc;
    }

    public void setDescription(final String desc) {
        this.description = desc;
        _titleDescription = null;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String pUrl) {
        this.url = pUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String ttl) {
        this.title = ttl;
        _titleDescription = null;
    }

    public Date getPublished() {
        return published;
    }

    public void setPublished(final Date pPub) {
        // silently ignore null!
        if (pPub == null)
            return;
        this.published = pPub;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(final Set<String> pTags) {
        this.tags = pTags;
    }

    public String getDescription() {
        return description;
    }

    public long getId() {
        return id;
    }

    /**
     * Returns "title" +" | "+"description".
     * @return String containing both "title" and "description" fields separated
     * by " | ".
     */
    public String getTitleDescription() {
        if (_titleDescription != null) {
            return _titleDescription;
        }
        this.buildTitleDescription();
        return _titleDescription;
    }
    
    /**
     * Builds the title+description string.
     */
    private synchronized void buildTitleDescription() {
        if (_titleDescription != null) {
            return;
        }
        StringBuilder b = new StringBuilder();
        b.append(getTitle());
        b.append(" | ");
        b.append(getDescription());
        _titleDescription = b.toString();
    }

}

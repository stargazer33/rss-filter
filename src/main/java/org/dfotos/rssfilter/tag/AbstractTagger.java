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
import java.util.Set;
import java.util.logging.Logger;
import org.dfotos.rssfilter.Data;
import org.dfotos.rssfilter.RssItem;

/**
 * AbstractTagger - something that can attach tags to the RssItem's.
 * @author stargazer33
 * @version $Id$
 */
public abstract class AbstractTagger {
    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(AbstractTagger.class.getName());

    /**
     * Set of values pointing to t(itle), b(ody) or td(title+body) of the
     * RssItem.
     */
    private static final Set<String> DOCPART_DOMAIN = new HashSet<String>();
    static {
        DOCPART_DOMAIN.add("t");
        DOCPART_DOMAIN.add("d");
        DOCPART_DOMAIN.add("td");
    }
    
    /**
     * The tag to attach.
     */
    private String tagName = "";

    /**
     * The Data to operate on.
     */
    private Data data;

    /**
     * Which part of the RssItem this tagger checks? Domain: "t" - check title
     * "d" - check description "td" - check both title and description
     */
    private String docPart = new String();

    /**
     * Assign tags to the "all items" list of our Data instance.
     * @see Data#getAllItems()
     * @throws Exception Something went wrong
     */
    public abstract void assignTags() throws Exception;
    
    @Override
    public String toString() {
        String result = getClass().getSimpleName() + ", name: " + tagName;
        return result;
    }

    /**
     * Get the "docpart" this tagger checks. Domain: "t" - check title. "d" -
     * check description. "td" - check both title and description.
     * @return The "docpart" this tagger checks.
     */
    public final String getDocPart() {
        return docPart;
    }

    /**
     * Set the "dpcPart". Domain: "t" - check title. "d" - check description.
     * "td" - check both title and description.
     * @param docPart The docPart.
     */
    public final void setDocPart(final String docPart) {
        if (!DOCPART_DOMAIN.contains(docPart)) {
            throw new IllegalArgumentException(
                    ""+ docPart+ 
                    " is wrong argument for Tagger.setDocPart. Use one of these arguments: 't', 'd', 'td'.");
        }
        this.docPart = docPart;
    }

    /**
     * @return The name of tag.
     */
    public final String getTagName() {
        return tagName;
    }

    /**
     * Set the tag name.
     * @param pTagName The tag name.
     */
    public final void setTagName(final String pTagName) {
        this.tagName = pTagName;
    }

    /**
     * The Data instance.
     * @return The Data instance.
     */
    public final Data getData() {
        return data;
    }

    /**
     * Set the Data instance.
     * @param pData The Data instance.
     */
    public final void setData(final Data pData) {
        this.data = pData;
    }

    /**
     * From the given item returns either a title field, or a description field, 
     * or both.
     * @param item The item used to get a string.
     * @return From the given item returns a string containing either a title 
     * field, or a description field, or both according to getDocPart().
     */
    public final String getRssItemStr(final RssItem item) {
        String result = "";
        if ("t".equals(getDocPart())) {
            result = item.getTitle();
        } else if ("d".equals(getDocPart())) {
            result = item.getDescription();
        } else if ("td".equals(getDocPart())) {
            result = item.getTitleDescription();
        }
        return result;
    }
}

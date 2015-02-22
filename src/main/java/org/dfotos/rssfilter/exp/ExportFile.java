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
package org.dfotos.rssfilter.exp;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dfotos.rssfilter.RssItem;
import org.dfotos.rssfilter.util.Utils;
import com.sun.syndication.io.FeedException;

/**
 * Describes the "export file" for a list of RssItems. Each ExportFile exports
 * the RssItem according to tags specified in "tagsNOT", "tagsOR", "tagsAND".
 * Export performed in 3 steps:
 * <ul>
 * <li>1. The ExportFile instance created.
 * <li>2. For each RssItem the exportItem() method is called. This method
 * decides whether to export an item or not.
 * <li>3. All items prepated to export are exported using write() method.
 * </ul>
 * @author stargazer33
 * @version $Id$
 */
public final class ExportFile {

    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(ExportFile.class.getName());

    /**
     * Export file name.
     */
    private String name;

    /**
     * The AND tags.
     */
    private Set<String> tagsAND = new HashSet<String>();

    /**
     * The OR tags.
     */
    private Set<String> tagsOR = new HashSet<String>();

    /**
     * The NOT tags.
     */
    private Set<String> tagsNOT = new HashSet<String>();

    /**
     * Public constructor.
     */
    public ExportFile() {
    }

    /**
     * Creates readable description.
     * @return The (end-user) readable description of the object.
     */
    public String getDescription() {
        String result = "";
        if (!tagsNOT.isEmpty()) {
            result = result + "NOT tags: ( " + tagsNOT.toString() + " ) ";
        }
        if (!tagsOR.isEmpty()) {
            result = result + "OR tags: ( " + tagsOR.toString() + " ) ";
        }
        if (!tagsAND.isEmpty()) {
            result = result + "AND tags: ( " + tagsAND.toString() + " )";
        }
        return result;
    }

    /**
     * Does the specified "item" meets the conditions in "tagsNOT", "tagsOR",
     * "tagsAND" ? This method checks all the conditions, and it can add the
     * item to "toExport" list.
     * @param item An item to check.
     * @param toExport Container with items ready for export.
     */
    public void exportItem(final RssItem item, final List<RssItem> toExport) {
        Set<String> itemTags = item.getTags();
        if (itemTags.isEmpty()) {
            return;
        }
        for (String tag : itemTags) {
            if (tagsNOT.contains(tag)) {
                // any "NOT" tags in our RssItem? -> skip other checks and
                // return
                return;
            }
        }
        for (String tag : itemTags) {
            if (tagsOR.contains(tag)) {
                // any "OR" tags in our RssItem? -> export it, skip other checks
                toExport.add(item);
                return;
            }
        }
        if (!tagsAND.isEmpty() && itemTags.containsAll(tagsAND)) {
            // last check
            toExport.add(item);
        }
    }

    /**
     * Writes the given list to the (ATOM) file.
     * @param toExport The Items to export.
     */
    public void write(final List<RssItem> toExport) {
        String fileName = this.getName() + ".atom";
        try {
            Utils.writeItemsToAtomFile(
                    toExport, 
                    this.getName(), 
                    "file:///" + fileName,
                    this.getDescription(), 
                    fileName
                    );
        } 
        catch (IOException | FeedException ex) {
            LOG.log(Level.SEVERE, "Error writing to: " + fileName, ex);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Set<String> getTagsAND() {
        return tagsAND;
    }

    public void setTagsAND(final Set<String> tagsAND) {
        this.tagsAND = tagsAND;
    }

    public Set<String> getTagsOR() {
        return tagsOR;
    }

    public void setTagsOR(final Set<String> tagsOR) {
        this.tagsOR = tagsOR;
    }

    public Set<String> getTagsNOT() {
        return tagsNOT;
    }

    public void setTagsNOT(final Set<String> tagsNOT) {
        this.tagsNOT = tagsNOT;
    }

}

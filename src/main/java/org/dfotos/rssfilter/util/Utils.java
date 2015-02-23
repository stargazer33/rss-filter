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
package org.dfotos.rssfilter.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dfotos.rssfilter.Config;
import org.dfotos.rssfilter.RssItem;
import org.dfotos.rssfilter.exp.ExportFile;
import org.dfotos.rssfilter.src.SrcIntf;
import org.dfotos.rssfilter.src.SrcRss;
import org.dfotos.rssfilter.tag.LuceneTagger;
import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlWriter;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * A collection of (unrelated) static methods used across the project.
 * @author stargazer33
 * @version $Id$
 */
public final class Utils {
    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(Utils.class.getName());

    /**
     * List of classes used in *.yml files.
     */
    @SuppressWarnings("rawtypes")
    private static final List<Class> YML_CLASSES = new ArrayList<Class>(10);
    static {
        YML_CLASSES.add(Config.class);
        YML_CLASSES.add(SrcIntf.class);
        YML_CLASSES.add(ExportFile.class);
        YML_CLASSES.add(SrcRss.class);
        YML_CLASSES.add(LuceneTagger.class);
    }
    
    /**
     * Regexp for characters allowed in an XML 1.0 document.
     */
    private static final String XML10_PATTERN = "[^" + "\u0009\r\n" + "\u0020-\uD7FF"
            + "\uE000-\uFFFD" + "\ud800\udc00-\udbff\udfff" + "]";

    /**
     * Regexp for end of line.
     */
    private static final String EOL_PATTERN = "(\r\n|\n)";
    
    /**
     * RssItem comparator, "by published date", ascending.
     */
    private static final Comparator<RssItem> RSS_ITEM_PUB_DATE_COMPARATOR_ASC =
            new Comparator<RssItem>() {
                @Override
                public int compare(RssItem o1, RssItem o2) {
                    if (o1.getPublished() == null || o2.getPublished() == null){
                        return 0;
                    }
                    return o1.getPublished().compareTo(o2.getPublished());
                }
            };

    /**
     * RssItem comparator, "by published date", descending.
     */
    private static final Comparator<RssItem> RSS_ITEM_PUB_DATE_COMPARATOR_DESC =
            new Comparator<RssItem>() {
                @Override
                public int compare(RssItem o1, RssItem o2) {

                    if (o1.getPublished() == null || o2.getPublished() == null)
                        return 0;

                    return (o1.getPublished().compareTo(o2.getPublished()))
                            * (-1);
                }
            };
    
    /**
     * Hidden constructor.
     */
    private Utils(){
    }
    
    /**
     * Conversion JSON(string)->List containing POJO's.
     * @param pJson JSON as string
     * @param pType Type specification of items in the returned list.
     * @return list Of objects created from resultStrJson. Type of each objects
     * in the List: as specified by "collectionType".
     */
    @SuppressWarnings("rawtypes")
    public static List convertJsonToPojo(final String pJson, final Type pType) 
    {
        List<RssItem> result = new ArrayList<RssItem>();
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new StringReader(pJson));
        reader.setLenient(true);
        result = gson.fromJson(reader, pType);
        return result;
    }

    /**
     * Store the listRssItem in the (YAML) fileName.
     * @param listRssItem Items to store.
     * @param fileName The file.
     * @throws IOException Something went wrong.
     */
    public static void writeItemsToYamlFile(
            final List<RssItem> listRssItem,
            final String fileName
            ) 
            throws IOException 
    {
        LOG.log(Level.FINE, "begin");
        YamlWriter writer = null;
        try {
            writer = new YamlWriter(new FileWriter(fileName));
            Utils.setClassTags(writer.getConfig());
            LOG.log(Level.INFO, "writing " + listRssItem.size()
                    + " items to file: " + fileName + "  ...");
            writer.write(listRssItem);
        } 
        finally {
            // if not null -- always close writer
            if (writer != null) {
                try {
                    writer.close();
                } 
                catch (Throwable e) {}
            }
        }
        LOG.log(Level.FINE, "end");
    }

    /**
     * Takes Set of Strings and returns it as a List<SyndCategoryImpl>.
     * @param set A set to convert.
     * @return A new List with contents of the set.
     */
    public static List<SyndCategoryImpl> convertSetToSyndCategories( final Set<String> set) 
    {
        List<SyndCategoryImpl> result = new ArrayList<SyndCategoryImpl>();
        for (String str : set) {
            SyndCategoryImpl cat = new SyndCategoryImpl();
            result.add(cat);
            cat.setName(str);
        }
        return result;
    }

    /**
     * Writes specified listRssItem into (RSS/ATOM) file "fileName". Set title,
     * link, feedDescription correspondingly.
     * @param listRssItem
     * @param title
     * @param link
     * @param feedDescription
     * @param fileName
     * @throws IOException
     * @throws FeedException
     */
    public static void writeItemsToAtomFile(
            final List<RssItem> listRssItem,
            final String title, 
            final String link, 
            final String feedDescription, 
            final String fileName
            )
            throws IOException, FeedException 
    {
        SyndFeed feed = new SyndFeedImpl();
        List<SyndEntry> entries = new ArrayList<SyndEntry>();
        feed.setEntries(entries);

        /*
         * possible feed types:
         * 
         * rss_0.91N rss_0.93 rss_0.92 rss_1.0 rss_0.94 rss_2.0 rss_0.91U
         * rss_0.9 atom_1.0 atom_0.3
         */

        feed.setFeedType("atom_1.0");
        feed.setLanguage("en");
        feed.setAuthor("rss-filter");

        feed.setTitle(title);
        feed.setLink(link);
        feed.setDescription(feedDescription);

        SyndEntry entry;
        SyndContent description;
        for (RssItem rssItem : listRssItem) {
            entry = new SyndEntryImpl();
            description = new SyndContentImpl();
            entry.setDescription(description);
            entry.setAuthor(rssItem.getSource());
            entry.setPublishedDate(rssItem.getPublished());
            entry.setTitle(rssItem.getTitle());
            entry.setLink(rssItem.getUrl());
            entry.setCategories(Utils.convertSetToSyndCategories(rssItem
                    .getTags()));
            description.setType("text/html");
            description.setValue(rssItem.getDescription());
            entries.add(entry);
        }

        Writer writer = null;
        try {
            writer = new FileWriter(fileName);
            SyndFeedOutput output = new SyndFeedOutput();
            LOG.log(Level.INFO, "writing " + listRssItem.size()
                    + " items to file: " + fileName + "  ...");
            output.output(feed, writer);
        } 
        finally {
            // if not null -- always close writer
            if (null != writer) {
                try {
                    writer.close();
                } 
                catch (Throwable e) {
                }
            }
        }
        LOG.log(Level.INFO, "done " + fileName);
    }


    /**
     * Sort given listItem by published date.
     * 
     * @param listItems The list to sort
     * @param asc True means "ascending"
     */
    public static void sortItemsByDate(final List<RssItem> listItems, final boolean asc) 
    {
        if (asc) {
            Collections.sort(listItems, RSS_ITEM_PUB_DATE_COMPARATOR_ASC);
        } 
        else {
            Collections.sort(listItems, RSS_ITEM_PUB_DATE_COMPARATOR_DESC);
        }
    }


    /**
     * Removes invalid characters.
     * @param in The input string.
     * @return "in" The input string but without the invalid (from XML 1.0 spec 
     * point of view) characters.
     */
    public static String stripNonValidXMLCharacters(final String in) {
        if (null == in) {
            return "";
        }
        return in.replaceAll(XML10_PATTERN, "");
    }

    /**
     * Make HTML tag from the end-of-lines.
     * @param in A string with end-of-lines.
     * @return "in" with all end-of-lines replaced with HTML tag "<br />
     * "
     */
    public static String convertEOLtoHTML(final String in) {
        if (null == in) {
            return "";
        }
        return in.replaceAll(EOL_PATTERN, "<br />");
    }

    /**
     * Apply two "fix" method to the input string.
     * @param in The input string.
     * @return apply both stripNonValidXMLCharacters and convertEOLtoHTML to in,
     * return "fixed" string.
     */
    public static String fixStringForRss(final String in) {
        return convertEOLtoHTML(Utils.stripNonValidXMLCharacters(in));
    }

    /**
     * Return simple class name in lower case.
     * @param c The class.
     * @return "Simple" (without package prefix) class name of "c"; in lower
     * case.
     */
    @SuppressWarnings("rawtypes")
    public static String getSimpleClassName(final Class c) {
        return c.getSimpleName().toLowerCase();
    }

    /**
     * Apply so-called "class tags" to the conf this replaces the the "full"
     * class names in the YML with the "simple" class names. Replacement is done
     * for all classes in the YML_CLASSES list
     * @param conf The configuration.
     * @throws IOException Something went wrong.
     */
    @SuppressWarnings("rawtypes")
    public static void setClassTags(final YamlConfig conf) 
    throws IOException 
    {
        for (Class cl : YML_CLASSES) {
            conf.setClassTag(Utils.getSimpleClassName(cl), cl);
        }
    }

}

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
import org.dfotos.rssfilter.tag.Tagger;

/**
 * The "tag" command. Runs all configured "taggers"
 * @author stargazer33
 * @version $Id$
 */
public class TagCmd implements CommandIntf {
    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(TagCmd.class.getName());

    @Override
    public final void run(final List<String> args) throws Exception {
        LOG.log(Level.INFO, "begin");
        final List<Tagger> allTaggers = App.getConfig().getTaggers();
        for (final Tagger tagger : allTaggers) {
            tagger.setData(App.getData());
            tagger.assignTags();
        }
        LOG.log(Level.INFO, "end");
    }

    @Override
    public final String getHelpStr() {
        return "attaches the tags to the rss items (actually delegates this \"taggers\")";
    }

}

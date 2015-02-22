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

/**
 * Code that runs just before the program exit. In case the "all items" list was
 * modified --> than we will save it on disk.
 * @author stargazer33
 * @version $Id$
 */
public class BeforeExit implements CommandIntf {
    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(BeforeExit.class.getName());

    @Override
    public final void run(final List<String> args) throws Exception {
        LOG.log(Level.FINE, "begin");
        if (App.getData().isAllDataChanged()) {
            App.getData().writeAllItems();
        }
        LOG.log(Level.FINE, "end");
    }

    @Override
    public final String getHelpStr() {
        return "";
        // internal command - the end-user will never see "help"
    }
}

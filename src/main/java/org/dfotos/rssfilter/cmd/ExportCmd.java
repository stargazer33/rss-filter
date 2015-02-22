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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dfotos.rssfilter.App;
import org.dfotos.rssfilter.RssItem;
import org.dfotos.rssfilter.exp.ExportFile;

/**
 * Implementation of "export" command. It runs all configured exports. And
 * exports should write the RssItem's from App.getData().getAllItems() into
 * user-friendly (*.atom or something else) output format. The actual job
 * delegated to the ExportFile instances.
 * @author stargazer33
 * @version $Id$
 */
public final class ExportCmd implements CommandIntf {

    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(ExportCmd.class.getName());

    @Override
    public void run(final List<String> args) throws Exception {
        LOG.log(Level.INFO, "begin");
        final List<RssItem> allItems = App.getData().getAllItems();
        final List<ExportFile> exports = App.getConfig().getExportFiles();
        for (final ExportFile exportFile : exports) {
            final List<RssItem> toExport = new ArrayList<RssItem>(50);
            for (final RssItem rssItem : allItems) {
                exportFile.exportItem(rssItem, toExport);
            }
            exportFile.write(toExport);
        }
        LOG.log(Level.INFO, "end");
    }

    @Override
    public String getHelpStr() {
        return "goes through all configured \"exportFiles\" and write files (usually these are *.atom files in the current dir)";
    }

}

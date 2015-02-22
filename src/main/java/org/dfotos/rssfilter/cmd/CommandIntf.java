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

/**
 * All rssfilter commands should implement this interface.
 * @author stargazer33
 * @version $Id$
 */
public interface CommandIntf {

    /**
     * The actual command implementation should be in this method.
     * @param args Optional list of the (command-line) arguments
     * @throws Exception if something goes wrong
     */
    void run(List<String> args) throws Exception;

    /**
     * Provides the "help string".
     * @return Description of this command (for end-user)
     */
    String getHelpStr();

}

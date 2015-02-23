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

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import org.dfotos.rssfilter.App;

/**
 * Collection of static methods dealing with logs.
 * @author stargazer33
 * @version $Id$
 */
public final class LogHelper {
    /**
     * Hidden default constructor.
     */
    private LogHelper() {
    }

    /**
     * Reads the given logConfig(should be in CLASSPATH), than initialize the
     * LogManager with this config. Hardcodes the format of SimpleFormatter
     * without date/time info.
     * @param logConfig File name of the logging.properties used to initialize
     * the logging subsystem.
     * @throws IOException Something went wrong
     */
    public static void initLogs(final String logConfig)
            throws IOException {
        InputStream logPropCfg = App.class.getResourceAsStream(logConfig);
        if (logPropCfg != null) {
            try {
                LogManager.getLogManager().readConfiguration(logPropCfg);
                System.getProperties().setProperty(
                        "java.util.logging.SimpleFormatter.format",
                        "%4$s %2$s %5$s%6$s%n");
            } 
            catch (SecurityException | IOException ex) {
                System.out.printf(
                        "Error %s reading log configuration from %s ",
                        ex.toString(), 
                        logConfig);
                throw new IOException(ex.fillInStackTrace());
            }
        } 
        else {
            System.out.println("Not found: " + logConfig);
        }
    }
}

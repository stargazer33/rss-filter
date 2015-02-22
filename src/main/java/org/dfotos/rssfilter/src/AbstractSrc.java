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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

/**
 * The base abstract class. Helps to implement the SrcIntf.
 * @author stargazer33
 * @version $Id$
 */
public abstract class AbstractSrc implements SrcIntf {
    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(AbstractSrc.class.getName());

    /**
     * The URL.
     */
    protected String url;

    /**
     * The name of the datasource.
     */
    protected String name;

    /**
     * We use it to do requests.
     */
    protected OkHttpClient client;

    /**
     * Performs the HTTP GET request using the "url" field. Returns the response
     * body or throws the IOException in case the URL can not be retrieved.
     * @return The response body.
     * @throws IOException Something went wrong.
     */
    public final String doHttpGet() throws IOException {
        LOG.log(Level.INFO, "{0}, reading...", new Object[] { getName() });
        if (client == null) {
            client = new OkHttpClient();
        }
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        long respLength = 0;
        if (response.body() != null) {
            respLength = response.body().contentLength();
        }
        if (!response.isSuccessful()) {
            LOG.log(Level.INFO, "HTTP response: " + response + " length: "+ respLength);
            throw new IOException("Server error: " + response);
        }
        String tmp = response.body().string();
        LOG.log(Level.FINE, "{0}, {1} bytes read", new Object[]{getName(), tmp.length()});
        return tmp;
    }

    @Override
    final public String getUrl() {
        return url;
    }

    @Override
    final public void setUrl(final String pUrl) {
        url = pUrl;
    }

    @Override
    final public String getName() {
        return name;
    }

    @Override
    final public void setName(final String pName) {
        name = pName;
    }

}

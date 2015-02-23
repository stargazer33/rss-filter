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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.dfotos.rssfilter.exp.ExportFile;
import org.dfotos.rssfilter.src.SrcIntf;
import org.dfotos.rssfilter.tag.AbstractTagger;
import org.dfotos.rssfilter.util.Utils;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

/**
 * YML-file based configuration of the program. Contains three lists: -sources
 * -taggers -exportFiles Call createInstance() factory method to
 * create+initialize the Config.
 * @author stargazer33
 * @version $Id$
 */
public final class Config {
    /**
     * Logger.
     */
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(Config.class.getName());

    /**
     * Our (data) sources. most of them are RSS, but there could be variants.
     */
    private List<SrcIntf> sources = new ArrayList<SrcIntf>();

    /**
     * List of our outputs.
     */
    private List<ExportFile> exportFiles = new ArrayList<ExportFile>();

    /**
     * List of taggers.
     */
    private List<AbstractTagger> abstractTaggers = new ArrayList<AbstractTagger>();

    /**
     * Hidden constructor.
     */
    private Config() {
    }

    /**
     * @return List of sources.
     */
    public List<SrcIntf> getSources() {
        return this.sources;
    }

    public void setSources(final List<SrcIntf> src) {
        this.sources = src;
    }
    
    /**
     * @return List of taggers.
     */
    public List<AbstractTagger> getTaggers() {
        return this.abstractTaggers;
    }

    public void setTaggers(final List<AbstractTagger> tagg) {
        this.abstractTaggers = tagg;
    }
    
    /**
     * @return List of export files.
     */
    public List<ExportFile> getExportFiles() {
        return this.exportFiles;
    }

    public void setExportFiles(final List<ExportFile> expF) {
        this.exportFiles = expF;
    }
    
    /**
     * Factory method creating new Config.
     * @return New Config instance created from the given configFileName (in the
     * YML format).
     * @param configFileName File in YML format.
     * @throws IOException If creation goes wrong.
     */
    static Config createInstance(final String configFileName) 
            throws IOException 
    {
        final YamlReader reader = new YamlReader(new FileReader(configFileName));
        Utils.setClassTags(reader.getConfig());
        final Config result = (Config) reader.read();
        return result;
    }

    /**
     * Store configuration in the given YML file fileName.
     * @param fileName File to store configuration
     * @exception IOException When file writing goes wrong
     */
    void writeToYamlFile(final String fileName) throws IOException {
        YamlWriter writer = null;
        try {
            writer = new YamlWriter(new FileWriter(fileName));
            Utils.setClassTags(writer.getConfig());
            writer.write(this);
        } 
        finally {
            if (writer != null) {
                try {
                    writer.close();
                } 
                catch (Throwable ex) {
                }
            }
        }
    }    
}

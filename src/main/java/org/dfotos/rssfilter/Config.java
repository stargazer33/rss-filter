package org.dfotos.rssfilter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.dfotos.rssfilter.exp.ExportFile;
import org.dfotos.rssfilter.src.SrcIntf;
import org.dfotos.rssfilter.tag.Tagger;
import org.dfotos.rssfilter.util.Utils;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

/**
 * YML-file based configuration of the program
 * 
 *  Contains three lists:
 *  
 *  -sources
 *  -taggers
 *  -exportFiles
 *
 * Call createInstance() to create+initialize the Config
 *
 */
public class Config {

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger( Config.class.getName() );	
	
	
	//our (data) sources. 
	//most of them are RSS, but there could be variants
	private List<SrcIntf> sources=new ArrayList<SrcIntf> ();
	
	//list of our outputs
	private List<ExportFile> exportFiles=new ArrayList<ExportFile> ();
	
	//list of taggers
	private List<Tagger> taggers=new ArrayList<Tagger> ();
	
	/*****************************************************/
	
	/**
	 * Factory method creating new Config 
	 * @return creates new Config reading it from the configFileName (YML format)
	 * 
	 * @throws IOException
	 */
	static Config createInstance( String configFileName) 
	throws IOException{
		
		YamlReader reader = new YamlReader(new FileReader(configFileName));
		Utils.setClassTags( reader.getConfig() );
		Config result = (Config) reader.read();
	
		//hack(result);
		
		return result;
	}

	/**
	 * Store configuration in YML file fileName 
	 * @param fileName
	 */
	void writeToYamlFile(String fileName)
	throws IOException
	{
		YamlWriter writer=null;
	    try {
			writer = new YamlWriter(new FileWriter(fileName));
			Utils.setClassTags( writer.getConfig() );
			writer.write(this);
		} 
	    finally{
		    if(writer!=null){
		    	try {
					writer.close();
				} 
		    	catch (Throwable e) {}
		    }
	    }
	}
	
	private Config(){
	}
	
	public List<SrcIntf> getSources(){
		return sources;
	}

	public void setSources(List<SrcIntf> sources) {
		this.sources = sources;
	}

	public List<Tagger> getTaggers() {
		return taggers;
	}

	public void setTaggers(List<Tagger> taggers) {
		this.taggers = taggers;
	}

	public List<ExportFile> getExportFiles() {
		return exportFiles;
	}

	public void setExportFiles(List<ExportFile> exportFiles) {
		this.exportFiles = exportFiles;
	}
}

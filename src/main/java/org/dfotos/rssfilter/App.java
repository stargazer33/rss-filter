package org.dfotos.rssfilter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.dfotos.rssfilter.cmd.*;
import org.dfotos.rssfilter.util.LogHelper;

/**
 * The main method lives here
 *
 */
public class App 
{
	private static final Logger log = Logger.getLogger( App.class.getName() );	
	
	//1:1 between a command on the command line (like "get"), and the implementation
	private static Map<String, CommandIntf> commands;
	static{
    	//initialize the map of commands
    	commands=new LinkedHashMap <String, CommandIntf>();
    	commands.put( "get", new GetCmd() );
    	commands.put( "tag", new TagCmd() );
    	commands.put( "tagclear", new TagClearCmd() );
    	commands.put( "export", new ExportCmd() );
	}

	/**
	 * Global configuration instance
	 */
	private static Config configInstance;
	
	/**
	 * Global Data instance
	 */
	private static Data dataInstance=new Data();
	
	private static String configFileName="config.yml";
	
    public static void main( String[] args ) 
    throws SecurityException, IOException
    {

    	//default logging properties, INFO level
    	String loggingConf="/logging.properties";
    	
    	//process command line
    	CommandLineParser parser = new PosixParser();
    	@SuppressWarnings("static-access")
		Option conffile   = OptionBuilder.withArgName( "file" )
                .hasArg()
                .withDescription(  "read configuration from the *.yml file (default: "+configFileName+" )" )
                .create( "c" );    	
    	Option quiet = new Option( "q", "logging: be extra quiet" );    	    	
    	Option verbose = new Option( "v", "logging: be extra verbose" );     	
    	Option help = new Option( "h", "print help" );     	
    	Options options = new Options();
    	options.addOption( conffile );
    	options.addOption( quiet );
    	options.addOption( verbose );    	
    	options.addOption( help );    	
    	CommandLine cmdLine;
    	
        try {
        	cmdLine = parser.parse( options, args );
        }
        catch( ParseException exp ) {
            System.err.println( exp.getMessage() );
            return; //makes no sense to continue
        }    	
    	
        //check command line options
        if ( cmdLine.hasOption( "q" ) ) {
    		loggingConf="/logging.q.properties";
    	}
    	else if ( cmdLine.hasOption( "v" ) ) {
    		loggingConf="/logging.v.properties";
    	}
        if ( cmdLine.hasOption( "h" ) ) {
        	printHelp(options);
        	return; //makes no sense to continue
    	}
        if ( cmdLine.hasOption( "c" ) ) {
        	configFileName = cmdLine.getOptionValue("c");
        }
        
        //do log init
    	LogHelper.initLogs(loggingConf);
    	
    	// now:
    	// 1. read configuration
    	// 2  init Data
    	// 2. run all commands ( as specified on the command line )
    	// 3. (finally) run cleanup command 
    	try{
        	readConfig();
        	getData().init();
        	
        	
        	if( cmdLine.getArgList().isEmpty() ){
        		//nothing on the command line - show help
        		printHelp(options);
        	}
        	else{
        		//run all specified commands
        		for (Object o: cmdLine.getArgList()) 
        		{
        			execCmd( (String)o );
        		}
        	}
    	}
    	catch(Throwable t)
    	{
    		log.log( Level.SEVERE,"", t);
    	}
    	finally{
    		BeforeExit exit=new BeforeExit();
    		try {
    			exit.run(new ArrayList<String>(0));
			} 
    		catch (Exception e) {
        		log.log( Level.SEVERE,"Exception running CleanupCmd: ", e);
			}
    	}
    	
    	
    } //main


    
    /**
     * This hack is needed to get "usage" string printed in System.out
     * WITHOUT a final new line!  
     * 
     * Unfortunately HelpFormatter.printUsage allows no customization,
     * therefore such a trick needed
     */
    static class MyPrintWriter extends PrintWriter{
    	public MyPrintWriter( OutputStream out ){
    		super(out);
    	}
    	
		public void println(String x) {
			print(x); //no new line here!
		}
    }

    /**
     * Prints "help" to the System.out
     * @param options
     */
	private static void printHelp(Options options) {
		System.out.println("");
		HelpFormatter formatter = new HelpFormatter();
        PrintWriter pw1 = new MyPrintWriter(System.out);
        pw1.print(" ");
        formatter.printUsage(pw1, formatter.getWidth(), "rss-filter", options);
        pw1.println(" <command> [command2] [command3] ... [commandN]");
        pw1.flush();

        System.out.println("\n\n Commands:");
        commands.keySet();
        for ( String cmd : commands.keySet() ) {
        	String tabs="\t\t";
        	if(cmd.length() > 6	)
        		tabs="\t";
        	System.out.println(" "+cmd+tabs+commands.get(cmd).getHelpStr() );
		}
        
        System.out.println("\n Options:");
        PrintWriter pw2 = new PrintWriter(System.out);
        formatter.printOptions(pw2, formatter.getWidth(), options, formatter.getLeftPadding(), formatter.getDescPadding() );
        pw2.flush();
        
        System.out.println("\n Example:");
        System.out.println(" rss-filter -v get tag export \n"); 
	}
    
    
    /**
     * Find an implementation for the cmdName argument; run the implementation; print err. message in 
     * case the implementation can not be found
     * 
     * @param cmdName
     * @throws Exception
     */
    private static void execCmd(String cmdName)
    throws Exception    
    {

    	CommandIntf command=commands.get(cmdName);
		if(command == null){
			System.err.println( "Unrecognized command: "+cmdName);
	    	return;
		}    		
    	
		command.run( new ArrayList<String>() );
    }
    
    /**
     * Create and init the conf file instance
     * @throws Exception
     */
	private static void readConfig() 
	throws Exception
	{
		log.log( Level.FINE,"begin");
		configInstance=Config.createInstance(configFileName);
		log.log( Level.FINE,"end");
	}
	
	public static Config getConfig(){
		return configInstance;
	}

	public static Data getData(){
		return dataInstance;
	}

}

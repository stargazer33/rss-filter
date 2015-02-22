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
 * The main method lives here. It performs the initialization, command-line
 * parsing, displays help (if necessary) and delegates the actual job to the
 * "commands", as specified on the command line. At the end it calls an instance
 * of BeforeExit.
 * @author stargazer33
 * @version $Id$
 */
public final class App {
    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(App.class.getName());

    /**
     * The 1:1 map between a command on the command line (like "get"), and the
     * implementation, like GetCmd.
     */
    private static final Map<String, CommandIntf> COMMANDS;
    static {
        COMMANDS = new LinkedHashMap<String, CommandIntf>();
        COMMANDS.put("get", new GetCmd());
        COMMANDS.put("tag", new TagCmd());
        COMMANDS.put("tagclear", new TagClearCmd());
        COMMANDS.put("export", new ExportCmd());
    }

    /**
     * Global configuration instance. Will be initialized later.
     */
    private static Config configInstance;

    /**
     * Global Data instance.
     */
    private static final Data DATA_INSTANCE = new Data();

    /**
     * Default config file name. Can be overwritten on the command line.
     */
    private static String configFileName = "config.yml";

    /**
     * Private default constructor.
     */
    private App() {
    }
        
    /**
     * Execution begins here.
     * 
     * @param args Command-line arguments.
     * @throws IOException Something else went wrong.
     */
    public static void main(final String[] args) 
    throws IOException {
        String loggingConf = "/logging.properties";
        CommandLineParser parser = new PosixParser();
        Options options = new Options();
        initCmdLineOptions(options);
        CommandLine cmdLine;
        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException exp) {
            System.err.println(exp.getMessage());
            return;
        }
        if (cmdLine.hasOption("q")) {
            loggingConf = "/logging.q.properties";
        } else if (cmdLine.hasOption("v")) {
            loggingConf = "/logging.v.properties";
        }
        if (cmdLine.hasOption("h")) {
            printHelp(options);
            return;
        }
        if (cmdLine.hasOption("c")) {
            configFileName = cmdLine.getOptionValue("c");
        }
        LogHelper.initLogs(loggingConf);
        try {
            readConfig();
            getData().init();
            if (cmdLine.getArgList().isEmpty()) {
                printHelp(options);
            } else {
                for (final Object arg : cmdLine.getArgList()) {
                    execCmd((String) arg);
                }
            }
        } 
        catch (final Throwable ex) {
            LOG.log(Level.SEVERE, "", ex);
        } 
        finally {
            final BeforeExit exit = new BeforeExit();
            try {
                exit.run(new ArrayList<String>(0));
            } 
            catch (final Exception ex) {
                LOG.log(Level.SEVERE, "Exception running CleanupCmd: ", ex);
            }
        }
    }

    /**
     * Initialize command-line options.
     * @param options An object to initialize.
     */
    private static void initCmdLineOptions(final Options options) {
        @SuppressWarnings("static-access")
        final Option conffile;
        conffile = OptionBuilder
                        .withArgName("file")
                        .hasArg()
                        .withDescription(
                                "read configuration from the *.yml file (default: "
                                        + configFileName + " )").create("c");
        final Option quiet = new Option("q", "logging: be extra quiet");
        final Option verbose = new Option("v", "logging: be extra verbose");
        final Option help = new Option("h", "print help");
        options.addOption(conffile);
        options.addOption(quiet);
        options.addOption(verbose);
        options.addOption(help);
    }

    /**
     * Get the configuration file instance.
     * @return The configuration file instance.
     */
    public static Config getConfig() {
        return configInstance;
    }

    /**
     * Get the global Data instance.
     * @return The global Data instance.
     */
    public static Data getData() {
        return DATA_INSTANCE;
    }    

    /**
     * Finds an implementation for the cmdName argument. 
     * Than runs the implementation.
     * Prints an err. message in case the implementation can not be found.
     * @param cmd Name of the command, as in the command line.
     * @throws Exception Something went wrong in command execution.
     */
    private static void execCmd(final String cmd) throws Exception {
        final CommandIntf command = COMMANDS.get(cmd);
        if (command == null) {
            System.err.println("Unrecognized command: " + cmd);
            return;
        }
        command.run(new ArrayList<String>(0));
    }

    /**
     * Create and init the configuration file instance.
     * @throws IOException Means initialization failed 
     */
    private static void readConfig() 
    throws IOException {
        LOG.log(Level.FINE, "begin");
        configInstance = Config.createInstance(configFileName);
        LOG.log(Level.FINE, "end");
    }

    /**
     * Prints "help" to the System.out.
     * @param options Our command line options.
     */
    private static void printHelp(final Options options) {
        System.out.println("");
        HelpFormatter formatter = new HelpFormatter();
        PrintWriter pw1 = new MyPrintWriter(System.out);
        pw1.print(" ");
        formatter.printUsage(pw1, formatter.getWidth(), "rss-filter", options);
        pw1.println(" <command> [command2] [command3] ... [commandN]");
        pw1.flush();
        System.out.println("\n\n Commands:");
        COMMANDS.keySet();
        for (String cmd : COMMANDS.keySet()) {
            String tabs = "\t\t";
            if (cmd.length() > 6){
                tabs = "\t";
            }
            System.out.println(" " + cmd + tabs
                    + COMMANDS.get(cmd).getHelpStr());
        }
        System.out.println("\n Options:");
        PrintWriter pw2 = new PrintWriter(System.out);
        formatter.printOptions(pw2, formatter.getWidth(), options,
                formatter.getLeftPadding(), formatter.getDescPadding());
        pw2.flush();
        System.out.println("\n Example:");
        System.out.println(" rss-filter -v get tag export \n");
    }
    
    /**
     * This hack is needed to get "usage" string printed in System.out WITHOUT a
     * final new line! Unfortunately HelpFormatter.printUsage allows no
     * customization, therefore such a trick needed.
     * This inner class used in printHelp() only.
     */
    static private final class MyPrintWriter extends PrintWriter {
        /**
         * Constructor.
         * @param out The output stream.
         */
        public MyPrintWriter(final OutputStream out) {
            super(out);
        }

        /**
         * Prints X but without printing a new line.
         *  @param str A string to print, without printing a new line.
         */
        public void println(final String str) {
            print(str);
        }
    }

}

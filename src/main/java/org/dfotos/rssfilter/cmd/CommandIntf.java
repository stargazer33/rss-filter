package org.dfotos.rssfilter.cmd;

import java.util.List;

/**
 * All commands should implement this interface 
 */
public interface CommandIntf {
	
	
	/**
	 * The actual command implementation should be here
	 * 
	 * @param args - optional list of arguments (they can be from the command-line)
	 * 
	 * @throws Exception
	 */
	public void run(List<String> args) 	throws Exception;

	/**
	 * Description of this command (for end-user) 
	 * @return
	 */
	public String getHelpStr();
 
}

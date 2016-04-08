/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SkytapPlugin;

/**
 *
 * @author Administrator
 */
import hudson.model.BuildListener;

public final class JenkinsLogger {

	private static BuildListener listener;
	private static Boolean loggingEnabled;
        
	public JenkinsLogger(BuildListener listener, Boolean loggingEnabled) {
		JenkinsLogger.listener = listener;
		JenkinsLogger.loggingEnabled = loggingEnabled;
	}

	public static void log(String message) {

		if (loggingEnabled) {
			listener.getLogger().println(message);
		}

	}
	
	/**
	 * These messages get logged no matter what
	 * the 'logging enabled' preference is in global settings
	 * 
	 * @param message
	 */
	public static void defaultLogMessage(String message){
		listener.getLogger().println(message);
	}

	public static void error(String error) {
		listener.getLogger().println(error);
	}

	public static BuildListener getListener() {
		return listener;
	}
}

package utils;

import java.util.logging.Logger;

/**
 * User: zoran
 * Date: 2006-maj-25
 * Time: 01:33:49
 * zoran@medorian.com
 */
public class CustomLogger {

	public static void logme(String className, String msg){
		Logger.getLogger(className).log(config.DebugLevels.getLevel(className), "[" + className + "] " + msg);
	}

	public static void logme(String className, String msg, boolean severe){
		Logger.getLogger(className).severe("[" + className + "] " + msg);
	}
}

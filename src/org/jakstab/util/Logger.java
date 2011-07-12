/*
 * Logger.java - This file is part of the Jakstab project.
 * Copyright 2007-2011 Johannes Kinder <kinder@cs.tu-darmstadt.de>
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, see <http://www.gnu.org/licenses/>.
 */

package org.jakstab.util;

import java.io.PrintStream;

/**
 * @author Johannes Kinder
 */
public class Logger {

	public enum Level { DEBUG, VERBOSE, INFO, WARN, ERROR, FATAL }
	private static String globalPrefix = "";

	private static boolean showClass = false;
	private static String[] infoPrefixes = new String[]{
		/*"org.jakstab.ssl",
		"org.jakstab.disasm",
		"org.jakstab.asm",
		"org.jakstab.loader"*/
	};

	public static Level defaultLevel = Level.DEBUG;

	public static Logger getLogger(Class<? extends Object> c) {
		String className = c.getCanonicalName();
		Level level = defaultLevel;
		
		if (level.ordinal() < Level.INFO.ordinal()) {
			for (String infoPrefix : infoPrefixes) {
				if (className.startsWith(infoPrefix)) { 
					level = Level.INFO;
					break;
				}
			}
		}
		return new Logger(level, c, System.out);
	}

	public static void setVerbosity(int verbosity) {
		int level = Level.values().length - 1;
		if (verbosity < 0) verbosity = 0;
		else if (verbosity > level) verbosity = level;
		level = level - verbosity;
		
		defaultLevel = Level.values()[level];
	}
	
	public static void setGlobalPrefix(String prefix) {
		globalPrefix = prefix + "\t";
	}

	private Logger(Level level, Class<? extends Object> clazz, PrintStream outStream) {
		this.activeLevel = level;
		this.out = outStream;
		this.prefix = (showClass ? (clazz.getSimpleName() + ":\t") : "");
	}

	private Level activeLevel;
	private PrintStream out;
	private String prefix;

	public Level getLevel() {
		return activeLevel;
	}

	public boolean isDebugEnabled() {
		return Level.DEBUG.ordinal() >= activeLevel.ordinal();
	}

	public boolean isVerboseEnabled() {
		return Level.VERBOSE.ordinal() >= activeLevel.ordinal();
	}

	public boolean isInfoEnabled() {
		return Level.INFO.ordinal() >= activeLevel.ordinal();
	}

	public void log(Level level) {
		if (level.ordinal() >= activeLevel.ordinal())
			out.println();
	}

	public void log(Level level, Object message) {
		if (level.ordinal() >= activeLevel.ordinal())
			out.println(globalPrefix + prefix + message);
	}

	public void log(Level level, Object message, Throwable t) {
		if (level.ordinal() >= activeLevel.ordinal())
			out.println(globalPrefix + prefix + message + " " + t.getMessage());
	}

	public void logString(Level level, String string) {
		if (level.ordinal() >= activeLevel.ordinal())
			out.print(globalPrefix + prefix + string);
	}

	public void debug() { 
		log(Level.DEBUG);
	}

	public void debug(Object message) { 
		log(Level.DEBUG, message);
	}

	public void debug(Object message, Throwable t) { 
		log(Level.DEBUG, message, t);
	}

	public void debugString(String message) {
		logString(Level.DEBUG, message);
	}

	public void verbose() { 
		log(Level.VERBOSE);
	}

	public void verbose(Object message) { 
		log(Level.VERBOSE, message);
	}

	public void verbose(Object message, Throwable t) { 
		log(Level.VERBOSE, message, t);
	}

	public void verboseString(String message) {
		logString(Level.VERBOSE, message);
	}

	public void info() { 
		log(Level.INFO);
	}

	public void info(Object message) { 
		log(Level.INFO, message);
	}

	public void infoString(String message) {
		logString(Level.INFO, message);
	}

	public void info(Object message, Throwable t) { 
		log(Level.INFO, message, t);
	}

	public void warn(Object message) { 
		log(Level.WARN, message);
	}

	public void warn(Object message, Throwable t) { 
		log(Level.WARN, message, t);
	}

	public void error(Object message) { 
		log(Level.ERROR, message);
	}

	public void error(Object message, Throwable t) { 
		log(Level.ERROR, message, t);
	}

	public void fatal(Object message) { 
		log(Level.FATAL, message);
	}

	public void fatal(Object message, Throwable t) { 
		log(Level.FATAL, message, t);
	}
}

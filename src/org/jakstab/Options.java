/*
 * Options.java - This file is part of the Jakstab project.
 * Copyright 2009-2011 Johannes Kinder <kinder@cs.tu-darmstadt.de>
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
package org.jakstab;

import java.io.File;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.jakstab.util.Logger;

/**
 * Parses and holds command line options.
 * 
 * @author Johannes Kinder
 */
public class Options {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Options.class);

	private final static int lineLength = 80;
	private final static int indentation = 22;

	
	public static final String jakstabHome;
	static {
		// Get path of Jakstab's directory from VM
		String classFileName = Options.class.getResource("/org/jakstab/Options.class").getPath();
		if (classFileName.startsWith("file:")) 
			classFileName = classFileName.substring(5);
		classFileName = classFileName.replace("%20", " ");
		jakstabHome = (new File(classFileName)).getParentFile().getParentFile().getParentFile().getParent();
	}
	
	private static Map<String,Option<?>> options = new TreeMap<String,Option<?>>(new Comparator<String>() {
		public int compare(String s1, String s2) {
			if (s1.length() != 2 && s2.length() == 2) return 1;
			if (s1.length() == 2 && s2.length() != 2) return -1;
			else return s1.compareTo(s2);
		}
	});
	
	static void addOption(Option<?> o) {
		String name = o.getName().toLowerCase();
		if (options.containsKey(name)) {
			logger.fatal("Option " + name + " already present!");
			System.exit(1);
		} else {
			options.put(name, o);
		}
	}
	
	private static AnalysisManager mgr = AnalysisManager.getInstance();

	
	public static String mainFilename = null;
	public static List<String> moduleFilenames = new LinkedList<String>();

	public static Option<String> cpas = Option.create("cpa", "{" + mgr.getShorthandsString() + "}", "x", "Configure which analyses to use for control flow reconstruction.");
	public static Option<String> secondaryCPAs = Option.create("cpa2", "{" + mgr.getShorthandsString() + "}", "", "Secondary analyses to be performed after the initial CFG reconstruction and dead code elimination are completed.");
	
	public static Option<String> sslFilename = Option.create("ssl", "file", jakstabHome + "/ssl/pentium.ssl", "Use <file> instead of pentium.ssl.");
	public static Option<Long> startAddress = Option.create("a", "address", -1L, "Start analysis at given virtual address.");
	public static Option<Boolean> wdm = Option.create("wdm", "WDM mode, export main function as DriverMain.");
	public static Option<Boolean> dumpStates = Option.create("s", "Output all reached states after analysis.");	
	public static Option<Boolean> outputLocationsWithMostStates = Option.create("toplocs", "Output the 10 locations with the highest state count.");
	public static Option<Boolean> failFast = Option.create("fail-fast", "Stop when unsound assumptions are necessary to continue.");
	public static Option<Boolean> debug = Option.create("debug", "Stop on failed assertions or weak updates to the complete stack or all store regions.");
	public static Option<Boolean> asmTrace = Option.create("asm-trace", "Output any error trace as a list of assembly instructions instead of IL statements.");
	public static Option<Boolean> errorTrace = Option.create("error-trace", "Build an abstract error trace for failed assertions and debug stops.");
	public static Option<Boolean> backward = Option.create("backward", "Perform secondary cpa as a backward analysis.");
	public static Option<Boolean> background = Option.create("b", "Background mode, i.e., disable shutdown hook on enter.");
	public static Option<Boolean> graphML = Option.create("graphML", "Produce graphML output instead of GraphViz .dot files.");
	public static Option<Boolean> noGraphs = Option.create("no-graphs", "Do not generate output graphs");
	public static Option<Boolean> heuristicEntryPoints = Option.create("h", "Use heuristics to determine additional procedures and add pseudo-calls to include them in disassembly.");
	public static Option<Boolean> ignoreWeakUpdates = Option.create("ignore-weak-updates", "Do not perform weak store updates (unsound).");
	public static Option<Boolean> initHeapToBot = Option.create("bot-heap", "Initialize heap cells to BOT to force strong updates.");
	public static Option<Boolean> summarizeRep = Option.create("summarize-rep", "Use summarizing transformer for string instructions.");
	public static Option<Boolean> basicBlocks = Option.create("basicblocks", "Build CFA from basic-blocks instead of single statements.");
	public static Option<Integer> verbosity = Option.create("v", "level", 3, "Set verbosity to value. Default is 3.");
	public static Option<Integer> timeout = Option.create("timeout", "t", -1, "Set timeout in seconds for the analysis.");
	public static Option<Integer> procedureAbstraction = Option.create("procedures", "n", 0, "Level of procedure assumptions: " +
			"0: Pessimistic: No assumptions, treat calls and returns as jumps (default). " + 
			"1: Semi-optimistic: Abstract unknown calls according to ABI contract. " + 
			"2: Optimistic: Abstract all calls to ABI contract (fastest).");
	public static Option<Integer> getProcAddress = Option.create("getprocaddress", "n", 0, "How to resolve GetProcAddress: 0: Always succeed (default), 1: Split success/fail, 2: Merge success/fail");

	/**
	 * Handle command line options.
	 * 
	 * @param args
	 */
	public static void parseOptions(String args[]) {
		
		// Pre-load analyses so that they can register their options
		AnalysisManager.getInstance();
		
		for (int i = 0; i < args.length; i++) {
			String arg = args[i].toLowerCase();
			// Dash (-) arguments
			if (arg.startsWith("-")) {
				
				Option<?> opt = options.get(arg);
				if (opt != null) {
					if (opt.getDefaultValue() instanceof Boolean) {
						opt.setValue(Boolean.TRUE);
					} else if (opt.getDefaultValue() instanceof Integer) {
						opt.setValue(Integer.parseInt(args[++i]));
					} else if (opt.getDefaultValue() instanceof Long) {
						arg = args[++i];
						if (arg.startsWith("0x")) 
							opt.setValue(Long.parseLong(arg.substring(2), 16));
						else
							opt.setValue(Long.parseLong(arg));
					} else if (opt.getDefaultValue() instanceof String) {
						opt.setValue(args[++i]);
					} else {
						assert false : "Unhandled Option type " + opt.getDefaultValue().getClass().getSimpleName();
					}
				}			
				// Arguments which require arguments
				else if (i + 1 < args.length) {
					if (arg.equals("-m")) {
						mainFilename = args[++i];
					} 
				} else {
					logger.fatal("Invalid command line argument: " + arg);
					logger.fatal("");
					Options.printOptions();
					System.exit(1);
				}
			} // arguments w/o dash
			else {
				moduleFilenames.add(arg);
			}
		}

		if (mainFilename == null) {
			logger.fatal("No main file specified!");
			logger.fatal("");
			Options.printOptions();
			System.exit(1);
		}
		
	}
	
	public static void printOptions() {
		logger.fatal("Usage: jakstab [options] -m mainfile [ modules... ]");
		logger.fatal("");
		logger.fatal("Options:");
		
		for (Option<?> o : options.values()) {
			StringBuilder os = new StringBuilder(lineLength);
			os.append("  ").append(o.getName());
			if (o.getParamName() != null)
				os.append(' ').append(o.getParamName());
			os.append(' ');

			printWithIndentedLineWrap(os, o.getDescription());
			
			// Special treatment for CPAs option
			if (o.equals(cpas)) {
				String shorthands = mgr.getShorthandsString();
				for (int i=0; i<shorthands.length(); i++) {
					Character cpa = shorthands.charAt(i);
					
					os = new StringBuilder(lineLength);
					os.append("        ").append(cpa);
					
					printWithIndentedLineWrap(os, mgr.getName(cpa) + ": " + mgr.getDescription(cpa));
				}
			}
			
		}
	}
	
	private static void printWithIndentedLineWrap(StringBuilder os, String s) {
		StringTokenizer st = new StringTokenizer(s, " ");
		String nextWord = st.nextToken();
		printLine: while (true) {
			// We are at the first or a new line
			while (os.length() < indentation) {
				os.append(' ');
			}
			os.append(nextWord);

			// Output text until end of line
			while (true) {
				if (!st.hasMoreTokens())
					break printLine;
				nextWord = st.nextToken();
				
				if (os.length() + 1 + nextWord.length() <= lineLength) {
					os.append(" ").append(nextWord);
				} else {
					logger.fatal(os.toString());
					os = new StringBuilder(lineLength);
					// Start new line
					continue printLine;
				}
			}
		}
		logger.fatal(os.toString());
	}

}

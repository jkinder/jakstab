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
import java.util.LinkedList;
import java.util.List;

import org.jakstab.util.Logger;

/**
 * Parses and holds command line options.
 * 
 * @author Johannes Kinder
 */
public class Options {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Options.class);
	
	public static final String jakstabHome;
	static {
		// Get path of Jakstab's directory from VM
		String classFileName = Options.class.getResource("/org/jakstab/Options.class").getPath();
		if (classFileName.startsWith("file:")) 
			classFileName = classFileName.substring(5);
		classFileName = classFileName.replace("%20", " ");
		jakstabHome = (new File(classFileName)).getParentFile().getParentFile().getParentFile().getParent();
	}
	
	public static String sslFilename = jakstabHome + "/ssl/pentium.ssl";
	public static String mainFilename = null;
	public static List<String> moduleFilenames = new LinkedList<String>();
	public static long startAddress = -1;
	public static boolean callingContextSensitive = true;
	public static boolean wdm = false;
	public static boolean dumpStates = false;
	public static boolean outputLocationsWithMostStates = false;
	public static boolean failFast = false;
	public static boolean debug = false;
	public static boolean asmTrace = false;
	public static boolean errorTrace = false;
	public static boolean backward = false;
	public static boolean background = false;
	public static boolean graphML = false;
	public static boolean writeGraphs = true;
	public static boolean heuristicEntryPoints = false;
	public static boolean ignoreWeakUpdates = false;
	public static boolean initHeapToBot = false;
	public static boolean summarizeRep = false;
	public static boolean repPrecBoost = false;
	public static boolean basicBlocks = false;
	public static int verbosity = 3;
	public static int explicitThreshold = 5;
	public static int heapDataThreshold = -1;
	public static int procedureAbstraction = 0;
	public static String cpas = "x";
	public static String secondaryCPAs = null;
	public static int timeout = -1;
	public static String[] traceFiles;
	

	/**
	 * Handle command line options.
	 * 
	 * @param args
	 */
	public static void parseOptions(String args[]) {

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			// Dash (-) arguments
			if (arg.startsWith("-")) {
				if (arg.equals("--wdm")) wdm = true;
				else if (arg.equals("--fail-fast")) failFast = true;
				else if (arg.equals("--bot-heap")) initHeapToBot = true;
				else if (arg.equals("--error-trace")) errorTrace = true;
				else if (arg.equals("--asm-trace")) asmTrace = true;
				else if (arg.equals("--debug")) debug = true;
				else if (arg.equals("--boost-rep")) repPrecBoost = true;
				else if (arg.equals("--no-graphs")) writeGraphs = false;
				else if (arg.equals("--summarize-rep")) summarizeRep = true;
				else if (arg.equals("--basic-blocks")) basicBlocks = true;
				else if (arg.equals("--graphml")) graphML = true;
				else if (arg.equals("--ignore-weak-updates")) ignoreWeakUpdates = true;
				else if (arg.equals("--toplocs")) outputLocationsWithMostStates = true;
				else if (arg.equals("-b")) background = true;
				else if (arg.equals("-h")) heuristicEntryPoints = true;
				else if (arg.equals("-s")) dumpStates = true;
				// Arguments which require arguments
				else if (i + 1 < args.length) {
					if (arg.equals("--ssl")) {
						sslFilename = args[++i];
					} else if (arg.equals("-m")) {
						mainFilename = args[++i];
					} else if (arg.equals("-a")) {
						String addr = args[++i];
						if (addr.startsWith("0x")) startAddress = Long.parseLong(addr.substring(2), 16);
						else startAddress = Long.parseLong(addr, 16);
					} else if (arg.equals("-v")) {
						verbosity = Integer.parseInt(args[++i]);
					} else if (arg.equals("-t")) {
						timeout = Integer.parseInt(args[++i]);
					} else if (arg.equals("--explicit-threshold")) {
						explicitThreshold = Integer.parseInt(args[++i]);
					} else if (arg.equals("--heap-threshold")) {
						heapDataThreshold = Integer.parseInt(args[++i]);
					} else if (arg.equals("--procedures")) {
						procedureAbstraction = Integer.parseInt(args[++i]);
					} else if (arg.equals("--cpa")) {
						cpas = args[++i];
					} else if (arg.equals("--cpa2")) {
						if (secondaryCPAs != null) {
							logger.fatal("--cpa2 and --backward-cpa2 are mutually exclusive!");
							System.exit(1);
						}
						secondaryCPAs = args[++i];
					} else if (arg.equals("--backward-cpa2")) {
						if (secondaryCPAs != null) {
							logger.fatal("--cpa2 and --backward-cpa2 are mutually exclusive!");
							System.exit(1);
						}
						secondaryCPAs = args[++i];
						backward = true;
					} else if (arg.equals("--trace-file")) {
						traceFiles = args[++i].split(",");
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
		
		// Use default value if not specified
		if (heapDataThreshold < 0)
			heapDataThreshold = explicitThreshold;
		
	}
	
	public static void printOptions() {
		logger.fatal("Usage: jakstab [options] -m mainfile [ modules... ]");
		logger.fatal("");
		logger.fatal("Options:");
		logger.fatal("  -a address       Start analysis at given virtual address.");
		logger.fatal("  -b               Background mode, i.e., disable shutdown hook on enter.");
		logger.fatal("  -h               Use heuristics to determine additional procedures and");
		logger.fatal("                   add pseudo-calls to include them in disassembly.");
		logger.fatal("  -s               Output all reached states.");
		logger.fatal("  -t seconds       Set timeout in seconds for the analysis.");
		logger.fatal("  -v value         Set verbosity to value. Default is " + (5 - Logger.defaultLevel.ordinal()) + ".");
		logger.fatal("  --cpa {cbxfis}   Configure which analyses to use for control flow");
		logger.fatal("                   reconstruction and in which order:");
		logger.fatal("         c         Constant propagation");
		logger.fatal("         b         Based constant propagation (memory regions)");
		logger.fatal("         x         Bounded Address Tracking ('b' with path-sensitivity)");
		logger.fatal("         f         Forward expression substitution");
		logger.fatal("         i         Strided intervals");
		logger.fatal("         s         Call stack analysis (unsound on non-standard code)");
		logger.fatal("         t         Trace replay analysis (under-approximation)");
		logger.fatal("  --procedures n   Level of procedure assumptions:");
		logger.fatal("         0         Pessimistic: No assumptions, treat calls and returns as jumps.");
		logger.fatal("         1         Semi-optimistic: Abstract unknown calls according to ABI contract.");
		logger.fatal("         2         Optimistic: Abstract all calls to ABI contract (fastest).");
		logger.fatal("  --cpa2 class1,... Comma separated list of subpackage and class names of");
		logger.fatal("                   secondary analyses to run after CFA reconstruction, e.g.,");
		logger.fatal("                   'rd.ReachingDefinitionsAnalysis,callstack.CallStackAnalysis'.");
		logger.fatal("  --backward-cpa2  Same as --cpa2, but performs backward analysis. Mutually");
		logger.fatal("                   exclusive with --cpa2.");
		logger.fatal("  --basic-blocks   Build CFA from basic-blocks instead of single statements.");
		logger.fatal("  --bot-heap       Initialize heap cells to BOT to force strong updates.");
		logger.fatal("  --debug          stop on failed assertions or weak updates to the complete stack ");
		logger.fatal("                   or all store regions");
		logger.fatal("  --error-trace    Build an abstract error trace for failed assertions and debug stops.");
		logger.fatal("  --explicit-threshold t   Set the maximum number of explicit values");
		logger.fatal("                   tracked per variable and location.");
		logger.fatal("  --fail-fast      Stop when unsound assumptions are necessary to continue.");
		logger.fatal("  --graphml        Produce graphML output instead of GraphViz .dot files");
		logger.fatal("  --heap-threshold t   Explicit threshold for data stored on the heap.");
		logger.fatal("  --ignore-weak-updates Do not perform weak store updates (unsound!)");
		logger.fatal("  --no-graphs      Do not generate output graphs.");
		logger.fatal("  --ssl SSLfile    Use SSLfile instead of pentium.ssl"); 
		logger.fatal("  --summarize-reps Use summarizing transformer for string instructions."); 
		logger.fatal("  --toplocs        Output the 10 locations with the highest state count.");
		logger.fatal("  --trace-file     Comma separated list of trace files used for tracereplay. Default is");
		logger.fatal("                   filename with .traced extension");
		logger.fatal("  --wdm            WDM mode, export main function as DriverMain.");
	}

}

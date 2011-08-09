/*
 * TraceReplayAnalysis.java - This file is part of the Jakstab project.
 * Copyright 2011 Johannes Kinder <kinder@cs.tu-darmstadt.de>
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
package org.jakstab.analysis.tracereplay;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jakstab.AnalysisProperties;
import org.jakstab.Option;
import org.jakstab.Options;
import org.jakstab.Program;
import org.jakstab.analysis.AbstractState;
import org.jakstab.analysis.CPAOperators;
import org.jakstab.analysis.ConfigurableProgramAnalysis;
import org.jakstab.analysis.Precision;
import org.jakstab.analysis.ReachedSet;
import org.jakstab.asm.AbsoluteAddress;
import org.jakstab.cfa.CFAEdge;
import org.jakstab.cfa.Location;
import org.jakstab.cfa.StateTransformer;
import org.jakstab.rtl.RTLLabel;
import org.jakstab.rtl.statements.RTLAssume;
import org.jakstab.rtl.statements.RTLGoto;
import org.jakstab.rtl.statements.RTLStatement;
import org.jakstab.util.Logger;
import org.jakstab.util.Pair;

/**
 * Analysis for replaying the program counter values of a single recorded trace.
 */
public class TraceReplayAnalysis implements ConfigurableProgramAnalysis {

	public static void register(AnalysisProperties p) {
		p.setShortHand('t');
		p.setName("Trace replay analysis");
		p.setDescription("Replays pre-recorded traces as an under-approximation of control flow.");
	}
	
	public static Option<String> traceFiles = Option.create("trace-file", "f", "", "Comma separated list of trace files to use for tracereplay (default is <mainFile>.parsed)");
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TraceReplayAnalysis.class);

	private final AbsoluteAddress[] trace;

	public TraceReplayAnalysis(String filename) {

		BufferedReader in;
		
		try {
			in = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			logger.fatal("Trace file not found: " + e.getMessage());
			throw new RuntimeException(e);
		}

		// Read entire trace
		
		String line = null;
		List<AbsoluteAddress> traceList = new LinkedList<AbsoluteAddress>();
		
		do {
			String lastLine = line; 
			try {
				line = in.readLine();
			} catch (IOException e) {
				logger.fatal("IO error when reading from trace: " + e.getMessage());
				throw new RuntimeException(e);
			}
			if (line != null) {
				
				AbsoluteAddress curPC;
				
				if (line.charAt(0) == 'A') {
					// Dima's "parsed" format
					curPC = new AbsoluteAddress(Long.parseLong(line.substring(9, line.indexOf('\t', 9)), 16));
				} else {
					// Pure format produced by temu's text conversion
					curPC = new AbsoluteAddress(Long.parseLong(line.substring(0, line.indexOf(':')), 16));
				}
				
				if (line.equals(lastLine)) {
					logger.warn("Warning: Skipping duplicate line in trace for address " + curPC);
				} else {
					traceList.add(curPC);
				}
			}
		} while (line != null);
		
		trace = traceList.toArray(new AbsoluteAddress[traceList.size()]);
	}

	@Override
	public Precision initPrecision(Location location, StateTransformer transformer) {
		return null;
	}

	public AbstractState initStartState(Location label) {

		int lineNumber = 0;
		
		// Set initial state to the first line that points to a program address
		// or the manually specified start address, if there is one
		while (lineNumber < trace.length && (
				!isProgramAddress(trace[lineNumber]) || 
				(Options.startAddress.getValue() >= 0L && trace[lineNumber].getValue() != Options.startAddress.getValue()
						))
				) {
			lineNumber++;
		}

		if (lineNumber >= trace.length) {
			throw new RuntimeException("Did not find program locations in trace!");
		}

		AbsoluteAddress cur = trace[lineNumber-1];
		AbsoluteAddress next = trace[lineNumber];
		logger.debug("Starting with trace replay from " + cur + " -> " + next);

		return new TraceReplayState(trace, lineNumber); 
	}

	@Override
	public AbstractState merge(AbstractState s1, AbstractState s2, Precision precision) {

		if (s2.isBot() && !s1.isBot()) return s1;
		if (s2.equals(s1)) return s1;
		return s2;
	}

	private static boolean isProgramAddress(AbsoluteAddress a) {
		return Program.getProgram().getModule(a) != null;
	}

	private static boolean isProgramAddress(Location l) {
		return isProgramAddress(((RTLLabel)l).getAddress());
	}

	@Override
	public Set<AbstractState> post(AbstractState state, CFAEdge cfaEdge,
			Precision precision) {
		return Collections.singleton(singlePost(state, cfaEdge, precision));
	}		

	private AbstractState singlePost(AbstractState state, CFAEdge cfaEdge, Precision precision) {

		if (state.isBot()) 
			return state;
		
		RTLLabel edgeTarget = (RTLLabel)cfaEdge.getTarget();
		
		// If the entire edge is outside the module, just wait and do nothing 
		if (!isProgramAddress(cfaEdge.getSource()) && !isProgramAddress(edgeTarget)) {
			//logger.debug("Outside of module at edge " + cfaEdge);
			return state;
		}
		
		//logger.debug("Inside module " + cfaEdge);
		
		TraceReplayState tState = (TraceReplayState)state;
		int lineNumber = tState.getLineNumber();
		
		RTLStatement stmt = (RTLStatement)cfaEdge.getTransformer();
		
		if (edgeTarget.getAddress().equals(tState.getCurrentPC()) &&  
				!(stmt instanceof RTLAssume && ((RTLAssume)stmt).getSource().getType() == RTLGoto.Type.REPEAT)) {
			// Next statement has same address (and is no back jump from REP), so do not move forward in trace 
			return tState;
		} else {
			// Next statement has a different address (or is the re-execution of a REP prefixed instruction)
			
			if (tState.getNextPC().equals(edgeTarget.getAddress())) {
				// Edge goes along the trace
				return new TraceReplayState(trace, lineNumber + 1);
			} else {
				// Edge diverges from trace - either other path or into library
				
				if (isProgramAddress(edgeTarget)) {
					// Target is in program, but on a different path not taken by this trace
					logger.debug("Visiting edge " + cfaEdge + ", trace expected " + tState.getNextPC() + " next.");
					return TraceReplayState.BOT;
				} else {
					// Target is not in program, so we went into another module (library)
					logger.debug("Calling out of module to " + edgeTarget + ", fast forwarding from " + cfaEdge.getSource());
					do {
						lineNumber++;
					} while (lineNumber < trace.length && !isProgramAddress(trace[lineNumber]));
					
					if (lineNumber >= trace.length) {
						logger.verbose("Reached end of trace.");
						return TraceReplayState.BOT;
					} else {
						logger.debug("Arrived at " + trace[lineNumber - 1] + " -> " + trace[lineNumber]);
						return new TraceReplayState(trace, lineNumber - 1);
					}
				}
			}
		}
		
	}
	
	@Override
	public Pair<AbstractState, Precision> prec(AbstractState s, Precision precision, ReachedSet reached) {
		return Pair.create(s, precision);
	}

	@Override
	public boolean stop(AbstractState s, ReachedSet reached, Precision precision) {
		return CPAOperators.stopSep(s, reached, precision);
	}

	@Override
	public AbstractState strengthen(AbstractState s, Iterable<AbstractState> otherStates, CFAEdge cfaEdge, Precision precision) {
		return null;
	}

}
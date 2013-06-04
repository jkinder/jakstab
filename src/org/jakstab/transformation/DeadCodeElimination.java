/*
 * LiveVariableAnalysis.java - This file is part of the Jakstab project.
 * Copyright 2007-2013 Johannes Kinder <jk@jakstab.org>
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

package org.jakstab.transformation;

import java.util.*;

import org.jakstab.Program;
import org.jakstab.cfa.CFAEdge;
import org.jakstab.cfa.Location;
import org.jakstab.cfa.StateTransformer;
import org.jakstab.rtl.expressions.*;
import org.jakstab.rtl.statements.*;
import org.jakstab.util.Characters;
import org.jakstab.util.FastSet;
import org.jakstab.util.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * @author Johannes Kinder
 */
public class DeadCodeElimination implements CFATransformation {

	@SuppressWarnings("unused")
	private final static Logger logger = Logger.getLogger(DeadCodeElimination.class);

	private Map<Location,SetOfVariables> liveVars;
	private SetOfVariables liveInSinks;
	private Set<CFAEdge> cfa;
	private Program program;
	private long removalCount;
	private boolean enableJumpThreading;
	private volatile boolean stop = false;
	private SetMultimap<Location, CFAEdge> inEdges;
	private SetMultimap<Location, CFAEdge> outEdges;
	
	public Set<CFAEdge> getCFA() {
		return cfa;
	}
	
	public long getRemovalCount() {
		return removalCount;
	}

	public DeadCodeElimination(Set<CFAEdge> cfa, boolean enableJumpThreading) {
		super();
		this.cfa = new HashSet<CFAEdge>(cfa);
		this.program = Program.getProgram();
		this.enableJumpThreading = enableJumpThreading;

		liveInSinks = new SetOfVariables();
		liveInSinks.addAll(program.getArchitecture().getRegisters());
		
	}

	private boolean isDeadEdge(CFAEdge edge) {
		StateTransformer t = edge.getTransformer();
		if (t instanceof RTLVariableAssignment) {
			RTLVariableAssignment a = (RTLVariableAssignment)edge.getTransformer();
			RTLVariable lhs = a.getLeftHandSide();
			if (!liveVars.get(edge.getTarget()).contains(lhs))
				return true;
		} else if (enableJumpThreading) {
			// Don't remove assumes when doing procedure detection!
			if (t instanceof RTLAssume) {
				RTLAssume a = (RTLAssume)edge.getTransformer();
				// Not needed if we remove every jump where we have just one out edge
				//if (a.getAssumption().equals(ExpressionFactory.TRUE)) {
				//	return true;
				//}
				// Remove jumps that have just one target
				if (outEdges.get(edge.getSource()).size() == 1) {
					switch (a.getSource().getType()) {
					case CALL: case RETURN:
						return false;
					default:
						// If this goes into or out of a stub, it's not a dead edge 
						if (program.isStub(edge.getSource().getAddress()) ^ 
								program.isStub(edge.getTarget().getAddress()))
							return false;
						if (program.getHarness().contains(edge.getSource().getAddress()) ^ 
								program.getHarness().contains(edge.getTarget().getAddress())) {
							return false;
						}

						return true;
					}
				}
			} else if (t instanceof RTLSkip) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void run() {
		logger.infoString("Eliminating dead code");
		long startTime = System.currentTimeMillis();


		removalCount = 0;
		long oldRemovalCount = 0;
		int iterations = 0;
		
		// Outer fixpoint iteration for doing liveness + DCE as long as possible 
		do {
			logger.infoString(".");

			// Reset and init liveness data
			liveVars = new HashMap<Location, SetOfVariables>();
			FastSet<Location> worklist = new FastSet<Location>();

			// Reset and init in and out edge sets
			inEdges = HashMultimap.create();
			outEdges = HashMultimap.create();
			for (CFAEdge e : cfa) {
				inEdges.put(e.getTarget(), e);
				outEdges.put(e.getSource(), e);
			}

			for (CFAEdge e : cfa) {
				// Initialize all to bot / empty set
				if (!liveVars.containsKey(e.getSource())) {
					liveVars.put(e.getSource(), new SetOfVariables());
					// There might be infinite loops where no sink is reachable, so we 
					// just add all nodes to the work list here as a poor man's solution
					worklist.add(e.getSource());
				}
			}

			for (Location l : inEdges.keySet()) {
				if (outEdges.get(l).size() == 0) {
					// Sinks havn't been initialized yet
					liveVars.put(l, new SetOfVariables(liveInSinks));
					
					// Initialize work list with sinks.
					worklist.add(l);
				}
			}
			
			oldRemovalCount = removalCount;
			iterations++;

			while (!worklist.isEmpty() && !stop) {

				Location node = worklist.pick();
				SetOfVariables sLVout = liveVars.get(node);
				
				for (CFAEdge inEdge : inEdges.get(node)) {
					RTLStatement stmt = (RTLStatement)inEdge.getTransformer();
					// start by copying LVout -> LVin
					SetOfVariables sLVin = new SetOfVariables(sLVout);

					//// Remove KILL(s) from sLVin
					sLVin.removeAll(stmt.getDefinedVariables());
					
					// Remove also al for eax etc.
					for (RTLVariable v : stmt.getDefinedVariables()) {
						sLVin.removeAll(ExpressionFactory.coveredRegisters(v));
					}

					//// Add GEN(s) to sLVin
					sLVin.addAll(stmt.getUsedVariables());
					
					// Add also eax for al, etc.
					for (RTLVariable v : stmt.getUsedVariables()) {
						sLVin.addAll(ExpressionFactory.coveringRegisters(v));
					}
					
					// Registers might be used inside an unknown procedure call
					if (inEdge.getTransformer() instanceof RTLUnknownProcedureCall) {
						sLVin.addAll(program.getArchitecture().getRegisters());
					}
					
					SetOfVariables predLVOut = liveVars.get(inEdge.getSource());
					if (predLVOut == null) {
						logger.error("No LV out for inEdge " + inEdge + " for node " + node);
						logger.error("In CFA: " + cfa.contains(inEdge));
						logger.error("Containskey: " + liveVars.containsKey(inEdge.getSource()));
					}

					SetOfVariables newPredLVout = new SetOfVariables(predLVOut);
					newPredLVout.addAll(sLVin);
					if (!newPredLVout.equals(predLVOut)) {
						liveVars.put(inEdge.getSource(), newPredLVout);
						worklist.add(inEdge.getSource());
					}

				}
			}

			Set<CFAEdge> deadEdges = new FastSet<CFAEdge>();
			for (CFAEdge edge : cfa) {
				if (isDeadEdge(edge)) {
					deadEdges.add(edge);
				}
			}			
			
			// Delete the dead edges
			for (CFAEdge deadEdge : deadEdges) {
				// Check that source only has this one outedge
				if (outEdges.get(deadEdge.getSource()).size() <= 1) {
					inEdges.remove(deadEdge.getTarget(), deadEdge);
					outEdges.remove(deadEdge.getSource(), deadEdge);
					cfa.remove(deadEdge);
					// Make all edges pointing to the source of the edge point to it's target
					Set<CFAEdge> edgesToDeadSource = new FastSet<CFAEdge>(inEdges.get(deadEdge.getSource()));
					for (CFAEdge inEdge : edgesToDeadSource) {
						inEdges.remove(deadEdge.getSource(), inEdge);
						inEdge.setTarget(deadEdge.getTarget());
						inEdges.put(inEdge.getTarget(), inEdge);
						
					}
					/*if (deadEdge.getSource().equals(program.getStart())) {
						program.setStart(deadEdge.getTarget().getLabel());
					}*/
					removalCount++;
				}
			}
		
		} while (removalCount > oldRemovalCount && !stop);

		logger.info();
		
		long endTime = System.currentTimeMillis();
		logger.verbose("Removed " + removalCount + " edges, finished after " + 
				(endTime - startTime) + "ms and " + iterations + " iterations.");

		//program.setCFA(cfa);
	}
	
	public void stop() {
		logger.fatal("");
		logger.fatal(Characters.starredBox("Interrupt! Stopping Dead Code Elimination!"));
		stop = true;
	}

}

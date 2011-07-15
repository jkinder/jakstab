/*
 * ProgramGraphWriter.java - This file is part of the Jakstab project.
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

import java.awt.Color;
import java.io.IOException;
import java.util.*;

import org.jakstab.analysis.*;
import org.jakstab.cfa.CFAEdge;
import org.jakstab.cfa.Location;
import org.jakstab.rtl.*;
import org.jakstab.rtl.statements.RTLHalt;
import org.jakstab.rtl.statements.RTLStatement;
import org.jakstab.util.*;

import com.google.common.collect.SetMultimap;

/**
 * Writes various Graphviz-graphs from a program structure.
 * 
 * @author Johannes Kinder
 */
public class ProgramGraphWriter {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ProgramGraphWriter.class);
	private Program program;

	public ProgramGraphWriter(Program program) {
		this.program = program;
	}
	
	private GraphWriter createGraphWriter(String filename) {
		try {
			if (Options.graphML) {
				return new GraphMLWriter(filename);
			} else {
				return new GraphvizWriter(filename);
			}
		} catch (IOException e) {
			logger.error("Cannot open output file!", e);
			return null;
		}
	}

	private Map<String,String> getNodeProperties(Location loc) {
		RTLStatement curStmt = program.getStatement((RTLLabel)loc);
		Map<String,String> properties = new HashMap<String, String>();

		if (curStmt != null) {
			if (curStmt.getLabel().getAddress().getValue() >= 0xFACE0000L) {
				properties.put("color", "lightgrey");
				properties.put("fillcolor", "lightgrey");
			}

			if (program.getUnresolvedBranches().contains(curStmt.getLabel())) {
				properties.put("fillcolor", "red");
			}

			if (curStmt.getLabel().equals(program.getStart())) {
				properties.put("color", "green");
				properties.put("style", "filled,bold");
			} else if (curStmt instanceof RTLHalt) {
				properties.put("color", "orange");
				properties.put("style", "filled,bold");
			}
		} else {
			logger.info("No real statement for location " + loc);
		}

		return properties;
	}

	public void writeControlFlowAutomaton(String filename) {
		writeControlFlowAutomaton(filename, null);
	}

	public void writeControlFlowAutomaton(String filename, ReachedSet reached) {
		Set<Location> nodes = new HashSet<Location>();
		for (CFAEdge e : program.getCFA()) {
			nodes.add(e.getTarget());
			nodes.add(e.getSource());
		}

		// Create dot file
		GraphWriter gwriter = createGraphWriter(filename);
		if (gwriter == null) return;

		logger.info("Writing CFA to " + gwriter.getFilename());
		try {
			for (Location node : nodes) {
				String nodeName = node.toString();
				StringBuilder labelBuilder = new StringBuilder();
				labelBuilder.append(nodeName);
				if (reached != null) {
					labelBuilder.append("\n");
					if (reached.where(node).isEmpty()) {
						logger.warn("No reached states for location " + node);
					}
					for (AbstractState a : reached.where(node)) {
						labelBuilder.append(a.toString());
						labelBuilder.append("\n");
					}
				}
				gwriter.writeNode(nodeName, labelBuilder.toString(), getNodeProperties(node));
			}

			for (CFAEdge e : program.getCFA()) {
				if (e.getKind() == null) logger.error("Null kind? " + e);
				gwriter.writeLabeledEdge(e.getSource().toString(), 
						e.getTarget().toString(), 
						e.getTransformer().toString(),
						e.getKind().equals(CFAEdge.Kind.MAY) ? Color.BLACK : Color.GREEN);
			}

			gwriter.close();
		} catch (IOException e) {
			logger.error("Cannot write to output file", e);
			return;
		}
	}
	
	public void writeCallGraph(String filename, SetMultimap<RTLLabel, RTLLabel> callGraph) {
		// Create dot file
		GraphWriter gwriter = createGraphWriter(filename);
		if (gwriter == null) return;
		
		Set<RTLLabel> nodes = new HashSet<RTLLabel>();
		
		logger.info("Writing callgraph to " + gwriter.getFilename());
		try {
			for (Map.Entry<RTLLabel, RTLLabel> e : callGraph.entries()) {
				nodes.add(e.getKey());
				nodes.add(e.getValue());
				gwriter.writeEdge(e.getKey().toString(), 
						e.getValue().toString());
			}
			
			for (RTLLabel node : nodes) {
				gwriter.writeNode(node.toString(), node.toString(), getNodeProperties(node));
			}

			gwriter.close();
		} catch (IOException e) {
			logger.error("Cannot write to output file", e);
			return;
		}		
	}

	public void writeART(String filename, AbstractReachabilityTree art) {
		Map<String,String> startNode = new HashMap<String, String>();
		Map<String,String> endNode = new HashMap<String, String>();
		startNode.put("color", "green");
		startNode.put("style", "filled,bold");
		endNode.put("color", "red");
		endNode.put("style", "filled,bold");

		// Create dot file
		GraphWriter gwriter = createGraphWriter(filename);
		if (gwriter == null) return;

		logger.info("Writing ART to " + gwriter.getFilename());
		try {
			Deque<AbstractState> worklist = new LinkedList<AbstractState>();
			//Set<AbstractState> visited = new HashSet<AbstractState>();
			worklist.add(art.getRoot());
			//visited.addAll(worklist);
			while (!worklist.isEmpty()) {
				AbstractState curState = worklist.removeFirst();

				String nodeName = curState.getIdentifier();
				Map<String, String> properties = null;
				if (curState == art.getRoot())
					properties = startNode;
				if (Program.getProgram().getStatement((RTLLabel)curState.getLocation()) instanceof RTLHalt)
					properties = endNode;
				StringBuilder nodeLabel = new StringBuilder();
				nodeLabel.append(curState.getIdentifier());
				//nodeLabel.append("\\n");
				//nodeLabel.append(curState);
				for (AbstractState coverState : art.getCoveringStates(curState)) {
					nodeLabel.append("Covered by ").append(coverState.getIdentifier()).append("\\n");
				}

				gwriter.writeNode(nodeName, nodeLabel.toString(), properties);

				for (AbstractState nextState : art.getChildren(curState)) {
					//if (!visited.contains(nextState)) {
					worklist.add(nextState);
					//visited.add(nextState);
					gwriter.writeEdge(nodeName, nextState.getIdentifier());
					//}
				}
			}			
			gwriter.close();
		} catch (IOException e) {
			logger.error("Cannot write to output file", e);
			return;
		}

	}

}

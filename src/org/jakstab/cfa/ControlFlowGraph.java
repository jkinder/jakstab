package org.jakstab.cfa;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jakstab.Program;
import org.jakstab.rtl.statements.BasicBlock;
import org.jakstab.rtl.statements.RTLGoto;
import org.jakstab.rtl.statements.RTLStatement;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class ControlFlowGraph {
	
	private SetMultimap<Location, CFAEdge> outEdges;
	private SetMultimap<Location, CFAEdge> inEdges;
	private SetMultimap<Location, CFAEdge> bbOutEdges;
	private SetMultimap<Location, CFAEdge> bbInEdges;
	private Map<Location, BasicBlock> basicBlocks;
	private Set<Location> locations;
	
	public ControlFlowGraph(Set<CFAEdge> edges) {
		outEdges = HashMultimap.create();
		inEdges = HashMultimap.create();
		locations = new HashSet<Location>();
		
		for (CFAEdge e : edges) {
			addEdge(e);
		}
		
		bbOutEdges = HashMultimap.create();
		bbInEdges = HashMultimap.create();
		basicBlocks = new HashMap<Location, BasicBlock>();
		Program program = Program.getProgram();
		
		for (Location l : locations) {
			Set<CFAEdge> in = inEdges.get(l);
			if (in != null && in.size() == 1) {
				CFAEdge e = in.iterator().next();
				if (!(program.getStatement(e.getSource()) instanceof RTLGoto))
					continue;
			}
			// Create new basic block from location
			BasicBlock bb = new BasicBlock();
			basicBlocks.put(l, bb);
		}
		
		for (Map.Entry<Location, BasicBlock> entry : basicBlocks.entrySet()) {
			Location head = entry.getKey();
			BasicBlock bb = entry.getValue();
			
			bb.add(program.getStatement(head));
			Location l = head;
			Set<CFAEdge> out = outEdges.get(l);
			while (out != null && out.size() == 1) {
				l = out.iterator().next().getTarget();
				if (basicBlocks.containsKey(l))
					break;
				bb.add(program.getStatement(l));
				out = outEdges.get(l);
			}
			
			if (out != null) for (CFAEdge e : out) {
				CFAEdge bbEdge = new CFAEdge(head, e.getTarget(), bb);
				bbOutEdges.put(head, bbEdge);
				bbInEdges.put(e.getTarget(), bbEdge);
			}
		}
	}
	
	public Set<Location> getNodes() {
		return Collections.unmodifiableSet(locations);
	}
	
	public Set<CFAEdge> getEdges() {
		return Collections.unmodifiableSet(
				new HashSet<CFAEdge>(outEdges.values()));
	}
	
	public Set<Location> getBasicBlockNodes() {
		return Collections.unmodifiableSet(basicBlocks.keySet());
	}
	
	public Set<CFAEdge> getBasicBlockEdges() {
		return Collections.unmodifiableSet(
				new HashSet<CFAEdge>(bbOutEdges.values()));
	}
	
	public Set<BasicBlock> getBasicBlocks() {
		return Collections.unmodifiableSet(
				new HashSet<BasicBlock>(basicBlocks.values()));
	}

	private void addEdge(CFAEdge e) {
		
		if (e.getTransformer() instanceof BasicBlock) {
			BasicBlock bb = (BasicBlock)e.getTransformer();
			for (RTLStatement stmt : bb) {
				CFAEdge newEdge = new CFAEdge(stmt.getLabel(), stmt.getNextLabel(), stmt);
				addEdge(newEdge);
			}
			return;
		}
		
		// Check if edge already exists (equality on edges uses not only source & target)
		Set<CFAEdge> existingEdges = outEdges.get(e.getSource());
		if (existingEdges != null) for (CFAEdge existing : existingEdges) {
			if (existing.getTarget().equals(e.getTarget()))
				return;
		}
		
		outEdges.put(e.getSource(), e);
		inEdges.put(e.getTarget(), e);
		locations.add(e.getSource());
		locations.add(e.getTarget());		
	}

}

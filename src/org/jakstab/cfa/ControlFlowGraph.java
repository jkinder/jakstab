package org.jakstab.cfa;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jakstab.rtl.statements.BasicBlock;
import org.jakstab.rtl.statements.RTLAssume;
import org.jakstab.rtl.statements.RTLSkip;
import org.jakstab.rtl.statements.RTLStatement;
import org.jakstab.util.FastSet;
import org.jakstab.util.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public abstract class ControlFlowGraph {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ControlFlowGraph.class);

	private SetMultimap<Location, CFAEdge> outEdges;
	private SetMultimap<Location, CFAEdge> inEdges;

	private SetMultimap<Location, CFAEdge> bbOutEdges;
	private SetMultimap<Location, CFAEdge> bbInEdges;
	private Map<Location, BasicBlock> basicBlocks;
	private Set<Location> locations;
	private Location entryPoint;
	
	
	public ControlFlowGraph() {
		outEdges = HashMultimap.create();
		inEdges = HashMultimap.create();
		locations = new HashSet<Location>();
	}
	
	protected abstract boolean isBasicBlockHead(Location l);
	
	public Location getEntryPoint() {
		return entryPoint;
	}

	public Set<Location> getNodes() {
		return Collections.unmodifiableSet(locations);
	}
	
	public Set<CFAEdge> getEdges() {
		return Collections.unmodifiableSet(
				new HashSet<CFAEdge>(outEdges.values()));
	}
	
	public int getInDegree(Location l) {
		return inEdges.get(l).size();
	}
	
	public int getOutDegree(Location l) {
		return outEdges.get(l).size();
	}
	
	public Set<CFAEdge> getInEdges(Location l) {
		return Collections.unmodifiableSet(inEdges.get(l));
	}
	
	public Set<CFAEdge> getOutEdges(Location l) {
		return Collections.unmodifiableSet(outEdges.get(l));
	}
	
	public Set<Location> getSuccessorLocations(Location l) {
		Set<Location> res = new FastSet<Location>();
		for (CFAEdge e : outEdges.get(l))
			res.add(e.getTarget());
		return res;
	}
	
	public BasicBlock getBasicBlock(Location l) {
		return basicBlocks.get(l);
	}
	
	public Set<Location> getBasicBlockNodes() {
		return Collections.unmodifiableSet(basicBlocks.keySet());
	}
	
	public Set<CFAEdge> getBasicBlockEdges() {
		return Collections.unmodifiableSet(
				new HashSet<CFAEdge>(bbOutEdges.values()));
	}
	
	public Set<CFAEdge> getBasicBlockOutEdges(Location l) {
		return bbOutEdges.get(l);
	}
	
	public Map<Location, BasicBlock> getBasicBlocks() {
		return Collections.unmodifiableMap(basicBlocks);
	}
	
	public CFAEdge getEdgeBetween(Location src, Location tgt) {
		Set<CFAEdge> out = outEdges.get(src);
		if (out != null) for (CFAEdge e : out)
			if (e.getTarget().equals(tgt))
				return e;
		return null;
	}

	public int numEdges() {
		return outEdges.size();
	}
	
	protected void findEntryPoint() {
		for (Location l : getNodes()) {
			if (getInDegree(l) == 0) {
				assert entryPoint == null : "Graph has multiple entry points: " + entryPoint + " and " + l; 
				entryPoint = l;
			}
		}
		assert entryPoint != null : "No entry point found! First statement in cycle?";
	}
	
	private Set<Location> findBasicBlockHeads() {
		Set<Location> result = new HashSet<Location>();
		
		// Find basic block heads
		for (Location l : getNodes()) {
			if (isBasicBlockHead(l))
				result.add(l);
		}
		logger.debug(result.size() + " basic blocks in the VPC-CFG.");
		return result;
	}
	
	protected void buildBasicBlocks() {
		
		Set<Location> basicBlockHeads = findBasicBlockHeads();
		
		bbOutEdges = HashMultimap.create();
		bbInEdges = HashMultimap.create();
		basicBlocks = new HashMap<Location, BasicBlock>();

		for (Location l : basicBlockHeads)
			basicBlocks.put(l, new BasicBlock());
		
		for (Map.Entry<Location, BasicBlock> entry : basicBlocks.entrySet()) {
			Location head = entry.getKey();
			BasicBlock bb = entry.getValue();
			
			//bb.add(program.getStatement(head.getLabel()));
			Location l = head;
			Set<CFAEdge> out = outEdges.get(l);
			while (!out.isEmpty()) {
				CFAEdge edge = out.iterator().next();				
				bb.add((RTLStatement)edge.getTransformer());
				if (out.size() > 1)
					break;
				l = edge.getTarget();
				if (basicBlocks.containsKey(l))
					break;
				//bb.add(program.getStatement(l.getLabel()));
				out = outEdges.get(l);
			}
			// If there's no statement (because there's an immediate jump), add a skip
			if (bb.isEmpty()) {
				RTLStatement dummy = new RTLSkip();
				dummy.setLabel(l.getLabel());
				if (!out.isEmpty())
					dummy.setNextLabel(out.iterator().next().getTarget().getLabel());
				bb.add(dummy);
			}
			
			for (CFAEdge e : out) {
				RTLStatement edgeStmt;
				if (out.size() > 1 && e.getTransformer() instanceof RTLAssume) {
					edgeStmt = (RTLStatement)e.getTransformer();
				} else {
					edgeStmt = new RTLSkip();
					RTLStatement oldStmt = (RTLStatement)e.getTransformer();
					edgeStmt.setLabel(oldStmt.getLabel());
					edgeStmt.setNextLabel(oldStmt.getNextLabel());
				}
				CFAEdge bbEdge = new CFAEdge(head, e.getTarget(), edgeStmt);
				if (!basicBlocks.containsKey(e.getTarget())) {
					logger.error("Target not in basic block head list? " + bbEdge);
				} else {
					bbOutEdges.put(head, bbEdge);
					//assert basicBlocks.containsKey(e.getTarget()) : "Target not in basic block head list? " + bbEdge;
					bbInEdges.put(e.getTarget(), bbEdge);
				}
			}
		}
		
	}

	protected void addEdge(CFAEdge e) {
		
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
	
	protected boolean valid() {
		
		int errors = 0;
		
		for (CFAEdge e : outEdges.values()) {
			if (!locations.contains(e.getSource())) {
				logger.error("Locations do not contain " + e.getSource() + " from edge " + e);
				errors++;
			}
			if (!locations.contains(e.getTarget())) {
				logger.error("Locations do not contain " + e.getTarget() + " from edge " + e);
				errors++;
			}
			if (!inEdges.get(e.getTarget()).contains(e)) {
				logger.error("In-edges do not contain out-edge " + e + " with statement " + e.getTransformer() + " and hashcode " + e.hashCode());
				errors++;
			}
		}
		
		for (CFAEdge e : inEdges.values()) {
			if (!locations.contains(e.getSource())) {
				logger.error("Locations do not contain " + e.getSource() + " from edge " + e);
				errors++;
			}
			if (!locations.contains(e.getTarget())) {
				logger.error("Locations do not contain " + e.getTarget() + " from edge " + e);
				errors++;
			}
			if (!outEdges.get(e.getSource()).contains(e)) {
				logger.error("Out-edges do not contain in-edge " + e + " with statement " + e.getTransformer() + " and hashcode " + e.hashCode());
				errors++;
			}
		}
		
		Set<Location> bbLocations = basicBlocks.keySet();

		for (CFAEdge e : bbOutEdges.values()) {
			if (!bbLocations.contains(e.getSource())) {
				logger.error("Basicblock locations do not contain " + e.getSource() + " from edge " + e);
				errors++;
			}
			if (!bbLocations.contains(e.getTarget())) {
				logger.error("Basicblock locations do not contain " + e.getTarget() + " from edge " + e);
				errors++;
			}
			if (!bbInEdges.get(e.getTarget()).contains(e)) {
				logger.error("BB in-edges do not contain out-edge " + e);
				errors++;
			}
		}
		
		for (CFAEdge e : bbInEdges.values()) {
			if (!bbLocations.contains(e.getSource())) {
				logger.error("Basicblock locations do not contain " + e.getSource() + " from edge " + e);
				errors++;
			}
			if (!bbLocations.contains(e.getTarget())) {
				logger.error("Basicblock locations do not contain " + e.getTarget() + " from edge " + e);
				errors++;
			}
			if (!bbOutEdges.get(e.getSource()).contains(e)) {
				logger.error("BB out-edges do not contain in-edge " + e);
				errors++;
			}
		}
		if (errors != 0) {
			logger.error(errors + " errors in CFG audit.");
			return false;
		} else {
			return true;
		}
	}
	
}

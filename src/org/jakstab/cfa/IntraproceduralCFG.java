package org.jakstab.cfa;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jakstab.Options;
import org.jakstab.Program;
import org.jakstab.asm.AbsoluteAddress;
import org.jakstab.rtl.statements.RTLAssume;
import org.jakstab.rtl.statements.RTLCallReturn;
import org.jakstab.rtl.statements.RTLStatement;
import org.jakstab.util.FastSet;
import org.jakstab.util.Logger;
import org.jakstab.util.Worklist;

public class IntraproceduralCFG extends ControlFlowGraph {

	private static final Logger logger = Logger.getLogger(IntraproceduralCFG.class);
	
	public IntraproceduralCFG(ControlFlowGraph cfg, String procName) {
		this(cfg, Program.getProgram().getAddressForSymbol(procName));
	}

	public IntraproceduralCFG(ControlFlowGraph cfg, AbsoluteAddress procedureHead) {
		super();
		if (procedureHead == null) {
			logger.error("IntraproceduralCFG constructor called with NULL procedure head");
			return;
		}
		
		Location headLoc = findLocation(procedureHead, cfg);
		if (headLoc == null)
			return;
		Set<CFAEdge> edges = filterEdges(cfg, headLoc);
		buildFromEdgeSet(edges);
	}
	
	private Location findLocation(AbsoluteAddress a, ControlFlowGraph cfg) {
		SortedSet<Location> candidates = new TreeSet<Location>();
		for (Location l : cfg.getNodes()) {
			if (a.equals(l.getAddress()))
				candidates.add(l);
		}
		logger.verbose("Found " + candidates.size() + " candidates for procedure entry, returning first one of " + candidates);
		if (candidates.size() == 0) {
			logger.error("Did not find procedure in CFG!");
			return null;
		}
		return candidates.iterator().next();
	}

	private Set<CFAEdge> filterEdges(ControlFlowGraph cfg, Location procHead) {
		Worklist<Location> worklist = new FastSet<Location>(procHead);
		Set<Location> visited = new HashSet<Location>();
		Set<CFAEdge> result = new HashSet<CFAEdge>();
		
		while (!worklist.isEmpty()) {
			Location loc = worklist.pick(); 
			for (CFAEdge e : cfg.getOutEdges(loc)) {
				
				RTLStatement stmt = (RTLStatement)e.getTransformer();
				if (stmt instanceof RTLAssume) {
					RTLAssume a = (RTLAssume)stmt;
					if (a.isCall()) {
						if (Options.procedureAbstraction.getValue() != 1) {
							if (a.getSource().getNextLabel() != null) {
								Location returnLoc = findReturnLocation(cfg, e, a.getSource().getNextLabel());
								
								if (returnLoc == null) {
									logger.verbose("Non-returning call to procedure " + 
											Program.getProgram().getSymbolFor(e.getTarget().getLabel()));
								} else {
									RTLCallReturn callReturn = new RTLCallReturn();
									callReturn.setLabel(a.getLabel());
									callReturn.setNextLabel(returnLoc.getLabel());
									CFAEdge fallThroughEdge = new CFAEdge(e.getSource(), returnLoc, callReturn);
									logger.debug("Generated fall-through edge " + fallThroughEdge);
									
									// Replace e with fallthrough edge to add it further down
									e = fallThroughEdge;
								}
							}
						} else {
							// don't add fall through in interproc analysis, the callReturn edge should be enough
							// TODO: Check if this works with VPC CFGs
							continue;
						}						
					} else if (a.isReturn()) {
						// don't add/follow the return edge, it delimits the procedure
						continue;
					}					
				} 
				
				result.add(e);
				
				if (visited.contains(e.getTarget()))
					continue;
				else {
					visited.add(e.getTarget());
					worklist.add(e.getTarget());
				}
			}
		}
		
		return result;
	}
	
	private Location findReturnLocation(ControlFlowGraph cfg, CFAEdge callEdge, RTLLabel returnLabel) {
		Worklist<Location> worklist = new FastSet<Location>(callEdge.getTarget());
		Set<Location> visited = new HashSet<Location>();
		while (!worklist.isEmpty()) {
			Location l = worklist.pick();
			for (CFAEdge e : cfg.getOutEdges(l)) {
				Location targetLoc = e.getTarget(); 
				if (visited.contains(targetLoc))
					continue;
				visited.add(targetLoc);
				
				if (targetLoc.getLabel().equals(returnLabel))
					return targetLoc;
				
				worklist.add(targetLoc);
			}
		}
		logger.debug("Could not find return location for call " + callEdge + " supposed to return to " + returnLabel);
		return null;
	}

}

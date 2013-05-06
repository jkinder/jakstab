package org.jakstab.cfa;

import java.util.Set;

import org.jakstab.rtl.statements.RTLAssume;
import org.jakstab.rtl.statements.RTLCallReturn;

/**
 * Control flow graph that splits basic blocks after every assume, including
 * calls, returns, and jumps.  
 */
public class FineGrainedCFG extends ControlFlowGraph {
	
	public FineGrainedCFG(Set<CFAEdge> edges) {
		super(edges);
	}

	protected boolean isBasicBlockHead(Location l) {
		if (super.isBasicBlockHead(l))
			return true;
		
		// Split blocks at any assume / callReturn
		
		CFAEdge e = getInEdges(l).iterator().next();
		
		if (e.getTransformer() instanceof RTLAssume)
			return true;
		
		if (e.getTransformer() instanceof RTLCallReturn)
			return true;

		return false;
	}

}

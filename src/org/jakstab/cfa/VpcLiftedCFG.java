package org.jakstab.cfa;

import java.util.Set;

import org.jakstab.rtl.statements.RTLAssume;
import org.jakstab.rtl.statements.RTLCallReturn;
import org.jakstab.util.Logger;

public class VpcLiftedCFG extends ControlFlowGraph {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(VpcLiftedCFG.class);
	
	public VpcLiftedCFG(Set<CFAEdge> edges) {
		super(edges);
	}

	protected boolean isBasicBlockHead(Location l) {
		if (super.isBasicBlockHead(l))
			return true;
		
		CFAEdge e = getInEdges(l).iterator().next();
		
		// Split blocks at calls / returns / callReturns
		
		if (e.getTransformer() instanceof RTLAssume) {
			RTLAssume a = (RTLAssume)e.getTransformer();
			return a.isCall() || a.isReturn();
		}
		
		if (e.getTransformer() instanceof RTLCallReturn)
			return true;

		return false;
	}
	
}

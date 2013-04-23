package org.jakstab.cfa;

import java.util.Set;

import org.jakstab.rtl.statements.RTLAssume;
import org.jakstab.rtl.statements.RTLCallReturn;

public class ProgramCFG extends ControlFlowGraph {
	
	public ProgramCFG(Set<CFAEdge> edges) {
		super(edges);
	}

	protected boolean isBasicBlockHead(Location l) {
		Set<CFAEdge> in = getInEdges(l);
		
		if (in.size() != 1)
			return true;
		
		CFAEdge e = in.iterator().next();
		
		if (e.getTransformer() instanceof RTLAssume)
			return true;
		
		if (e.getTransformer() instanceof RTLCallReturn)
			return true;

		//if (!(Program.getProgram().getStatement(e.getSource().getLabel()) instanceof RTLGoto))
		//  return true;

		return false;
	}

}

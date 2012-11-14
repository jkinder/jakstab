package org.jakstab.cfa;

import java.util.Set;

import org.jakstab.rtl.statements.RTLAssume;

public class ProgramCFG extends ControlFlowGraph {
	
	public ProgramCFG(Set<CFAEdge> edges) {
		super();
		
		for (CFAEdge e : edges) {
			addEdge(e);
		}
		
		findEntryPoint();		
		buildBasicBlocks();
		assert valid();
	}
	
	protected boolean isBasicBlockHead(Location l) {
		Set<CFAEdge> in = getInEdges(l);
		if (in.size() != 1)
			return true;
		CFAEdge e = in.iterator().next();
		if ((e.getTransformer() instanceof RTLAssume))
			return true;
		//if (!(Program.getProgram().getStatement(e.getSource().getLabel()) instanceof RTLGoto))
		//  return true;

		return false;
	}

}

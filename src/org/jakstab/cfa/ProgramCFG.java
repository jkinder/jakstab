package org.jakstab.cfa;

import java.util.HashSet;
import java.util.Set;

import org.jakstab.Program;
import org.jakstab.rtl.statements.RTLGoto;

public class ProgramCFG extends ControlFlowGraph {
	
	public ProgramCFG(Set<CFAEdge> edges) {
		super();
		
		for (CFAEdge e : edges) {
			addEdge(e);
		}
		
		buildBasicBlocks(findBasicBlockHeads());
		
		assert valid();
	}

	private Set<Location> findBasicBlockHeads() {
		Set<Location> result = new HashSet<Location>();
		for (Location l : getNodes()) {
			Set<CFAEdge> in = getInEdges(l);
			if (in != null && in.size() == 1) {
				CFAEdge e = in.iterator().next();
				if (!(Program.getProgram().getStatement(e.getSource().getLabel()) instanceof RTLGoto))
					continue;
			}
			result.add(l);
		}
		return result;
	}

}

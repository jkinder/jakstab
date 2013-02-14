package org.jakstab.cfa;

import java.util.Set;

import org.jakstab.Program;
import org.jakstab.rtl.statements.RTLGoto;
import org.jakstab.rtl.statements.RTLStatement;
import org.jakstab.util.Logger;

public class VpcLiftedCFG extends ControlFlowGraph {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(VpcLiftedCFG.class);
	
	public VpcLiftedCFG(Set<CFAEdge> edges) {
		super(edges);
	}

	protected boolean isBasicBlockHead(Location l) {
		Set<CFAEdge> in = getInEdges(l);

		if (in.size() == 0) {
			logger.debug("Orphan block at " + l);
			return true;
		}
		
		// If it has in-degree greater than 1, it's a head
		if (in.size() > 1) {
			return true;
		}
		
		// There's only one edge
		CFAEdge e = in.iterator().next();
		Location predLoc = e.getSource();
		// If out-degree of predecessor is greater than 1, this is a head
		if (getOutDegree(predLoc) > 1)
			return true;

		Program program = Program.getProgram();

		RTLStatement predStmt = program.getStatement(predLoc.getLabel());
		if (!(predStmt instanceof RTLGoto))
			return false;
		
		RTLGoto g = (RTLGoto)predStmt;
		switch (g.getType()) {
		case CALL: 
		case RETURN: 
			return true;
		}

		// If inEdge crosses into a stub, make this stub a new block
		if (!program.isStub(predLoc.getAddress()) &&
				program.isStub(l.getAddress())) {
			return true;
		}
		
		return false;
	}
	
}

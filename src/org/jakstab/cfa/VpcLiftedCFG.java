package org.jakstab.cfa;

import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jakstab.AnalysisManager;
import org.jakstab.Options;
import org.jakstab.Program;
import org.jakstab.analysis.AbstractReachabilityTree;
import org.jakstab.analysis.AbstractState;
import org.jakstab.analysis.ValueContainer;
import org.jakstab.analysis.composite.CompositeState;
import org.jakstab.analysis.explicit.BasedNumberElement;
import org.jakstab.analysis.explicit.BasedNumberValuation;
import org.jakstab.analysis.explicit.VpcTrackingAnalysis;
import org.jakstab.rtl.statements.BasicBlock;
import org.jakstab.rtl.statements.RTLGoto;
import org.jakstab.rtl.statements.RTLHalt;
import org.jakstab.rtl.statements.RTLStatement;
import org.jakstab.util.Logger;
import org.jakstab.util.Pair;

public class VpcLiftedCFG extends ControlFlowGraph {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(VpcLiftedCFG.class);
	
	public VpcLiftedCFG(AbstractReachabilityTree art) {

		super();

		reconstructCFGFromVPC(art);
		logger.debug(getNodes().size() + " VPC locations.");
		logger.debug(getEdges().size() + " edges in the VPC-CFG.");
		
		findEntryPoint();
		buildBasicBlocks();
		logger.debug(getBasicBlockEdges().size() + " basic block edges in the VPC-CFG.");

		assert valid();
	}

	
	protected boolean isBasicBlockHead(Location loc) {
		VpcLocation l = (VpcLocation)loc;
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
		VpcLocation predLoc = (VpcLocation)e.getSource();
		// If out-degree of predecessor is greater than 1, this is a head
		if (getOutDegree(predLoc) > 1)
			return true;

		RTLStatement predStmt = Program.getProgram().getStatement(predLoc.getLabel());
		if (!(predStmt instanceof RTLGoto))
			return false;
		
		RTLGoto g = (RTLGoto)predStmt;
		switch (g.getType()) {
		case CALL: 
		case RETURN: 
			return true;
		default: 
			return false;
		}
	}
	
	private VpcLocation reconstructCFGFromVPC(AbstractReachabilityTree art) {
		
		AnalysisManager mgr = AnalysisManager.getInstance();
		
		VpcTrackingAnalysis vpcAnalysis = (VpcTrackingAnalysis)mgr.getAnalysis(VpcTrackingAnalysis.class);
		
		int vAnalysisPos = 1 + Options.cpas.getValue().indexOf(mgr.getShorthand(VpcTrackingAnalysis.class));
		
		Deque<AbstractState> worklist = new LinkedList<AbstractState>();
		worklist.add(art.getRoot());
		Set<AbstractState> visited = new HashSet<AbstractState>();
		visited.add(art.getRoot());
		
		VpcLocation root = new VpcLocation(getVPC(art.getRoot(), vpcAnalysis, vAnalysisPos), 
				(RTLLabel)art.getRoot().getLocation()); 

		while (!worklist.isEmpty()) {
			AbstractState headState = worklist.removeFirst();
			BasedNumberElement vpcVal = getVPC(headState, vpcAnalysis, vAnalysisPos);
			VpcLocation headVpcLoc = new VpcLocation(vpcVal, (RTLLabel)headState.getLocation());

			Set<Pair<CFAEdge, AbstractState>> successors = art.getChildren(headState);
			for (Pair<CFAEdge, AbstractState> sPair : successors) {
				AbstractState nextState = sPair.getRight();
				CFAEdge edge = sPair.getLeft();
				
				VpcLocation vpcLoc = headVpcLoc;
				BasedNumberElement nextVpcVal = getVPC(nextState, vpcAnalysis, vAnalysisPos);
				
				List<RTLStatement> stmtList;
				if (Options.basicBlocks.getValue())
					stmtList = (BasicBlock)edge.getTransformer();
				else
					stmtList = Collections.singletonList((RTLStatement)edge.getTransformer());
				for (RTLStatement stmt : stmtList) {
					if (stmt instanceof RTLHalt)
						break;
					VpcLocation nextVpcLoc = new VpcLocation(nextVpcVal, stmt.getNextLabel());

					addEdge(new CFAEdge(vpcLoc, nextVpcLoc, stmt));
					
					vpcLoc = nextVpcLoc;
				}
				
				if (!visited.contains(nextState)) {
					visited.add(nextState);
					worklist.add(nextState);
				}
			}
			
		}
		return root;		
	}

	private BasedNumberElement getVPC(AbstractState s, VpcTrackingAnalysis vpcAnalysis, int vAnalysisPos) {
		RTLLabel l = (RTLLabel)s.getLocation();				
		ValueContainer vpcVar = vpcAnalysis.getVPC(l);
		CompositeState cState = (CompositeState)s;
		BasedNumberElement vpcVal = ((BasedNumberValuation)cState.getComponent(vAnalysisPos)).getValue(vpcVar);
		return vpcVal;
	}
	
}

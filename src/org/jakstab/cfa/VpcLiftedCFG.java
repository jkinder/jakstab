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
		buildBasicBlocks(findBasicBlockHeads());
		logger.debug(getBasicBlockEdges().size() + " basic block edges in the VPC-CFG.");

		assert valid();
	}
	
	private Set<Location> findBasicBlockHeads() {
		Set<Location> result = new HashSet<Location>();
		
		// Find basic block heads
		for (Location loc : getNodes()) {
			VpcLocation l = (VpcLocation)loc;
			Set<CFAEdge> in = getInEdges(l);
			// This can only NOT be a BB head if it has in-degree 1
			if (in.size() == 1) {
				CFAEdge e = in.iterator().next();
				VpcLocation predLoc = (VpcLocation)e.getSource();
				// Out-degree of predecessor also has to be 1
				if (getOutEdges(predLoc).size() == 1) {
					
					RTLStatement predStmt = Program.getProgram().getStatement(predLoc.getLabel());
					if (predStmt instanceof RTLGoto) {
						RTLGoto g = (RTLGoto)predStmt;
						if (g.getType() != RTLGoto.Type.CALL && 
								g.getType() != RTLGoto.Type.RETURN) {
							// Uncomment this to break blocks at VPC boundaries
							//if (predLoc.getVPC().equals(l.getVPC()))
								continue;
						}
					} else {
						// Non-goto
						continue;
					}
				}
			}
			if (in.size() == 0) {
				logger.debug("Orphan block at " + l);
			}

			result.add(l);
		}
		logger.debug(result.size() + " basic blocks in the VPC-CFG.");
		return result;
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

package org.jakstab.transformation;

import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jakstab.Algorithm;
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
import org.jakstab.cfa.CFAEdge;
import org.jakstab.cfa.ControlFlowGraph;
import org.jakstab.cfa.ProgramCFG;
import org.jakstab.cfa.RTLLabel;
import org.jakstab.cfa.VpcLiftedCFG;
import org.jakstab.cfa.VpcLocation;
import org.jakstab.rtl.statements.BasicBlock;
import org.jakstab.rtl.statements.RTLHalt;
import org.jakstab.rtl.statements.RTLStatement;
import org.jakstab.util.Logger;
import org.jakstab.util.Pair;


public class VpcCfgReconstruction implements Algorithm {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ControlFlowGraph.class);
	
	public static ControlFlowGraph reconstruct(AbstractReachabilityTree art) {
		VpcCfgReconstruction rec = new VpcCfgReconstruction(art);
		rec.run();
		return rec.getTransformedCfg();
	}

	AbstractReachabilityTree art;
	ControlFlowGraph transformedCfg;
	
	public VpcCfgReconstruction(AbstractReachabilityTree art) {
		this.art = art;
	}
	
	public ControlFlowGraph getTransformedCfg() {
		return transformedCfg;
	}

	@SuppressWarnings("unused")
	@Override
	public void run() {
		
		Set<CFAEdge> edges = reconstructCFGFromVPC(art);
		
		transformedCfg = new VpcLiftedCFG(edges);

		// Simplify CFA
		if (Options.simplifyVCFG.getValue() > 0) {
			logger.info("=== Simplifying reconstructed CFA ===");
			DeadCodeElimination dce;
			long totalRemoved = 0;
			edges = transformedCfg.getEdges();
			do {
				dce = new DeadCodeElimination(edges, true); 
				dce.run();
				edges = dce.getCFA();					
				totalRemoved += dce.getRemovalCount();
			} while (dce.getRemovalCount() > 0);

			transformedCfg = new ProgramCFG(edges);

			if (Options.simplifyVCFG.getValue() > 1) {
				ExpressionSubstitution subst = new ExpressionSubstitution(transformedCfg);
				subst.run();
				edges = subst.getCFA();

				do {
					dce = new DeadCodeElimination(edges, true); 
					dce.run();
					edges = dce.getCFA();					
					totalRemoved += dce.getRemovalCount();
				} while (dce.getRemovalCount() > 0);

				logger.info("=== Finished CFA simplification, removed " + totalRemoved + " edges. ===");
				transformedCfg = new ProgramCFG(edges);		
			}
		}
	
	}
	
	private Set<CFAEdge> reconstructCFGFromVPC(AbstractReachabilityTree art) {
		
		Set<CFAEdge> edges = new HashSet<CFAEdge>(1000);
		
		AnalysisManager mgr = AnalysisManager.getInstance();
		
		VpcTrackingAnalysis vpcAnalysis = (VpcTrackingAnalysis)mgr.getAnalysis(VpcTrackingAnalysis.class);
		
		int vAnalysisPos = 1 + Options.cpas.getValue().indexOf(mgr.getShorthand(VpcTrackingAnalysis.class));
		
		Deque<AbstractState> worklist = new LinkedList<AbstractState>();
		worklist.add(art.getRoot());
		Set<AbstractState> visited = new HashSet<AbstractState>();
		visited.add(art.getRoot());
		
		/*VpcLocation root = new VpcLocation(getVPC(art.getRoot(), vpcAnalysis, vAnalysisPos), 
				(RTLLabel)art.getRoot().getLocation());*/ 

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

					edges.add(new CFAEdge(vpcLoc, nextVpcLoc, stmt));
					
					vpcLoc = nextVpcLoc;
				}
				
				if (!visited.contains(nextState)) {
					visited.add(nextState);
					worklist.add(nextState);
				}
			}
			
		}
		return edges;		
	}

	private BasedNumberElement getVPC(AbstractState s, VpcTrackingAnalysis vpcAnalysis, int vAnalysisPos) {
		RTLLabel l = (RTLLabel)s.getLocation();

		// Do not assign a VPC value to stub methods - make them all share TOP 
		if (Program.getProgram().isImport(l.getAddress()))
			return BasedNumberElement.getTop(32);
		
		ValueContainer vpcVar = vpcAnalysis.getVPC(l);
		CompositeState cState = (CompositeState)s;
		BasedNumberElement vpcVal = ((BasedNumberValuation)cState.getComponent(vAnalysisPos)).getValue(vpcVar);
		return vpcVal;
	}	

	@Override
	public void stop() {
		// TODO Auto-generated method stub
	}

}

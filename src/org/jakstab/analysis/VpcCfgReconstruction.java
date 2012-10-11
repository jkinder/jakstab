package org.jakstab.analysis;

import java.util.Set;

import org.jakstab.Algorithm;
import org.jakstab.cfa.CFAEdge;
import org.jakstab.cfa.ControlFlowGraph;
import org.jakstab.cfa.ProgramCFG;
import org.jakstab.cfa.VpcLiftedCFG;
import org.jakstab.transformation.DeadCodeElimination;
import org.jakstab.transformation.ExpressionSubstitution;
import org.jakstab.util.Logger;


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

	@Override
	public void run() {
		ControlFlowGraph vcfg = new VpcLiftedCFG(art);
		
		// Simplify CFA
		logger.info("=== Simplifying reconstructed CFA ===");
		DeadCodeElimination dce;
		long totalRemoved = 0;
		ExpressionSubstitution subst = new ExpressionSubstitution(vcfg);
		subst.run();
		Set<CFAEdge> edges = subst.getCFA();
		do {
			dce = new DeadCodeElimination(edges); 
			dce.run();
			edges = dce.getCFA();					
			totalRemoved += dce.getRemovalCount();
		} while (dce.getRemovalCount() > 0);				
		logger.info("=== Finished CFA simplification, removed " + totalRemoved + " edges. ===");
		transformedCfg = new ProgramCFG(edges);		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
	}

}

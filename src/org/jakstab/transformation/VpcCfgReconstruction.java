package org.jakstab.transformation;

import java.util.Set;

import org.jakstab.Algorithm;
import org.jakstab.Options;
import org.jakstab.VpcCfgMain;
import org.jakstab.analysis.AbstractReachabilityTree;
import org.jakstab.cfa.CFAEdge;
import org.jakstab.cfa.ControlFlowGraph;
import org.jakstab.cfa.ProgramCFG;
import org.jakstab.cfa.VpcLiftedCFG;
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

	@SuppressWarnings("unused")
	@Override
	public void run() {
		transformedCfg = new VpcLiftedCFG(art);

		// Simplify CFA
		if (Options.simplifyVCFG.getValue() > 0) {
			logger.info("=== Simplifying reconstructed CFA ===");
			DeadCodeElimination dce;
			long totalRemoved = 0;
			Set<CFAEdge> edges = transformedCfg.getEdges();
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
		
		VpcCfgMain.fanOut = (double)transformedCfg.getBasicBlockEdges().size() / 
				(double)transformedCfg.getBasicBlockNodes().size();
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
	}

}

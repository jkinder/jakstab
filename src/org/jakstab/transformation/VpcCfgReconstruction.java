package org.jakstab.transformation;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import org.jakstab.cfa.Location;
import org.jakstab.cfa.ProgramCFG;
import org.jakstab.cfa.RTLLabel;
import org.jakstab.cfa.VpcLiftedCFG;
import org.jakstab.cfa.VpcLocation;
import org.jakstab.rtl.Context;
import org.jakstab.rtl.expressions.RTLMemoryLocation;
import org.jakstab.rtl.expressions.RTLVariable;
import org.jakstab.rtl.expressions.Writable;
import org.jakstab.rtl.statements.BasicBlock;
import org.jakstab.rtl.statements.RTLHalt;
import org.jakstab.rtl.statements.RTLSkip;
import org.jakstab.rtl.statements.RTLStatement;
import org.jakstab.util.Lattices;
import org.jakstab.util.Logger;
import org.jakstab.util.Pair;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;


public class VpcCfgReconstruction implements Algorithm {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ControlFlowGraph.class);
	
	public static ControlFlowGraph reconstruct(AbstractReachabilityTree art) {
		VpcCfgReconstruction rec = new VpcCfgReconstruction(art);
		rec.run();
		return rec.getTransformedCfg();
	}

	private AbstractReachabilityTree art;
	private ControlFlowGraph transformedCfg;
	private VpcTrackingAnalysis vpcAnalysis;
	private int vAnalysisPos;
	
	public VpcCfgReconstruction(AbstractReachabilityTree art) {
		this.art = art;

		AnalysisManager mgr = AnalysisManager.getInstance();
		this.vpcAnalysis = (VpcTrackingAnalysis)mgr.getAnalysis(VpcTrackingAnalysis.class);
		this.vAnalysisPos = 1 + Options.cpas.getValue().indexOf(mgr.getShorthand(VpcTrackingAnalysis.class));
	}
	
	public ControlFlowGraph getTransformedCfg() {
		return transformedCfg;
	}

	@SuppressWarnings("unused")
	@Override
	public void run() {
		
		Map<Location, AbstractState> constants = flattenArtOntoVpcLocations();
		
		Set<CFAEdge> edges = reconstructCFGFromVPC(constants);
		
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
	
	private Set<CFAEdge> reconstructCFGFromVPC(Map<Location, AbstractState> constants) {
		
		Set<CFAEdge> edges = new HashSet<CFAEdge>(1000);
		
		Deque<AbstractState> worklist = new LinkedList<AbstractState>();
		worklist.add(art.getRoot());
		Set<AbstractState> visited = new HashSet<AbstractState>();
		visited.add(art.getRoot());
		
		while (!worklist.isEmpty()) {
			AbstractState headState = worklist.removeFirst();
			BasedNumberElement vpcVal = getVPC(headState);
			VpcLocation headVpcLoc = new VpcLocation(vpcVal, (RTLLabel)headState.getLocation());

			Set<Pair<CFAEdge, AbstractState>> successors = art.getChildren(headState);
			for (Pair<CFAEdge, AbstractState> sPair : successors) {
				AbstractState nextState = sPair.getRight();
				CFAEdge edge = sPair.getLeft();
				
				VpcLocation vpcLoc = headVpcLoc;
				BasedNumberElement nextVpcVal = getVPC(nextState);
				
				List<RTLStatement> stmtList;
				if (Options.basicBlocks.getValue())
					stmtList = (BasicBlock)edge.getTransformer();
				else
					stmtList = Collections.singletonList((RTLStatement)edge.getTransformer());
				for (RTLStatement stmt : stmtList) {
					if (stmt instanceof RTLHalt)
						break;
					VpcLocation nextVpcLoc = new VpcLocation(nextVpcVal, stmt.getNextLabel());
					
					AbstractState flattenedStateAtStart = constants.get(vpcLoc);
					if (flattenedStateAtStart != null)
						stmt = substituteStatement(stmt, flattenedStateAtStart);
					
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
	
	/**
	 * Fold ART into a map from VPC locations to sets of abstract states, and
	 * then flatten the state sets into single abstract states by joining.
	 * 
	 * @return a map from VPC locations to the join of all abstract states at 
	 * that VPC location
	 */
	private Map<Location, AbstractState> flattenArtOntoVpcLocations() {

		SetMultimap<Location, AbstractState> vpcSensitiveReached = HashMultimap.create();
		
		Deque<AbstractState> worklist = new LinkedList<AbstractState>();
		worklist.add(art.getRoot());
		Set<AbstractState> visited = new HashSet<AbstractState>();
		visited.add(art.getRoot());
		
		while (!worklist.isEmpty()) {
			AbstractState headState = worklist.removeFirst();
			BasedNumberElement vpcVal = getVPC(headState);
			VpcLocation headVpcLoc = new VpcLocation(vpcVal, (RTLLabel)headState.getLocation());

			vpcSensitiveReached.put(headVpcLoc, headState);

			Set<Pair<CFAEdge, AbstractState>> successors = art.getChildren(headState);
			for (Pair<CFAEdge, AbstractState> sPair : successors) {
				AbstractState nextState = sPair.getRight();
				
				if (!visited.contains(nextState)) {
					visited.add(nextState);
					worklist.add(nextState);
				}
			}
		}
		
		Map<Location, AbstractState> constants = new HashMap<Location, AbstractState>();
		for (Location l : vpcSensitiveReached.keySet()) {
			constants.put(l, Lattices.joinAll(vpcSensitiveReached.get(l)));
		}
		
		return constants;
	}
	
	private BasedNumberElement getVPC(AbstractState s) {
		RTLLabel l = (RTLLabel)s.getLocation();

		// Do not assign a VPC value to stub methods - make them all share TOP 
		if (Program.getProgram().isImport(l.getAddress()))
			return BasedNumberElement.getTop(32);
		
		ValueContainer vpcVar = vpcAnalysis.getVPC(l);
		CompositeState cState = (CompositeState)s;
		BasedNumberElement vpcVal = ((BasedNumberValuation)cState.getComponent(vAnalysisPos)).getValue(vpcVar);
		return vpcVal;
	}
	
	private boolean assignWritable(Context ctx, Writable w, BasedNumberValuation bnv) {
		BasedNumberElement value = bnv.abstractEval(w);
		if (value.hasUniqueConcretization()) {
			ctx.addAssignment(w, value.concretize().iterator().next());
			return true;
		} else {
			return false;
		}
	}
	
	public RTLStatement substituteStatement(RTLStatement stmt, AbstractState s) {
		CompositeState cState = (CompositeState)s;
		BasedNumberValuation bnv = ((BasedNumberValuation)cState.getComponent(vAnalysisPos));
		Context substCtx = new Context();

		boolean assigned = false;		
		for (RTLMemoryLocation m : stmt.getUsedMemoryLocations())
			assigned |= assignWritable(substCtx, m, bnv);
		for (RTLVariable v : stmt.getUsedVariables())
			assigned |= assignWritable(substCtx, v, bnv);	

		if (!assigned)
			return stmt;
		
		RTLStatement newStmt = stmt.copy().evaluate(substCtx);
		if (newStmt != null) {
			return newStmt.evaluate(new Context());
		} else {
			RTLSkip skip = new RTLSkip();
			skip.setLabel(stmt.getLabel());
			skip.setNextLabel(stmt.getNextLabel());
			return skip;
		}
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
	}

}

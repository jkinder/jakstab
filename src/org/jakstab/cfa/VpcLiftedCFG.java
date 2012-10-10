package org.jakstab.cfa;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jakstab.AnalysisManager;
import org.jakstab.Options;
import org.jakstab.Program;
import org.jakstab.analysis.AbstractReachabilityTree;
import org.jakstab.analysis.AbstractState;
import org.jakstab.analysis.composite.CompositeState;
import org.jakstab.analysis.explicit.BasedNumberElement;
import org.jakstab.analysis.explicit.BasedNumberValuation;
import org.jakstab.analysis.explicit.VpcTrackingAnalysis;
import org.jakstab.rtl.expressions.RTLVariable;
import org.jakstab.rtl.statements.BasicBlock;
import org.jakstab.rtl.statements.RTLGoto;
import org.jakstab.rtl.statements.RTLHalt;
import org.jakstab.rtl.statements.RTLStatement;
import org.jakstab.util.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class VpcLiftedCFG {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(VpcLiftedCFG.class);
	
	private SetMultimap<VpcLocation, VpcLocation> outEdges;
	private SetMultimap<VpcLocation, VpcLocation> inEdges;
	
	private SetMultimap<VpcLocation, VpcLocation> bbOutEdges;
	private SetMultimap<VpcLocation, VpcLocation> bbInEdges;
	private Map<VpcLocation, BasicBlock> basicBlocks;
	private Set<VpcLocation> locations;
	
	public VpcLiftedCFG(AbstractReachabilityTree art) {
		
		outEdges = HashMultimap.create();
		inEdges = HashMultimap.create();
		locations = new HashSet<VpcLocation>();
		
		reconstructCFGFromVPC(art);
		logger.debug(locations.size() + " VPC locations.");
		logger.debug(outEdges.size() + " edges in the VPC-CFG.");

		bbOutEdges = HashMultimap.create();
		bbInEdges = HashMultimap.create();
		basicBlocks = new HashMap<VpcLocation, BasicBlock>();
		Program program = Program.getProgram();
		
		// Find basic block heads
		for (VpcLocation l : locations) {
			Set<VpcLocation> in = inEdges.get(l);
			// This can only NOT be a BB head if it has in-degree 1
			if (in.size() == 1) {
				VpcLocation e = in.iterator().next();
				// Out-degree of predecessor also has to be 1
				if (outEdges.get(e).size() == 1) {

					RTLStatement predStmt = program.getStatement(e.getLocation());
					if (predStmt instanceof RTLGoto) {
						RTLGoto g = (RTLGoto)predStmt;
						if (g.getType() != RTLGoto.Type.CALL && 
								g.getType() != RTLGoto.Type.RETURN) {
							// If vpcs are equal, don't start a new head even for Gotos
							if (e.getVPC().equals(l.getVPC()))
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
			// Create new basic block from location
			BasicBlock bb = new BasicBlock();
			basicBlocks.put(l, bb);
		}
		logger.debug(basicBlocks.size() + " basic blocks in the VPC-CFG.");
		
		
		for (Map.Entry<VpcLocation, BasicBlock> entry : basicBlocks.entrySet()) {
			VpcLocation head = entry.getKey();
			BasicBlock bb = entry.getValue();
			
			bb.add(program.getStatement(head.getLocation()));
			VpcLocation l = head;
			Set<VpcLocation> out = outEdges.get(l);
			while (out != null && out.size() == 1) {
				l = out.iterator().next();
				if (basicBlocks.containsKey(l))
					break;
				bb.add(program.getStatement(l.getLocation()));
				out = outEdges.get(l);
			}
			
			for (VpcLocation e : out) {
				bbOutEdges.put(head, e);
				bbInEdges.put(e, head);
			}
		}
		
		logger.debug(bbOutEdges.size() + " basic block edges in the VPC-CFG.");

	}
	
	public Set<VpcLocation> getNodes() {
		return Collections.unmodifiableSet(locations);
	}
	
	public Set<Map.Entry<VpcLocation, VpcLocation>> getEdges() {
		return outEdges.entries();
	}
	
	public BasicBlock getBasicBlock(VpcLocation l) {
		return basicBlocks.get(l);
	}
	
	public Set<Map.Entry<VpcLocation, VpcLocation>> getBasicBlockEdges() {
		return bbOutEdges.entries();
	}
	
	public Set<VpcLocation> getBasicBlockNodes() {
		return Collections.unmodifiableSet(basicBlocks.keySet());
	}
	
	public Set<BasicBlock> getBasicBlocks() {
		return Collections.unmodifiableSet(
				new HashSet<BasicBlock>(basicBlocks.values()));
	}
	
	private VpcLocation reconstructCFGFromVPC(AbstractReachabilityTree art) {
		
		Program program = Program.getProgram();
		AnalysisManager mgr = AnalysisManager.getInstance();
		ControlFlowGraph cfg = program.getCFG();
		
		SetMultimap<RTLLabel, CFAEdge> cfaEdges = HashMultimap.create();
		for (CFAEdge e : program.getCFA()) {
			cfaEdges.put(e.getSource(), e);
		}

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
			locations.add(headVpcLoc);

			Set<AbstractState> successors = art.getChildren(headState);
			for (AbstractState nextState : successors) {

				VpcLocation vpcLoc = headVpcLoc;
				BasedNumberElement nextVpcVal = getVPC(nextState, vpcAnalysis, vAnalysisPos);
				
				CFAEdge edge = getEdgeBetween(cfaEdges, 
						(RTLLabel)headState.getLocation(), 
						(RTLLabel)nextState.getLocation());

				List<RTLStatement> stmtList;
				if (Options.basicBlocks.getValue())
					stmtList = (BasicBlock)edge.getTransformer();
				else
					stmtList = Collections.singletonList((RTLStatement)edge.getTransformer());
				for (RTLStatement stmt : stmtList) {
					if (stmt instanceof RTLHalt)
						break;
					VpcLocation nextVpcLoc = new VpcLocation(nextVpcVal, stmt.getNextLabel());
					assert cfg.getSuccessorLocations(vpcLoc.getLocation()).contains(nextVpcLoc.getLocation());
					outEdges.put(vpcLoc, nextVpcLoc);
					inEdges.put(nextVpcLoc, vpcLoc);
					locations.add(nextVpcLoc);
					
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
		RTLVariable vpcVar = vpcAnalysis.getVPC(l);
		CompositeState cState = (CompositeState)s;
		BasedNumberElement vpcVal = ((BasedNumberValuation)cState.getComponent(vAnalysisPos)).getValue(vpcVar);
		return vpcVal;
	}
	
	private CFAEdge getEdgeBetween(SetMultimap<RTLLabel, CFAEdge> cfaEdges, RTLLabel src, RTLLabel tgt) {
		Set<CFAEdge> out = cfaEdges.get(src);
		if (out != null) for (CFAEdge e : out)
			if (e.getTarget().equals(tgt))
				return e;
		return null;
	}
	

}

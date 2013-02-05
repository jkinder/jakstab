package org.jakstab.cfa;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jakstab.Program;
import org.jakstab.asm.Instruction;
import org.jakstab.rtl.expressions.ExpressionFactory;
import org.jakstab.rtl.statements.RTLAssume;
import org.jakstab.rtl.statements.RTLGoto;
import org.jakstab.rtl.statements.RTLStatement;
import org.jakstab.util.Pair;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class AsmCFG {
	
	private Map<Location, Instruction> nodes;
	private SetMultimap<Location, Pair<Location, Object>> edges;
	
	public AsmCFG(ControlFlowGraph cfg) {
		
		nodes = new HashMap<Location, Instruction>();
		edges = HashMultimap.create();
		
		Program p = Program.getProgram();
		for (Location l : cfg.getNodes()) {
			// the instruction inserted here might be null (if it's a stub/prologue location)
			Location iLoc = instructionLoc(l);
			if (!nodes.containsKey(iLoc))
				nodes.put(iLoc, p.getInstruction(l.getAddress()));
		}
		
		for (CFAEdge e : cfg.getEdges()) {
			// If this is a forward intra-instruction-edge, skip it
			if (e.getSource().getAddress().equals(e.getTarget().getAddress()) && 
					e.getSource().getLabel().getIndex() < e.getTarget().getLabel().getIndex())
				continue;
			
			RTLStatement stmt = (RTLStatement)e.getTransformer();
			String label = "";
			
			if (stmt instanceof RTLAssume) {
				// Get the original goto from the program (not the converted assume) 
				RTLGoto rtlGoto = ((RTLAssume) stmt).getSource();

				// If this is the fall-through edge, output F, otherwise T
				// If the assume in the edge has the same nextlabel as Goto, then it's the fall-through
				if (rtlGoto != null && !rtlGoto.getCondition().equals(ExpressionFactory.TRUE))
					label = stmt.getNextLabel().equals(rtlGoto.getNextLabel()) ? "F" : "T";
			}

			edges.put(instructionLoc(e.getSource()), Pair.create(instructionLoc(e.getTarget()), (Object)label));
		}
	}
	
	private Location instructionLoc(Location l) {
		if (l.getLabel().getIndex() == 0)
			return l;
		if (l instanceof VpcLocation) {
			return new VpcLocation(((VpcLocation) l).getVPC(), new RTLLabel(l.getAddress(), 0));
		} else if (l instanceof RTLLabel) {
			return new RTLLabel(l.getAddress(), 0); 
		}
		throw new IllegalArgumentException("Unsupported location type");
	}
	
	public Instruction getInstruction(Location l) {
		return nodes.get(l);
	}
	
	public void setInstruction(Location l, Instruction i) {
		nodes.put(l, i);
	}
	
	public Map<Location, Instruction> getNodes() {
		return nodes;
	}
	
	public SetMultimap<Location, Pair<Location, Object>> getEdges() {
		return edges;
	}

	public Set<Pair<Location, Object>> getOutEdges(Location l) {
		return edges.get(l);
	}

}

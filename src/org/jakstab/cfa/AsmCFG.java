package org.jakstab.cfa;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.jakstab.Program;
import org.jakstab.asm.BranchInstruction;
import org.jakstab.asm.Instruction;
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
		
		/*
		 * When constructing an ASM CFG from a CFA, we have the following problem:
		 * The VPC can change within an instruction, so if we just skip intra-instruction
		 * edges, the VPC value for CFA edge entering the instruction can be different from
		 * the one for the CFA edge leaving the instruction. In that case, the instruction 
		 * label is different, and the edges don't connect.
		 * 
		 * Therefore, we define the VPC of an instruction location to be the VPC at the 
		 * instruction entry.
		 * 
		 * We can't just iterate over all VPC locations and check whether an VPC, address pair
		 * already has an instruction, because this might put an instruction twice if the VPC
		 * changes in the middle of it.  
		 */
		
		nodes = new HashMap<Location, Instruction>();
		edges = HashMultimap.create();
		
		Program p = Program.getProgram();
		
		Deque<Location> worklist = new LinkedList<Location>();
		worklist.add(cfg.getEntryPoint());
		
		while (!worklist.isEmpty()) {
			Location instrEntry = worklist.pop();
			if (nodes.containsKey(instrEntry))
				continue;
			// the instruction might be null (if it's a stub/prologue location)
			Instruction instr = p.getInstruction(instrEntry.getAddress());
			nodes.put(instrEntry, instr);
			
			Set<CFAEdge> outEdges;
			Location curLoc = instrEntry;
			do {
				outEdges = cfg.getOutEdges(curLoc);
				if (outEdges.size() != 1)
					break;
				curLoc = outEdges.iterator().next().getTarget();				
			} while (curLoc.getAddress().equals(instrEntry.getAddress()) && 
					curLoc.getLabel().getIndex() >= instrEntry.getLabel().getIndex());
			
			for (CFAEdge e : outEdges) {
				RTLStatement stmt = (RTLStatement)e.getTransformer();
				String label = "";
				
				if (instr instanceof BranchInstruction) {
					assert stmt instanceof RTLAssume;
					BranchInstruction bi = (BranchInstruction)instr;
					if (bi.isConditional()) {
						// Get the original goto from the program (not the converted assume) 
						RTLGoto rtlGoto = ((RTLAssume) stmt).getSource();

						// If this is the fall-through edge, output F, otherwise T
						// If the assume in the edge has the same nextlabel as Goto, then it's the fall-through
						label = stmt.getNextLabel().equals(rtlGoto.getNextLabel()) ? "F" : "T";
					}
				}

				edges.put(instrEntry, Pair.create(e.getTarget(), (Object)label));
				worklist.add(e.getTarget());
			}
				
		}

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

/*
 * Harness to call every function exported by a library. First function is DllMain
 */
package org.jakstab.loader;

import java.util.LinkedList;

import org.jakstab.Program;
import org.jakstab.analysis.MemoryRegion;
import org.jakstab.asm.AbsoluteAddress;
import org.jakstab.cfa.RTLLabel;
import org.jakstab.rtl.expressions.ExpressionFactory;
import org.jakstab.rtl.expressions.RTLVariable;
import org.jakstab.rtl.statements.ILBuilder;
import org.jakstab.rtl.statements.RTLAlloc;
import org.jakstab.rtl.statements.RTLGoto;
import org.jakstab.rtl.statements.RTLHalt;
import org.jakstab.rtl.statements.RTLStatement;
import org.jakstab.rtl.statements.RTLVariableAssignment;
import org.jakstab.rtl.statements.StatementSequence;
import org.jakstab.util.Logger;

public class LibraryHarness implements Harness {

	private LinkedList<AbsoluteAddress> entryPoints;
	
	private RTLVariable esp = Program.getProgram().getArchitecture().stackPointer();

	private static int CALL_INSTR_DISTANCE = 1;

	private static Logger logger = Logger.getLogger(LibraryHarness.class);
	
	private AbsoluteAddress lastAddress;

	
	public LibraryHarness() {
		entryPoints = new LinkedList<AbsoluteAddress>();
		
		Program program = Program.getProgram();
		
		// add all exports
		for (ExportedSymbol es: program.getSymbols()) {
			entryPoints.add(es.getAddress());
		}
		
		lastAddress = new AbsoluteAddress(0);
		
		logger.info("DLL analysis unlocked, adding exports");
	}
	
	@Override
	public void install(Program program) {
		StatementSequence seq = new StatementSequence();
		seq.addLast(new RTLVariableAssignment(1, ExpressionFactory.createVariable("%DF", 1), ExpressionFactory.FALSE));
		seq.addLast(new RTLAlloc(esp, MemoryRegion.STACK.toString()));
		
		// Allocate TLS depending on OS type
		if (program.getTargetOS() == Program.TargetOS.WINDOWS)
			seq.addLast(new RTLAlloc(ExpressionFactory.createVariable("%fs", 16), "FS"));
		else if (program.getTargetOS() == Program.TargetOS.LINUX)
			seq.addLast(new RTLAlloc(ExpressionFactory.createVariable("%gs", 16), "GS"));
		
		// push the return address for the first goto
		AbsoluteAddress returnAddress = new AbsoluteAddress(prologueAddress.getValue() + CALL_INSTR_DISTANCE);
		ILBuilder.getInstance().createPush(returnAddress.toNumericConstant(), seq);
		// jump to the entry point
		seq.addLast(new RTLGoto(program.getStart().getAddress().toNumericConstant(), RTLGoto.Type.CALL));

		// add the sequence for DllMain
		putSequence(program, seq, prologueAddress);
		
		// now, create calls to all exports
		for (AbsoluteAddress export: entryPoints) {
			// start a new instruction sequence
			seq = new StatementSequence();
			
			AbsoluteAddress currentAddress = returnAddress;
			lastAddress = currentAddress;

			// the new return address is either the following call or the epilogue
			returnAddress = entryPoints.peekLast() != export ? new AbsoluteAddress(currentAddress.getValue() + CALL_INSTR_DISTANCE) : epilogueAddress;
			
			// clear registers
			for (RTLVariable v : program.getArchitecture().getRegisters()) {
				if (!v.equals(esp)) {
					clearReg(seq, v);
				}
			}
			
			// reset esp
			clearReg(seq, esp);
			seq.addLast(new RTLAlloc(esp, MemoryRegion.STACK.toString()));
			
			// push new return address
			ILBuilder.getInstance().createPush(returnAddress.toNumericConstant(), seq);
			//push32(seq, returnAddress.toNumericConstant());
			// add the call
			seq.addLast(new RTLGoto(export.toNumericConstant(), RTLGoto.Type.CALL));
			
			//logger.info("Adding call to " + export + " at " + currentAddress);
			putSequence(program, seq, currentAddress);
		}
		
		// set the prologue as the entry address of the program
		program.setEntryAddress(prologueAddress);
		
		// epilogue with halt statement
		seq = new StatementSequence();
		seq.addLast(new RTLHalt());
		putSequence(program, seq, epilogueAddress);
	}

	private void clearReg(StatementSequence seq, RTLVariable v) {
		seq.addLast(new RTLVariableAssignment(v.getBitWidth(), v, ExpressionFactory.nondet(v.getBitWidth())));
	}
	
	private void putSequence(Program program, StatementSequence seq, AbsoluteAddress address) {
		int rtlId = 0;
		for (RTLStatement stmt : seq) {
			stmt.setLabel(address, rtlId++);
			stmt.setNextLabel(new RTLLabel(address, rtlId));
		}
		seq.getLast().setNextLabel(null);

		// add stub statements to program
		for (RTLStatement s : seq) {
			program.putStatement(s);
		}
	}
	
	@Override
	public boolean contains(AbsoluteAddress a) {
		return a.getValue() >= PROLOGUE_BASE && a.getValue() <= lastAddress.getValue();
	}

	@Override
	public AbsoluteAddress getFallthroughAddress(AbsoluteAddress a) {
		if (a.getValue() >= PROLOGUE_BASE && a.getValue() < lastAddress.getValue()) {
			logger.info("Providing fall through address");
			return new AbsoluteAddress(a.getValue() + CALL_INSTR_DISTANCE);
		} else if (a.equals(lastAddress)) {
			logger.info("Providing last fall through address (epilogue)");
			return epilogueAddress;
		} else { 
			return null;
		}
	}

}

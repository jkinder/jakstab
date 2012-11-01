package org.jakstab.rtl.statements;

import org.jakstab.Program;
import org.jakstab.asm.AbsoluteAddress;
import org.jakstab.cfa.RTLLabel;
import org.jakstab.rtl.expressions.ExpressionFactory;
import org.jakstab.rtl.expressions.RTLExpression;
import org.jakstab.rtl.expressions.RTLVariable;
import org.jakstab.ssl.Architecture;

public class ILBuilder {
	
	private static ILBuilder instance = new ILBuilder();
	
	public static ILBuilder getInstance() { 
		return instance; 
	}
	
	private RTLVariable sp;
	private Architecture arch;
	
	private ILBuilder() {
		Program program = Program.getProgram();
		arch = program.getArchitecture();
		
		sp = arch.stackPointer();
	}
	
	/**
	 * Add a the statements equivalent to a push instruction to the end of a sequence.
	 *  
	 * @param value the value to push
	 * @param seq the sequence to add the statements to
	 */
	public void createPush(RTLExpression value, StatementSequence seq) {
		createSPIncrement(-(value.getBitWidth() / 8), seq);
		seq.addLast(new RTLMemoryAssignment(ExpressionFactory.createMemoryLocation(
				sp, arch.getAddressBitWidth()), value));
	}
	
	public void createSPIncrement(int delta, StatementSequence seq) {
		seq.addLast(new RTLVariableAssignment(
				sp.getBitWidth(), 
				sp, 
				ExpressionFactory.createPlus(sp, delta)));
	}
	
	public void linkAndStoreSequence(AbsoluteAddress address, StatementSequence seq) {
		int rtlId = 0;
		for (RTLStatement stmt : seq) {
			stmt.setLabel(address, rtlId++);
			stmt.setNextLabel(new RTLLabel(address, rtlId));
		}
		seq.getLast().setNextLabel(null);

		// add stub statements to program
		for (RTLStatement s : seq)
			Program.getProgram().putStatement(s);
	}
	
}

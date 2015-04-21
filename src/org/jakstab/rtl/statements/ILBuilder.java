/*
 * ILBuilder.java - This file is part of the Jakstab project.
 * Copyright 2007-2015 Johannes Kinder <jk@jakstab.org>
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, see <http://www.gnu.org/licenses/>.
 */
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

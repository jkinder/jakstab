/*
 * LinuxStubLibrary.java - This file is part of the Jakstab project.
 * Copyright 2007-2012 Johannes Kinder <jk@jakstab.org>
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
package org.jakstab.loader;

import java.util.HashMap;
import java.util.Map;

import org.jakstab.Program;
import org.jakstab.asm.AbsoluteAddress;
import org.jakstab.asm.DummySymbolFinder;
import org.jakstab.asm.SymbolFinder;
import org.jakstab.rtl.expressions.ExpressionFactory;
import org.jakstab.rtl.expressions.RTLExpression;
import org.jakstab.rtl.expressions.RTLSpecialExpression;
import org.jakstab.rtl.statements.*;
import org.jakstab.rtl.statements.RTLGoto.Type;
import org.jakstab.ssl.Architecture;
import org.jakstab.util.Logger;

/**
 * @author Johannes Kinder
 */
public class LinuxStubLibrary implements StubProvider {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(LinuxStubLibrary.class);
	
	private Map<String,AbsoluteAddress> activeStubs;
	private int impId;
	private Architecture arch;
	//private RTLExpression arg1;

	public LinuxStubLibrary(Architecture arch) {
		this.arch = arch;
		activeStubs = new HashMap<String, AbsoluteAddress>();
		impId = 0;
		//arg1 = ExpressionFactory.createMemoryLocation(ExpressionFactory.createPlus(arch.stackPointer(), 8), 32);
	}
	
	private AbsoluteAddress createStubInstance(String library, String function) {

		boolean returns = true;
		if (function.equals("exit")) {
			returns = false;
		}
		
		impId += 0x10;
		AbsoluteAddress address = new AbsoluteAddress(STUB_BASE + impId);
		
		StatementSequence seq = new StatementSequence();
		ILBuilder builder = ILBuilder.getInstance();
		
		// start_main is special
		if (function.equals("__libc_start_main")) {
			
			// env
			builder.createPush(ExpressionFactory.nondet(arch.getAddressBitWidth()), seq);
			// argv
			builder.createPush(ExpressionFactory.nondet(arch.getAddressBitWidth()), seq);
			// argc
			builder.createPush(ExpressionFactory.nondet(arch.getAddressBitWidth()), seq);

			// return address (use epilogue to directly jump to halt)
			builder.createPush(ExpressionFactory.createNumber(DefaultHarness.epilogueAddress), seq);

			// address of program main is passed as arg0 to __libc_start_main
			// args0 is 5 dwords from esp: 3 for main's args, 1 for the return address, and 1 
			// for the return address from the entry point code   
			RTLExpression arg0 = ExpressionFactory.createMemoryLocation(ExpressionFactory.createPlus(
					arch.stackPointer(), 
					5 * arch.getAddressBitWidth() / 8),
					32);
			seq.addLast(new RTLGoto(arg0, Type.CALL));
			
			// Complete pseudo call to main  
			builder.linkAndStoreSequence(address, seq);
			return address;
		}
		// manual printf stub, helps debugging
		else if (function.equals("printf")) {
			seq.addLast(new RTLDebugPrint("Call to printf, format @ %esp =", 
					ExpressionFactory.createSpecialExpression(RTLSpecialExpression.DBG_PRINTF, 
							ExpressionFactory.createPlus(arch.stackPointer(), 4))));
		} 
		// Any other function clobbers eax, ecx, edx by default
		else {
			seq.addLast(new RTLVariableAssignment(32, ExpressionFactory.createVariable("%eax"), ExpressionFactory.nondet(32)));
			seq.addLast(new RTLVariableAssignment(32, ExpressionFactory.createVariable("%ecx"), ExpressionFactory.nondet(32)));
			seq.addLast(new RTLVariableAssignment(32, ExpressionFactory.createVariable("%edx"), ExpressionFactory.nondet(32)));
		}
		
		// store return address in retaddr
		if (returns) {
			seq.addLast(new RTLVariableAssignment(32, arch.returnAddressVariable(), 
					ExpressionFactory.createMemoryLocation(arch.stackPointer(), 
							arch.stackPointer().getBitWidth())
			));
		}

		// pop PC
		int stackIncrement = arch.programCounter().getBitWidth() / 8;

		// adjust stack pointer
		builder.createSPIncrement(stackIncrement, seq);

		if (returns) {
			// Read return address from temporary variable
			seq.addLast(new RTLGoto(Program.getProgram().getArchitecture().returnAddressVariable(), RTLGoto.Type.RETURN));
		} else {
			// artificial termination statement
			seq.addLast(new RTLHalt());
		}
		
		builder.linkAndStoreSequence(address, seq);
		
		return address;
	}


	/*
	 * @see org.jakstab.loader.StubProvider#resolveSymbol(java.lang.String, java.lang.String)
	 */
	@Override
	public AbsoluteAddress resolveSymbol(String library, String symbol) {
		if (library == null) return null;
		AbsoluteAddress functionAddress;
		if (activeStubs.containsKey(symbol))
			functionAddress = activeStubs.get(symbol);
		else {
			// create a new stub instance
			functionAddress = createStubInstance(library, symbol);
			activeStubs.put(symbol, functionAddress);
			logger.debug("Created new stub for " + symbol + "@" + library);
		}
		return functionAddress;
	}

	@Override
	public SymbolFinder getSymbolFinder() {
		return new DummySymbolFinder();
	}

}

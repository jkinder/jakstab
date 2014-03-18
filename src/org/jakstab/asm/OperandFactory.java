/*
 * OperandFactory.java - This file is part of the Jakstab project.
 * Copyright 2007-2014 Johannes Kinder <jk@jakstab.org>
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
package org.jakstab.asm;

import org.jakstab.rtl.expressions.RTLExpression;
import org.jakstab.rtl.expressions.RTLMemoryLocation;
import org.jakstab.rtl.expressions.RTLNumber;
import org.jakstab.util.Logger;

/**
 * Static factory methods for creating ASM operands from RTL expressions. 
 */
public class OperandFactory {
	
	private static final Logger logger = Logger.getLogger(OperandFactory.class);

	public static Operand createOperand(RTLExpression e) {
		
		if (e instanceof RTLNumber) {
			return new Immediate(((RTLNumber) e).longValue(), DataType.uIntfromBits(e.getBitWidth()));
		} else if (e instanceof RTLMemoryLocation) {
			//(RTLMemoryLocation m = (RTLMemoryLocation)e;
			//return new X86MemoryOperand(DataType.uIntfromBits(e.getBitWidth()), null, null, null, 0, 0)
			// TODO: implement
			return null;
		} else {
			logger.warn("Cannot create operand for expression type of " + e);
			return null;
		}
	}

}

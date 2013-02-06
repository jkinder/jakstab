/**
 * 
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
	
	@SuppressWarnings("unused")
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

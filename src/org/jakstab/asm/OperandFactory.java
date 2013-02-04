/**
 * 
 */
package org.jakstab.asm;

import org.jakstab.rtl.expressions.RTLExpression;
import org.jakstab.rtl.expressions.RTLNumber;

/**
 * Static factory methods for creating ASM operands from RTL expressions. 
 */
public class OperandFactory {
	
	public static Operand createOperand(RTLExpression e) {
		
		if (e instanceof RTLNumber) {
			return new Immediate(((RTLNumber) e).longValue(), DataType.uIntfromBits(e.getBitWidth()));
		} else {
			throw new IllegalArgumentException("Cannot create operand for expression type of " + e);
		}
	}

}

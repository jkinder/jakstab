/*
 * ExpressionFactory.java - This file is part of the Jakstab project.
 * Copyright 2007-2011 Johannes Kinder <jk@jakstab.org>
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

package org.jakstab.rtl.expressions;

import java.util.*;

import org.jakstab.util.Logger;
import org.jakstab.asm.*;
import org.jakstab.asm.x86.*;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Factory class for all RTL expressions. It is a singleton that holds the references to variables 
 * and commonly used constants. 
 * 
 * @author Johannes Kinder
 */
public class ExpressionFactory {

	private static final Logger logger = Logger.getLogger(ExpressionFactory.class);
	// This should be a multiple of 64 for use as bitset size
	public static final int DEFAULT_VARIABLE_COUNT = 128;
	private static ExpressionFactory instance = new ExpressionFactory();

	/**
	 * Get the singleton instance of the factory.
	 * 
	 * @return an ExpressionFactory instance
	 */
	public static ExpressionFactory getInstance() {
		return instance;
	}
	
	public final RTLNumber TRUE;
	public final RTLNumber FALSE;

	public final RTLVariable pc;
	public final RTLVariable SKIP;
	public final RTLVariable REPEAT;

	private int uniqueVariableCount;
	private final Map<String, RTLVariable> variableInstances;
	private ArrayList<RTLVariable> variableArray;
	private final RTLNondet[] nondetArray;

	private final Map<RTLVariable, RTLBitRange> sharedRegisterMap;
	private final SetMultimap<RTLVariable, RTLVariable> coveredRegs;
	private final SetMultimap<RTLVariable, RTLVariable> coveredBy;
	
	private ExpressionFactory() {
		variableInstances = new HashMap<String, RTLVariable>(DEFAULT_VARIABLE_COUNT);
		variableArray = new ArrayList<RTLVariable>(DEFAULT_VARIABLE_COUNT);
		uniqueVariableCount = 0;
		sharedRegisterMap = new HashMap<RTLVariable, RTLBitRange>();
		coveredRegs = HashMultimap.create();
		coveredBy = HashMultimap.create();
		nondetArray = new RTLNondet[128];

		// Initialize constants
		// Setting TRUE to -1 yields TRUE = ~FALSE, which makes life easier 
		TRUE = new RTLNumber(-1, 1);
		FALSE = new RTLNumber(0, 1);
		pc = createVariable("%pc", 32);
		SKIP = createVariable("%SKIP", 1);
		REPEAT = createVariable("%RPT", 1);
	}
	
	public RTLBitRange createBitRange(RTLExpression operand,
			RTLExpression firstBit, RTLExpression lastBit) {
		return new RTLBitRange(operand, firstBit, lastBit);
	}

	public RTLConditionalExpression createConditionalExpression(
			RTLExpression condition, RTLExpression trueExpression,
			RTLExpression falseExpression) {
		// Invert not-expressions
		if (condition instanceof RTLOperation && ((RTLOperation)condition).getOperator().equals(Operator.NOT)) {
			//logger.debug("Inverting negated conditional expression.");
			return createConditionalExpression(((RTLOperation)condition).getOperands()[0], 
					falseExpression,
					trueExpression);
		}
		return new RTLConditionalExpression(condition, trueExpression,
				falseExpression);
	}
	
	public RTLExpression createImplication(RTLExpression a, RTLExpression b) {
		return createOr(createNot(a), b);
	}

	public RTLMemoryLocation createMemoryLocation(RTLExpression address, int bitWidth) {
		return createMemoryLocation(0, null, address, bitWidth);
	}

	public RTLMemoryLocation createMemoryLocation(int memoryState, RTLExpression address, int bitWidth) {
		return createMemoryLocation(memoryState, null, address, bitWidth);
	}

	public RTLMemoryLocation createMemoryLocation(RTLExpression segmentRegister, RTLExpression address, int bitWidth) {
		return createMemoryLocation(0, segmentRegister, address, bitWidth);
	}

	public RTLMemoryLocation createMemoryLocation(int memoryState, RTLExpression segmentRegister, RTLExpression address, int bitWidth) {
		assert bitWidth > 0 : "Trying to create memory location of unknown width with address " + address + "!";
		return new RTLMemoryLocation(memoryState, segmentRegister, address, bitWidth);
	}

	public RTLNumber createNumber(Number value) {
		int bitWidth = 64;
		if (value instanceof Long) bitWidth = 64;
		else if (value instanceof Integer) bitWidth = 32;
		else if (value instanceof Short) bitWidth = 16;
		else if (value instanceof Byte) bitWidth = 8;
		return new RTLNumber(value.longValue(), bitWidth);
	}

	public RTLNumber createNumber(long value, int bitWidth) {
		if (bitWidth == 1) {
			if (value == 0) return FALSE;
			else return TRUE;
		}
		return new RTLNumber(value, bitWidth);
	}

	/**
	 * Generic creation method that calls more specific methods depending on the
	 * type of the assembly operand passed as paramater.
	 * 
	 * @param iOp an operand of an assembly instruction 
	 * @return a translation of the operand into an RTLExpression 
	 */
	public RTLExpression createOperand(Operand iOp) {
		RTLExpression opAsExpr = null;
		if (iOp instanceof Immediate) {
			opAsExpr = createNumber(((Immediate)iOp).getNumber());
		} else if (iOp instanceof Register) {
			opAsExpr = createRegister((Register)iOp);
		} else if (iOp instanceof MemoryOperand)  {
			opAsExpr = createMemoryLocation((MemoryOperand)iOp);
		} else if (iOp instanceof AbsoluteAddress) {
			opAsExpr = createAddress((AbsoluteAddress)iOp);
		} else if (iOp instanceof PCRelativeAddress) {
			opAsExpr = createAddress((PCRelativeAddress)iOp);
		} else {
			if (iOp == null) logger.warn("Null operand in RTL conversion!");
			else logger.warn("Unsupported operand type: " + iOp.getClass().getSimpleName());
		}
		return opAsExpr;
	}
	
	public RTLExpression createPlus(RTLExpression... operands) {
		return createOperation(Operator.PLUS, operands);
	}

	public RTLExpression createMinus(RTLExpression op1, RTLExpression op2) {
		return createPlus(op1, createNeg(op2));
	}

	public RTLExpression createMultiply(RTLExpression... operands) {
		return createOperation(Operator.MUL, operands);
	}

	public RTLExpression createFloatMultiply(RTLExpression... operands) {
		return createOperation(Operator.FMUL, operands);
	}

	public RTLExpression createFloatDivide(RTLExpression op1, RTLExpression op2) {
		return createOperation(Operator.FDIV, op1, op2);
	}

	public RTLExpression createDivide(RTLExpression op1, RTLExpression op2) {
		return createOperation(Operator.DIV, op1, op2);
	}

	public RTLExpression createPowerOf(RTLExpression op1, RTLExpression op2) {
		return createOperation(Operator.POWER_OF, op1, op2);
	}

	public RTLExpression createModulo(RTLExpression... operands) {
		return createOperation(Operator.MOD, operands);
	}
	
	public RTLExpression createEqual(RTLExpression op1, RTLExpression op2) {
		return createOperation(Operator.EQUAL, op1, op2);
	}

	public RTLExpression createNotEqual(RTLExpression op1, RTLExpression op2) {
		return createNot(createEqual(op1, op2));
	}

	public RTLExpression createLessThan(RTLExpression op1, RTLExpression op2) {
		return createOperation(Operator.LESS, op1, op2);
	}

	public RTLExpression createLessOrEqual(RTLExpression op1, RTLExpression op2) {
		return createOperation(Operator.LESS_OR_EQUAL, op1, op2);
	}

	public RTLExpression createGreaterThan(RTLExpression op1, RTLExpression op2) {
		return createLessThan(op2, op1);
	}

	public RTLExpression createGreaterOrEqual(RTLExpression op1, RTLExpression op2) {
		//return createLessOrEqual(op2, op1);
		return createNot(createLessThan(op1, op2));
	}

	public RTLExpression createUnsignedLessThan(RTLExpression op1, RTLExpression op2) {
		return createOperation(Operator.UNSIGNED_LESS, op1, op2);
	}

	public RTLExpression createUnsignedLessOrEqual(RTLExpression op1, RTLExpression op2) {
		return createOperation(Operator.UNSIGNED_LESS_OR_EQUAL, op1, op2);
	}

	public RTLExpression createUnsignedGreaterThan(RTLExpression op1, RTLExpression op2) {
		return createUnsignedLessThan(op2, op1);
	}

	public RTLExpression createUnsignedGreaterOrEqual(RTLExpression op1, RTLExpression op2) {
		return createNot(createUnsignedLessThan(op1, op2));
	}

	public RTLExpression createAnd(RTLExpression op1, RTLExpression op2) {
		return createOperation(Operator.AND, op1, op2);
	}

	public RTLExpression createOr(RTLExpression op1, RTLExpression op2) {
		return createOperation(Operator.OR, op1, op2);
	}

	public RTLExpression createXor(RTLExpression op1, RTLExpression op2) {
		return createOperation(Operator.XOR, op1, op2);
	}
	
	public RTLExpression createNot(RTLExpression op) {
		return createOperation(Operator.NOT, op);
	}

	public RTLExpression createNeg(RTLExpression op) {
		return createOperation(Operator.NEG, op);
	}

	public RTLExpression createShiftRight(RTLExpression op1, RTLExpression op2) {
		return createOperation(Operator.SHR, op1, op2);
	}
	
	public RTLExpression createShiftArithmeticRight(RTLExpression op1, RTLExpression op2) {
		return createOperation(Operator.SAR, op1, op2);
	}
	
	public RTLExpression createShiftLeft(RTLExpression op1, RTLExpression op2) {
		return createOperation(Operator.SHL, op1, op2);
	}
	
	public RTLExpression createRotateLeft(RTLExpression op1, RTLExpression op2) {
		return createOperation(Operator.ROL, op1, op2);
	}
	
	public RTLExpression createRotateRight(RTLExpression op1, RTLExpression op2) {
		return createOperation(Operator.ROR, op1, op2);
	}
	
	public RTLExpression createRotateLeftWithCarry(RTLExpression op1, RTLExpression op2) {
		return createOperation(Operator.ROLC, op1, op2);
	}
	
	public RTLExpression createRotateRightWithCarry(RTLExpression op1, RTLExpression op2) {
		return createOperation(Operator.RORC, op1, op2);
	}
	
	public RTLExpression createCast(RTLExpression op, RTLNumber bitWidth) {
		return createOperation(Operator.CAST, op, bitWidth);
	}
	
	public RTLExpression createSignExtend(int from, int to, RTLExpression op) {
		return createSignExtend(createNumber(from, 8), createNumber(to, 8), op);
	}

	public RTLExpression createSignExtend(RTLExpression from, RTLExpression to, RTLExpression op) {
		return createOperation(Operator.SIGN_EXTEND, from, to, op);
	}
	
	public RTLExpression createZeroFill(int from, int to, RTLExpression op) {
		return createZeroFill(createNumber(from, 8), createNumber(to, 8), op);
	}
	
	public RTLExpression createZeroFill(RTLExpression from, RTLExpression to, RTLExpression op) {
		return createOperation(Operator.ZERO_FILL, from, to, op);
	}
	
	public RTLExpression createFloatResize(RTLExpression toBits, RTLExpression fromBits, RTLExpression op) {
		//fromBits = createNumber(((RTLNumber)fromBits).intValue(), 8);
		//toBits = createNumber(((RTLNumber)toBits).intValue(), 8);
		return createOperation(Operator.FSIZE, toBits, fromBits, op);
	}
	
	public RTLExpression createOperation(Operator operator,
			RTLExpression... operands) {
		switch (operator) {
		// Handle nested operators for commutative associative operations
		case PLUS:
		case AND:
		case OR:
			for (int i=0; i<operands.length; i++) {
				// Combine associative operands of same type
				if (operands[i] instanceof RTLOperation) {
					RTLOperation subOp = ((RTLOperation)operands[i]);
					if (subOp.getOperator() == operator) {
						RTLExpression[] newOps = new RTLExpression[operands.length - 1 + subOp.getOperandCount()];
						System.arraycopy(operands, 0, newOps, 0, i);
						System.arraycopy(operands, i + 1, newOps, i, operands.length - i - 1);
						System.arraycopy(subOp.getOperands(), 0, newOps, operands.length - 1, subOp.getOperandCount());
						return createOperation(operator, newOps);
					}
				} 
			}
			break;
		// Cancel double negation/not
		case NOT:
		case NEG:
			if (operands[0] instanceof RTLOperation) {
				RTLOperation nestedOp = (RTLOperation)operands[0];
				if (nestedOp.getOperator().equals(operator)) {
					return nestedOp.getOperands()[0];
				}
				if (operator == Operator.NEG && 
						nestedOp.getOperator() == Operator.PLUS) {
					RTLExpression[] newNestedOperands = new RTLExpression[nestedOp.getOperandCount()];
					for (int i=0; i<nestedOp.getOperandCount(); i++) {
						newNestedOperands[i] = createNeg(nestedOp.getOperands()[i]);
					}
					return createOperation(nestedOp.getOperator(), newNestedOperands);
				}
			}
			break;
		}
		return new RTLOperation(operator, operands);
	}

	public RTLExpression createSpecialExpression(
			String operation, RTLExpression... operands) {
		// Load effective address (lea)
		if (operation.equals("addr")) {
			if (operands[0] instanceof RTLMemoryLocation) {
				return ((RTLMemoryLocation)operands[0]).getAddress();
			}
		}
		else if (operation.equals("nondet")) {
			return nondet(((RTLNumber)operands[0]).intValue());
		}

		return new RTLSpecialExpression(operation, operands);
	}

	public RTLVariable createVariable(String name, int bitWidth) {
		// Remove leading %-signs on registers to avoid confusion of users
		if (name.charAt(0) == '%') name = name.substring(1);
		
		RTLVariable var;
		if (variableInstances.containsKey(name)) {
			var = variableInstances.get(name);
			if (!(bitWidth == RTLVariable.UNKNOWN_BITWIDTH || var.getBitWidth() == bitWidth)) {
				if (!(name.startsWith("reg") || name.startsWith("modrm") || name.startsWith("i") || name.startsWith("sti"))) {
					logger.error(var + " exists with width " + var.getBitWidth() + "! Cannot make it width " + bitWidth + "!");
					assert(false);
				}
			}
		}
		else {
			var = new RTLVariable(uniqueVariableCount, name, bitWidth);
			variableArray.add(var);
			assert(variableArray.get(var.getIndex()) == var) : "Something's wrong with variable caching!";
			variableInstances.put(name, var);
			uniqueVariableCount++;
			assert uniqueVariableCount < DEFAULT_VARIABLE_COUNT : "Too many variables!";
		}
		
		return var;
	}
	
	private void addCoveringRegister(RTLVariable var, RTLVariable parent) {
		for (RTLVariable ancestor : coveringRegisters(parent)) {
			addCoveringRegister(var, ancestor);
		}
		coveredBy.put(var, parent);
	}
	
	private void addCoveredRegister(RTLVariable var, RTLVariable child) {
		for (RTLVariable ancestor : coveringRegisters(var)) {
			addCoveredRegister(ancestor, child);
		}
		coveredRegs.put(var, child);
	}
	
	public RTLVariable createSharedRegisterVariable(String name, String parentName, int startBit, int endBit) {
		RTLVariable var = createVariable(name, endBit - startBit + 1);
		RTLVariable parent = createVariable(parentName);

		RTLBitRange expr = createBitRange(parent, createNumber(startBit, 8), createNumber(endBit, 8));
		sharedRegisterMap.put(var, expr);
		
		addCoveringRegister(var, parent);
		addCoveredRegister(parent, var);

		return var;
	}
	
	public RTLBitRange getRegisterAsParent(RTLVariable var) {
		return sharedRegisterMap.get(var);
	}
	
	public Set<RTLVariable> coveredRegisters(RTLVariable var) {
		return coveredRegs.get(var);
	}
	
	public Set<RTLVariable> coveringRegisters(RTLVariable var) {
		return coveredBy.get(var);
	}

	public Writable createRegisterVariable(String name, int bitWidth) {
		// Use explicit 16 and 8 bit registers now
		//if (sharedRegisterMap.containsKey(name)) return sharedRegisterMap.get(name);
		return createVariable(name, bitWidth);
	}
	
	public RTLVariable createVariable(String name) {
		return createVariable(name, RTLVariable.UNKNOWN_BITWIDTH);
	}
	
	public RTLVariable getVariable(int index) {
		return variableArray.get(index);
	}

	public int getVariableCount() {
		return uniqueVariableCount;
	}

	/**
	 * Returns an expression representing a nondeterministic value of the 
	 * given bit width. In Yices translation, each occurrence of a nondeterministic 
	 * expression is converted to a fresh variable. 
	 * 
	 * @param bitWidth
	 * @return a nondeterministic RTLExpression. 
	 */
	public RTLExpression nondet(int bitWidth) {
		if (nondetArray[bitWidth - 1] == null)
			nondetArray[bitWidth - 1] = new RTLNondet(bitWidth);
		return nondetArray[bitWidth - 1];
	}

	private RTLExpression createAddress(AbsoluteAddress asmAddress) {
		RTLExpression addressExpression;
		addressExpression = 
			createNumber(asmAddress.getValue(), asmAddress.getBitWidth());
		return addressExpression;
	}

	private RTLExpression createAddress(PCRelativeAddress asmAddress) {
		RTLExpression addressExpression;
		addressExpression = createNumber(asmAddress.getDisplacement(), asmAddress.getBitWidth());
		return addressExpression;
	}

	private RTLMemoryLocation createMemoryLocation(MemoryOperand asmMemOp) {
		RTLExpression segmentRegister = null;
		if (asmMemOp instanceof X86MemoryOperand) {
			X86SegmentRegister segReg = ((X86MemoryOperand)asmMemOp).getSegmentRegister();
			if (segReg != null) segmentRegister = createRegister(segReg);
		}

		RTLExpression addressExpr = null;
		if (asmMemOp.getBase() != null) 
			addressExpr = createRegister(asmMemOp.getBase());
		if (asmMemOp.getIndex() != null) {
			RTLExpression indexScale = createRegister(asmMemOp.getIndex());
			if (asmMemOp.getScale() > 1) {
				indexScale = 
					createBitRange(
							createOperation(Operator.MUL, indexScale, 
									createNumber(asmMemOp.getScale(), 32)
							),
							createNumber(0,8), 
							createNumber(31,8));
			}
			if (addressExpr == null) addressExpr = indexScale;
			else addressExpr = createOperation(Operator.PLUS, 
					addressExpr, indexScale);
		}
		if (asmMemOp.getDisplacement() != 0 || addressExpr == null) {
			RTLExpression disp = 
				createNumber(asmMemOp.getDisplacement(), 32);
			if (addressExpr == null) addressExpr = disp;
			else addressExpr = createOperation(Operator.PLUS, 
					addressExpr, disp);
		}
		assert (addressExpr != null) : "Address expression is null!";
		int bitWidth = asmMemOp.getDataType().bits();
		return createMemoryLocation(segmentRegister, addressExpr, bitWidth);
	}

	private RTLExpression createRegister(Register asmRegister) {
		return createRegisterVariable(asmRegister.toString(), RTLVariable.UNKNOWN_BITWIDTH);
	}
}

/*
 * CallStackState.java - This file is part of the Jakstab project.
 * Copyright 2009-2011 Johannes Kinder <kinder@cs.tu-darmstadt.de>
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
package org.jakstab.analysis.callstack;

import java.util.*;

import org.jakstab.Program;
import org.jakstab.analysis.*;
import org.jakstab.asm.*;
import org.jakstab.cfa.Location;
import org.jakstab.cfa.StateTransformer;
import org.jakstab.rtl.RTLLabel;
import org.jakstab.rtl.expressions.*;
import org.jakstab.rtl.statements.*;
import org.jakstab.util.Logger;
import org.jakstab.util.Tuple;

/**
 * @author Johannes Kinder
 */
public class CallStackState implements AbstractState {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CallStackState.class);
	
	public static CallStackState TOP = new CallStackState(null);
	public static CallStackState BOT = new CallStackState(null);
	
	private final Deque<Location> callStack;
	private static final RTLVariable retVar = Program.getProgram().getArchitecture().returnAddressVariable();
	
	public CallStackState() {
		this(new LinkedList<Location>());
	}
	
	public CallStackState(Deque<Location> callStack) {
		this.callStack = callStack;
	}

	public AbstractState abstractPost(StateTransformer transformer,
			Precision precision) {
		if (isBot()) return BOT;
		if (isTop()) return TOP;
		
		final RTLStatement statement = (RTLStatement)transformer;
		return statement.accept(new DefaultStatementVisitor<CallStackState>() {

			@Override
			public CallStackState visit(RTLVariableAssignment stmt) {
				return CallStackState.this;
			}

			@Override
			public CallStackState visit(RTLMemoryAssignment stmt) {
				return CallStackState.this;
			}

			@Override
			public CallStackState visit(RTLAssume stmt) {

				Instruction instr = Program.getProgram().getAssemblyMap().get(stmt.getAddress());
				Deque<Location> postStack;
				RTLGoto gotoStmt = stmt.getSource();

				long addressValue = stmt.getAddress().getValue(); 

				// in the stub area there are only returns (but no real asm instructions)
				// in the prologue there is only a single call
				// Return
				if (gotoStmt.getType() == RTLGoto.Type.RETURN) {
					postStack = new LinkedList<Location>(callStack);
					if (postStack.isEmpty()) {
						logger.warn("Return instruction on empty call stack!");
					} else {
						Location target = postStack.pop();
						logger.debug("Call stack: Return to " + target + ". Remaining stack " + postStack);
					}
				} 
				// Prologue Call
				else if (Program.getProgram().getHarness().contains(stmt.getAddress())) {
					postStack = new LinkedList<Location>(callStack);
					postStack.push(new RTLLabel(Program.getProgram().getHarness().getFallthroughAddress(stmt.getAddress())));
				}
				// Call
				else if (gotoStmt.getType() == RTLGoto.Type.CALL) {
					RTLLabel returnLabel; 
					if (instr == null) {
						// Happens in import stubs containing a call
						logger.info("No instruction at address " + stmt.getLabel());
						returnLabel = gotoStmt.getNextLabel();
					} else {
						returnLabel = new RTLLabel(new AbsoluteAddress(addressValue + instr.getSize()));
					}

					postStack = new LinkedList<Location>();
					for (Iterator<Location> iter = callStack.descendingIterator(); iter.hasNext();) {
						Location exRetLoc = iter.next();
						if (exRetLoc.equals(returnLabel)) {
							logger.verbose("Recursion detected in call at " + stmt.getAddress());
							break;
						} else {
							postStack.push(exRetLoc);
						}
					}

					logger.debug("Call stack: Pushing " + returnLabel);
					postStack.push(returnLabel);
				} 
				// Something else
				else {
					return CallStackState.this;
				}
				return new CallStackState(postStack);
			}

			@Override
			public CallStackState visit(RTLSkip stmt) {
				return CallStackState.this;
			}
			
			@Override
			public CallStackState visit(RTLAssert stmt) {
				return CallStackState.this;
			}

			@Override
			public CallStackState visit(RTLAlloc stmt) {
				return CallStackState.this;
			}
			
			@Override
			public CallStackState visit(RTLHavoc stmt) {
				return CallStackState.this;
			}

			@Override
			public CallStackState visit(RTLDealloc stmt) {
				return CallStackState.this;
			}

			@Override
			public CallStackState visit(RTLUnknownProcedureCall stmt) {
				return CallStackState.this;
			}
		});		
	}

	/*
	 * @see org.jakstab.analysis.AbstractState#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return Integer.toString(hashCode());
	}

	/*
	 * @see org.jakstab.analysis.AbstractState#getLocation()
	 */
	@Override
	public Location getLocation() {
		throw new UnsupportedOperationException(this.getClass().getSimpleName() + " does not contain location information.");
	}

	/*
	 * @see org.jakstab.analysis.AbstractState#join(org.jakstab.analysis.LatticeElement)
	 */
	@Override
	public AbstractState join(LatticeElement l) {
		CallStackState other = (CallStackState)l;
		if (this.isBot()) return other;
		if (other.isBot() || this.equals(other)) return this;
		return TOP;
	}

	/*
	 * @see org.jakstab.analysis.AbstractState#projectionFromConcretization(org.jakstab.rtl.expressions.RTLExpression[])
	 */
	@Override
	public Set<Tuple<RTLNumber>> projectionFromConcretization(
			RTLExpression... expressions) {
		ExpressionFactory factory = ExpressionFactory.getInstance();
		if (!isBot() && !isTop() && expressions.length == 2 && 
 				expressions[0].equals(ExpressionFactory.getInstance().TRUE) && 
				expressions[1].equals(retVar)) {
			logger.debug("Concretizing callstack element: " + callStack.peek());
			return Collections.singleton(Tuple.create(
					factory.TRUE,
					factory.createNumber(((RTLLabel)(callStack.peek())).getAddress().getValue(), 32))
					); 
		} else {
			Tuple<RTLNumber> result = new Tuple<RTLNumber>(expressions.length);
			for (int i=0; i<expressions.length; i++) {
				result.set(i, null);
			}
			return Collections.singleton(result);
		}
	}

	/*
	 * @see org.jakstab.analysis.LatticeElement#isBot()
	 */
	@Override
	public boolean isBot() {
		return this == BOT;
	}

	/*
	 * @see org.jakstab.analysis.LatticeElement#isTop()
	 */
	@Override
	public boolean isTop() {
		return this == TOP;
	}

	/*
	 * @see org.jakstab.analysis.LatticeElement#lessOrEqual(org.jakstab.analysis.LatticeElement)
	 */
	@Override
	public boolean lessOrEqual(LatticeElement l) {
		CallStackState other = (CallStackState)l;
		if (this.isBot() || other.isTop()) return true;
		if (other.equals(this)) return true;
		else return false;
	}
	
	@Override
	public String toString() {
		return "Call stack: " + callStack;
	}

	@Override
	public int hashCode() {
		if (isTop()) return 842192;
		if (isBot()) return 189487;
		return callStack.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		CallStackState other = (CallStackState)obj;
		if (callStack == null) {
			// callStack is only null for singletons TOP and BOT  
			assert isTop() || isBot();
			return false;
		} 
		return callStack.equals(other.callStack);
	}

}

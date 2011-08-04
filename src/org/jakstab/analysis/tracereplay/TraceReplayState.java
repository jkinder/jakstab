/*
 * TraceReplayState.java - This file is part of the Jakstab project.
 * Copyright 2011 Johannes Kinder <kinder@cs.tu-darmstadt.de>
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
package org.jakstab.analysis.tracereplay;

import java.util.Collections;
import java.util.Set;

import org.jakstab.analysis.AbstractState;
import org.jakstab.analysis.LatticeElement;
import org.jakstab.analysis.UnderApproximateState;
import org.jakstab.asm.AbsoluteAddress;
import org.jakstab.cfa.Location;
import org.jakstab.rtl.expressions.ExpressionFactory;
import org.jakstab.rtl.expressions.RTLExpression;
import org.jakstab.rtl.expressions.RTLNumber;
import org.jakstab.util.Logger;
import org.jakstab.util.Tuple;

/**
 * States corresponding to a line number within a pre-recorded trace. Each state
 * stores a reference to the trace to allow direct access to recorded PC values.
 */
public class TraceReplayState implements UnderApproximateState {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TraceReplayState.class);

	public static TraceReplayState BOT = new TraceReplayState();
	
	private final int lineNumber;
	private final AbsoluteAddress[] trace;
	
	public TraceReplayState() {
		super();
		lineNumber = -1;
		trace = null;
	}
	
	public TraceReplayState(AbsoluteAddress[] trace, int lineNumber) {
		this.trace = trace;
		this.lineNumber = lineNumber;
	}
	
	@Override
	public String getIdentifier() {
		return Integer.toString(lineNumber);
	}

	@Override
	public Location getLocation() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Get the value of the program counter at the current state. 
	 * 
	 * @return an AbsoluteAddress corresponding to the PC value for the analyzed module. 
	 */
	public AbsoluteAddress getCurrentPC() {
		if (lineNumber < 0) {
			// BOT
			return new AbsoluteAddress(0xF0000B07L);
		} else {
			return trace[lineNumber];
		}
	}

	/**
	 * Get the value of the program counter at the current state's successor state. 
	 * 
	 * @return an AbsoluteAddress corresponding to the next PC value for the analyzed module. 
	 */
	public AbsoluteAddress getNextPC() {
		if (lineNumber < 0) {
			// BOT
			return new AbsoluteAddress(0xF0100B07L);
		} else {
			return trace[lineNumber+1];
		}
	}
	
	int getLineNumber() {
		return lineNumber;
	}

	@Override
	public AbstractState join(LatticeElement l) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Set<Tuple<RTLNumber>> projectionFromConcretization(RTLExpression... expressions) {
		ExpressionFactory factory = ExpressionFactory.getInstance();

		// Only concretize expression requests from transformerFactory
		// Warning: If this method is invoked with 2 parameters for other reasons, it will 
		//          likely fail!
		if (expressions.length != 2) return null;
		
		// If not on trace, don't concretize
		if (isBot()) return null;

		RTLExpression condition = expressions[0];
		RTLExpression target = expressions[1];
		RTLNumber cCondition;
		RTLNumber cTarget;
		
		RTLNumber nextPC = getNextPC().toNumericConstant();

		if (target instanceof RTLNumber) {
			// If target is a number, this is a direct jump, and maybe conditional

			cTarget = (RTLNumber)target;

			if (condition instanceof RTLNumber) {
				// Direct, unconditional jump
				cCondition = (RTLNumber)condition;
			} else if (target.equals(nextPC)) {
				// Conditional jump that is taken according to the trace
				cCondition = factory.TRUE;
			} else { 
				// Conditional jump that is not taken
				cCondition = factory.FALSE;
			}

		} else {
			// Target is not a number, so this is an indirect jump

			assert (condition instanceof RTLNumber) : "There should be no conditional indirect jumps in x86!";
			cCondition = (RTLNumber)condition;
			cTarget = nextPC;
		}
		return Collections.singleton(Tuple.create(cCondition, cTarget));
	}

	@Override
	public boolean isBot() {
		return this == BOT;
	}

	@Override
	public boolean isTop() {
		return false;
	}

	@Override
	public int hashCode() {
		return lineNumber;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		
		return lineNumber == ((TraceReplayState)obj).lineNumber;
	}

	@Override
	public boolean lessOrEqual(LatticeElement l) {
		TraceReplayState other = (TraceReplayState)l;
		if (other.isTop() || this.isBot()) return true;
		return this.equals(l); 
	}
	
	public String toString() {
		return "Trace@" + getCurrentPC() + ": Next: " + getNextPC() + " Line: " + lineNumber;
	}
}
	
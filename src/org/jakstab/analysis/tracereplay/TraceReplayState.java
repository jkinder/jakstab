package org.jakstab.analysis.tracereplay;

import java.util.Collections;
import java.util.Set;

import org.jakstab.Program;
import org.jakstab.analysis.AbstractState;
import org.jakstab.analysis.LatticeElement;
import org.jakstab.analysis.UnderApproximateState;
import org.jakstab.asm.AbsoluteAddress;
import org.jakstab.cfa.Location;
import org.jakstab.rtl.expressions.ExpressionFactory;
import org.jakstab.rtl.expressions.RTLExpression;
import org.jakstab.rtl.expressions.RTLMemoryLocation;
import org.jakstab.rtl.expressions.RTLNumber;
import org.jakstab.rtl.expressions.RTLVariable;
import org.jakstab.util.Logger;
import org.jakstab.util.Tuple;

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
	
	public AbsoluteAddress getCurrentPC() {
		if (lineNumber < 0)
			return new AbsoluteAddress(0xF0000B07L);
		else 
			return trace[lineNumber];
	}

	public AbsoluteAddress getNextPC() {
		if (lineNumber < 0)
			return new AbsoluteAddress(0xF0100B07L);
		else 
			return trace[lineNumber+1];
	}
	
	int getLineNumber() {
		return lineNumber;
	}

	@Override
	public AbstractState join(LatticeElement l) {
		throw new UnsupportedOperationException();
	}
	
	private static boolean isProgramAddress(RTLNumber n) {
		return Program.getProgram().getModule(new AbsoluteAddress(n.longValue())) != null;
	}

	@Override
	public Set<Tuple<RTLNumber>> projectionFromConcretization(RTLExpression... expressions) {
		ExpressionFactory factory = ExpressionFactory.getInstance();

		// Only concretize expression requests from transformerFactory
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

			if (target instanceof RTLMemoryLocation) {
				// Target address is read from memory
				if (isProgramAddress(nextPC)) {
					// Points to program, e.g., jump table
					cTarget = nextPC;
				} else {
					// Points outside, i.e., to an imported function
					// Set target to null, so that static analysis can provide correct stub address
					cTarget = null;
				}
			} else if (target instanceof RTLVariable) {
				// Target address is a variable, i.e., a register or the special retval variable
				if (isProgramAddress(nextPC)) {
					// Points to program
					cTarget = nextPC;
				} else {
					// Points out of program, make this also null. This could point to the epilogue, 
					// but also to library functions (because of storing import addresses in a register
					// or because of a return into a library function from a callback) 
					cTarget = null;
					//cTarget = factory.createNumber(DefaultHarness.EPILOGUE_BASE, 32);
				}
			} else {
				logger.error("Unhandled target type for indirect jump to " + nextPC.toHexString() + ": " + target.getClass().getSimpleName());
				cTarget = null;
			}
		}
		return Collections.singleton(Tuple.create(cCondition, cTarget));
	}

	@Override
	public boolean isBot() {
		if (this == BOT) return true;
		else return false;
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

		TraceReplayState other = (TraceReplayState) obj;
		if (lineNumber != other.lineNumber)
			return false;
		return true;
	}

	@Override
	public boolean lessOrEqual(LatticeElement l) {
		TraceReplayState other = (TraceReplayState)l;
		if (other.isTop() || this.isBot()) return true;
		return (this.equals(l)); 
	}
	
	public String toString() {
		return "Trace@" + getCurrentPC() + ": Next: " + getNextPC() + " Line: " + lineNumber;
	}
}
	
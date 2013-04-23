package org.jakstab.rtl.statements;

import java.util.Collections;
import java.util.Set;

import org.jakstab.rtl.Context;
import org.jakstab.rtl.expressions.RTLMemoryLocation;
import org.jakstab.rtl.expressions.SetOfVariables;

public class RTLCallReturn extends AbstractRTLStatement {

	@Override
	public <T> T accept(StatementVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public RTLStatement evaluate(Context context) {
		return this;
	}

	@Override
	protected SetOfVariables initDefinedVariables() {
		return SetOfVariables.EMPTY_SET;
	}

	@Override
	protected SetOfVariables initUsedVariables() {
		return SetOfVariables.EMPTY_SET;
	}

	@Override
	protected Set<RTLMemoryLocation> initUsedMemoryLocations() {
		return Collections.emptySet();
	}

}

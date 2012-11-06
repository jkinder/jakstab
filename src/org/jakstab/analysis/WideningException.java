package org.jakstab.analysis;

import org.jakstab.rtl.expressions.RTLExpression;

public class WideningException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private final RTLExpression widenedLocation;
	private final AbstractState state;

	public WideningException(AbstractState s, RTLExpression widenedLocation) {
		super();
		this.widenedLocation = widenedLocation;
		this.state = s;
	}

	public RTLExpression getWidenedExpression() {
		return widenedLocation;
	}

	public AbstractState getState() {
		return state;
	}
	
}

package org.jakstab.analysis;


public class WideningException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private final ValueContainer widenedLocation;
	private final AbstractState state;

	public WideningException(AbstractState s, ValueContainer widenedLocation) {
		super();
		this.widenedLocation = widenedLocation;
		this.state = s;
	}

	public ValueContainer getWidenedExpression() {
		return widenedLocation;
	}

	public AbstractState getState() {
		return state;
	}
	
}

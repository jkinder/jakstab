package org.jakstab.analysis.tracereplay;

import java.util.Collections;
import java.util.Set;

import org.jakstab.Program;
import org.jakstab.analysis.AbstractState;
import org.jakstab.analysis.LatticeElement;
import org.jakstab.asm.AbsoluteAddress;
import org.jakstab.cfa.Location;
import org.jakstab.rtl.RTLLabel;
import org.jakstab.rtl.expressions.ExpressionFactory;
import org.jakstab.rtl.expressions.RTLExpression;
import org.jakstab.rtl.expressions.RTLMemoryLocation;
import org.jakstab.rtl.expressions.RTLNumber;
import org.jakstab.rtl.expressions.RTLVariable;
import org.jakstab.util.Logger;
import org.jakstab.util.Tuple;

//import org.jakstab.asm.AbsoluteAddress;

public class TraceReplayState implements AbstractState {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(TraceReplayState.class);

	public static TraceReplayState TOP = new TraceReplayState();
	public static TraceReplayState BOT = new TraceReplayState();
	private RTLNumber pcValue;
	private RTLLabel pcLabel;
	//private int index;
	
	//static private int count = 0;
	//static public int mod3 = 0;
	
	
	int lineNumber;
	//static int counter = 0;
	

	public TraceReplayState() {
		super ();
		ExpressionFactory factory = ExpressionFactory.getInstance();
		pcValue = factory.createNumber(0, 32);
		pcLabel = new RTLLabel(new AbsoluteAddress(pcValue), 0);
		//this.index = 0;
		lineNumber = 0;
		//System.out.println(toString1());
		logger.debug(toString1());
	}
	
	public TraceReplayState(RTLLabel label, int lineNumber) {
		
		ExpressionFactory factory = ExpressionFactory.getInstance();
			
		if (label == null){
			pcValue = factory.createNumber(0,32);
			
		}
		else
			pcValue = factory.createNumber(label.getAddress().getValue(), 32);
		pcLabel = new RTLLabel(new AbsoluteAddress(pcValue));
		//mod3++;
		//this.index = pcLabel.getIndex();
		
		this.lineNumber = lineNumber;
		//System.out.println(toString1());
		logger.debug(toString1());
	}
	
	public TraceReplayState(long address, int lineNumber, Location location) {
		ExpressionFactory factory = ExpressionFactory.getInstance();
		pcValue = factory.createNumber(address, 32);
		//this.index = index;
		//pcLabel = new RTLLabel(new AbsoluteAddress(address), this.index);
		pcLabel = (RTLLabel)location;

		this.lineNumber = lineNumber;
		//System.out.println(toString1());
		logger.debug(toString1());
		
		
	}
	
	@Override
	public String getIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getLineNumber () {
		return lineNumber;
	}
	
	@Override
	public Location getLocation() {
		return pcLabel;
		// TODO Auto-generated method stub
		//return null;
	}
	
	//public LocationState getLocationState(){
		//return pcLocation;
	//}
	
	public RTLNumber getRTLNumber () {
		return pcValue;
	}
	
	
	public long getpcCounter (){
		return pcValue.longValue();
	}
	@Override
	public AbstractState join(LatticeElement l) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Tuple<RTLNumber>> projectionFromConcretization(
			RTLExpression... expressions) {
		
		ExpressionFactory factory = ExpressionFactory.getInstance();
		
		if (expressions[expressions.length-1] instanceof RTLNumber)
			
			if (((RTLNumber)expressions[expressions.length-1]).longValue() == getpcCounter() || 
					Program.getProgram().getModule(new AbsoluteAddress (pcValue.longValue())) == null)
				return Collections.singleton(Tuple.create(
						factory.TRUE,
						(RTLNumber)expressions[expressions.length-1]));
			else 
				return Collections.singleton(Tuple.create(
					factory.FALSE,
					(RTLNumber)expressions[expressions.length-1])); //pcValue));
					//pcValue));
		
		else {
			if (expressions[expressions.length-1] instanceof RTLMemoryLocation)
				if (Program.getProgram().getModule(new AbsoluteAddress(pcValue.longValue())) == null)
					return Collections.singleton(Tuple.create(factory.TRUE, null));
				//else
					//return null;
			if (expressions[expressions.length-1] instanceof RTLVariable)
				if (((RTLNumber)expressions[0]).longValue()== -1)
					if (Program.getProgram().getModule(new AbsoluteAddress (pcValue.longValue())) == null)
						return Collections.singleton(Tuple.create(factory.TRUE, factory.createNumber(-18415616, 32)));
					else
						return Collections.singleton(Tuple.create(factory.TRUE, pcValue));
		}
		//This is a kind of stub, because if-else statements above doesn't cover all cases 
		return Collections.singleton(Tuple.create(factory.TRUE,null));
	}

	@Override
	public boolean isBot() {
		if (this == BOT)
			return true;
		else
			return false;
	}

	@Override
	public boolean isTop() {
		if (this == TOP)
			return true;
		else
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
		if (!(obj instanceof TraceReplayState)) return false;
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
		//return pcLabel.toString();
		return pcValue.toHexString() + ' ' + pcValue.toString() + ' ' + lineNumber + ' ' + pcLabel.toString();
		//return  pcValue.toString() +' ' + lineNumber;
	}
	
	public String toString1() {
		//return pcLabel.toString();
		return pcValue.toHexString() + ' ' + pcValue.toString() + ' ' + lineNumber + ' ' + pcLabel.toString();
	}
}

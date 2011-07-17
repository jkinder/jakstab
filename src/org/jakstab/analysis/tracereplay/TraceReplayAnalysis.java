package org.jakstab.analysis.tracereplay;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.StringTokenizer;

import org.jakstab.Program;
import org.jakstab.analysis.AbstractState;
import org.jakstab.analysis.CPAOperators;
import org.jakstab.analysis.ConfigurableProgramAnalysis;
import org.jakstab.analysis.Precision;
import org.jakstab.analysis.ReachedSet;
import org.jakstab.asm.AbsoluteAddress;
import org.jakstab.cfa.CFAEdge;
import org.jakstab.cfa.Location;
import org.jakstab.cfa.StateTransformer;
import org.jakstab.rtl.RTLLabel;
import org.jakstab.util.Logger;
import org.jakstab.util.Pair;

public class TraceReplayAnalysis implements ConfigurableProgramAnalysis {

	private int lineNumber = 0;
	private long precedingAddress = -1;
	private long newAddress = -1;

	private FileReader temuTrace;
	private String entireLineInTrace;
	private BufferedReader in;
	private File fin;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger
	.getLogger(TraceReplayAnalysis.class);


	public TraceReplayAnalysis(String filename) {
		try {
			fin = new File(filename).getAbsoluteFile(); //CanonicalFile();
			temuTrace = new FileReader (fin);
			in = new BufferedReader (temuTrace);
			entireLineInTrace = in.readLine();
		} catch (FileNotFoundException e) {
			logger.fatal("Trace file not found: " + e.getMessage());
			throw new RuntimeException(e);
		} catch (IOException e) {
			logger.fatal("IOException while parsing trace! ", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public Precision initPrecision(Location location,
			StateTransformer transformer) {
		// TODO Auto-generated method stub
		return null;
	}

	public AbstractState initStartState(Location label){
		//linNumberis equals here to '0' and '0' is used because we have just started observing Trace/ParsedFile 
		return new TraceReplayState(((RTLLabel)label).getAddress().getValue(), lineNumber, label); 
	}

	@Override
	public AbstractState merge(AbstractState s1, AbstractState s2,
			Precision precision) {

		if (s2.isBot()) return s1;
		else return s2;
	}

	@Override
	public Set<AbstractState> post(AbstractState state, CFAEdge cfaEdge,
			Precision precision){
		StringTokenizer st;

		if (state.isBot()) 
			return Collections.singleton((AbstractState)TraceReplayState.BOT);

		long l = ((RTLLabel)cfaEdge.getTarget()).getAddress().getValue();

		try {
			if (Program.getProgram().getModule(((RTLLabel)cfaEdge.getTarget()).getAddress()) == null) {
				if (lineNumber == 0) //We just start to parse file (prolog-states)

					return Collections.singleton ((AbstractState)new TraceReplayState(l, lineNumber, cfaEdge.getTarget())); //???index = 0
				else
					if (Program.getProgram().getModule(new AbsoluteAddress(newAddress)) != null)	//???we consider now sys-call, but next state is the state from observed program
						if ( ((TraceReplayState)state).getpcCounter() == 0)
							//if ( new RTLLabel(new  AbsoluteAddress(precedingAddress)) != (cfaEdge.getTarget()))
							//return Collections.singleton((AbstractState)new TraceReplayState(null, 0));
							return Collections.singleton((AbstractState)TraceReplayState.BOT);
						else
							return Collections.singleton ((AbstractState)new TraceReplayState(newAddress, lineNumber, cfaEdge.getTarget())); //index = 0;
					else {

						if ( ((TraceReplayState)state).getLineNumber() == 0 )
							//if ( ((TraceReplayState)state).getpcCounter() == 0 )
							//return Collections.singleton((AbstractState)new TraceReplayState(null, 0));
							return Collections.singleton((AbstractState)TraceReplayState.BOT);

						entireLineInTrace = in.readLine();
						while (entireLineInTrace != null) {
							st = new StringTokenizer (entireLineInTrace, "\t ");
							st.nextToken();
							l = Long.parseLong(st.nextToken(), 16);
							lineNumber++;
							if (Program.getProgram().getModule(new AbsoluteAddress(l)) == null){ //We are parsing external function and looking for it's end 
								//precedingAddress = newAddress;
								entireLineInTrace = in.readLine();
							}
							else { //We have reached the end of external function and next line belongs to source code
								//System.out.println(lineNumber);
								//precedingAddress = newAddress;
								newAddress = l;
								return Collections.singleton ((AbstractState)new TraceReplayState(newAddress, lineNumber, cfaEdge.getTarget())); //index = 0
							}
						}
						return Collections.singleton ((AbstractState)new TraceReplayState(0, lineNumber, cfaEdge.getTarget())); //index  = 0 We have reached the end of file, but didn't encounter to source code of executable
					}
			}
			else 
				if (precedingAddress == l) //When we observe the same state in file, but for Jakstab it is new state - f.e. 0x40001000_0 and 0x40001000_1 
					return Collections.singleton ((AbstractState)new TraceReplayState(newAddress, lineNumber, cfaEdge.getTarget())); // ++index
				else {

					if (Program.getProgram().getModule(((RTLLabel)cfaEdge.getSource()).getAddress()) != null) { 
						//&&(((RTLLabel)cfaEdge.getSource()).getAddress().getValue() 
						//!= ((RTLLabel)cfaEdge.getTarget()).getAddress().getValue()) 
						if (((RTLLabel)cfaEdge.getSource()).getAddress().getValue() != precedingAddress) 
							//return  Collections.singleton((AbstractState)new TraceReplayState(null, 0)); //index = 0
							return Collections.singleton((AbstractState)TraceReplayState.BOT);
						else if (((RTLLabel)cfaEdge.getTarget()).getAddress().getValue() != newAddress) 
							//return  Collections.singleton((AbstractState)new TraceReplayState(null, 0)); //index = 0
							return Collections.singleton((AbstractState)TraceReplayState.BOT);
						//return Collections.emptySet();
						else if (((TraceReplayState)state).getpcCounter() == 0)
							//return  Collections.singleton((AbstractState)new TraceReplayState(null, 0));
							return Collections.singleton((AbstractState)TraceReplayState.BOT);
					}

					if (Program.getProgram().getModule(((RTLLabel)cfaEdge.getTarget()).getAddress()) != null)
						if (lineNumber != 0)
							if (((RTLLabel)cfaEdge.getTarget()).getAddress().getValue() != newAddress) 
								//return  Collections.singleton((AbstractState)new TraceReplayState(null, 0));
								return Collections.singleton((AbstractState)TraceReplayState.BOT);

					if (lineNumber == 0)
						lineNumber++;
					while (entireLineInTrace != null) {
						st = new StringTokenizer (entireLineInTrace, "\t ");
						st.nextToken();
						if (Long.parseLong(st.nextToken(), 16) == l){
							entireLineInTrace = in.readLine();
							lineNumber++;
							precedingAddress = l;
							if (entireLineInTrace != null){
								st = new StringTokenizer (entireLineInTrace, "\t ");
								st.nextToken();
								newAddress = Long.parseLong(st.nextToken(), 16);
								return Collections.singleton ((AbstractState)new TraceReplayState(newAddress, lineNumber, cfaEdge.getTarget())); //index = 0, We return next line
							}
							else {
								//Input file is finished.Something special should be done
								newAddress = 0;
								//return Collections.singleton ((AbstractState)new TraceReplayState(null, 0));
								return Collections.singleton((AbstractState)TraceReplayState.BOT);
							} //index = 0
							//temuTrace.close();
						}
						entireLineInTrace = in.readLine();
						lineNumber++;
					}
					//return Collections.singleton ((AbstractState)new TraceReplayState(null, 0));	//index = 0
					return Collections.singleton((AbstractState)TraceReplayState.BOT);
					//TODO Actually here a kind of exception should be invoked, because it is not standard case; here return-statement should be specified
				}
		} catch (IOException e){
			logger.fatal("IOException while parsing executable!", e);		
			//return Collections.singleton ((AbstractState)new TraceReplayState(null, 0));
			return Collections.singleton((AbstractState)TraceReplayState.BOT);
		}
	}

	@Override
	public Pair<AbstractState, Precision> prec(AbstractState s,
			Precision precision, ReachedSet reached) {
		return Pair.create(s, precision);
	}

	@Override
	public boolean stop(AbstractState s, ReachedSet reached, Precision precision) {

		if ( ((TraceReplayState)s).isBot() )	//This conditional is used to patch fault with empty ReachedSedUnderApp. 
			//In new version of Jakstab only one ReachedSet should exist!
			return true;
		else
			return CPAOperators.stopSep(s, reached, precision);
	}

	@Override
	public AbstractState strengthen(AbstractState s,
			Iterable<AbstractState> otherStates, CFAEdge cfaEdge,
			Precision precision) {
		// TODO Auto-generated method stub
		return null;
	}

}
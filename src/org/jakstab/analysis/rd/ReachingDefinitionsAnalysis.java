package org.jakstab.analysis.rd;

import java.util.Collections;
import java.util.Set;

import org.jakstab.analysis.*;
import org.jakstab.cfa.CFAEdge;
import org.jakstab.cfa.Location;
import org.jakstab.cfa.StateTransformer;
import org.jakstab.rtl.statements.*;
import org.jakstab.util.Logger;
import org.jakstab.util.Pair;

/**
 * Demo analysis for classroom use.
 * 
 * Template for a reaching definitions analysis based on the CPA framework. All methods that need to
 * be implemented are marked with TODO.  
 */
public class ReachingDefinitionsAnalysis implements ConfigurableProgramAnalysis {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ReachingDefinitionsAnalysis.class);

	@Override
	public AbstractState initStartState(Location label) {
		// TODO Return the initial state for a program entry location "label" here.
		return new RDState(); // Dummy
	}

	@Override
	public AbstractState merge(AbstractState s1, AbstractState s2,
			Precision precision) {
		// TODO Implement the merge operator
		return s2; // Dummy
	}

	@Override
	public Set<AbstractState> post(AbstractState state, CFAEdge edge,
			Precision precision) {
		// TODO Implement the abstract transfer relation. You can ignore the precision
		// parameter for now
		
		// Read statement from control flow edge
		RTLStatement statement = (RTLStatement)edge.getTransformer();
		// Cast the current abstract state to it's type
		final RDState curState = (RDState)state;
		
		// Calculate the set of abstract successors using a visitor pattern
		// The DefaultStatementVisitor throws exceptions on every not overridden visit method
		Set<AbstractState> abstractSuccessors = statement.accept(new DefaultStatementVisitor<Set<AbstractState>>() {

			@Override
			public Set<AbstractState> visit(RTLVariableAssignment stmt) {
				// TODO Implement transfer function for assignments
				logger.info("Processing assignment at " + stmt.getLabel() + ": " + stmt.toString());
				return Collections.singleton((AbstractState)curState); // Dummy
			}

			@Override
			public Set<AbstractState> visit(RTLMemoryAssignment stmt) {
				// We don't treat memory assignments here, so just return the same state
				return Collections.singleton((AbstractState)curState);
			}

			@Override
			public Set<AbstractState> visit(RTLAssume stmt) {
				// TODO Implement transfer function for assume
				logger.info("Processing assume at " + stmt.getLabel() + ": " + stmt.toString());
				return Collections.singleton((AbstractState)curState); // Dummy
			}

			@Override
			public Set<AbstractState> visit(RTLSkip stmt) {
				// TODO Implement transfer function for skip
				logger.info("Processing skip at " + stmt.getLabel() + ": " + stmt.toString());
				return Collections.singleton((AbstractState)curState); // Dummy
			}

		});
		
		return abstractSuccessors;
	}

	@Override
	public AbstractState strengthen(AbstractState s, Iterable<AbstractState> otherStates,
			CFAEdge cfaEdge, Precision precision) {
		// No strengthening for this analysis
		return s;
	}

	@Override
	public boolean stop(AbstractState s, ReachedSet reached, Precision precision) {
		// TODO Implement stop operator
		return true; // Dummy
	}

	@Override
	public Precision initPrecision(Location location, StateTransformer transformer) {
		// No precision information for this analysis
		return null;
	}

	@Override
	public Pair<AbstractState, Precision> prec(AbstractState s,
			Precision precision, ReachedSet reached) {
		// No precision refinement for this analysis
		return Pair.create(s, precision);
	}

}

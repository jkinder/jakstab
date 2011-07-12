package org.jakstab.analysis.explicit;

import java.util.Collections;
import java.util.Set;

import org.jakstab.analysis.*;
import org.jakstab.cfa.CFAEdge;
import org.jakstab.cfa.Location;
import org.jakstab.cfa.StateTransformer;
import org.jakstab.rtl.expressions.*;
import org.jakstab.rtl.statements.*;
import org.jakstab.util.Logger;
import org.jakstab.util.Pair;

/**
 * A K-Set analysis as used for the example in the VMCAI'09 paper. It 
 * represents values as sets of a constant size, merging them to TOP if
 * the bound is exceeded. Supports memory values and register aliasing
 * through the generic VariableValuation class.
 * 
 * For programs with procedure calls, it should be used together with a 
 * call-stack analysis for context sensitivity. Otherwise, it will merge
 * the stack contents from different calling contexts, which leads to
 * illegal addresses used as jump targets on return. 
 */
public class KSetAnalysis implements ConfigurableProgramAnalysis {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(KSetAnalysis.class);
	
	private AbstractValueFactory<KSet> valueFactory;
	private int bound;

	public KSetAnalysis(int k) {
		this.valueFactory = new KSetFactory(k);
		this.bound = k;
	}

	@Override
	public Precision initPrecision(Location location,
			StateTransformer transformer) {
		return null;
	}

	@Override
	public AbstractState initStartState(Location label) {
		return new ValuationState(valueFactory);
	}

	@Override
	public AbstractState merge(AbstractState s1, AbstractState s2,
			Precision precision) {
		return CPAOperators.mergeJoin(s1, s2, precision);
	}

	@Override
	public Set<AbstractState> post(AbstractState state, CFAEdge cfaEdge,
			Precision precision) {

		final RTLStatement statement = (RTLStatement)cfaEdge.getTransformer();
		final ValuationState iState = (ValuationState)state;
		return Collections.singleton(statement.accept(new DefaultStatementVisitor<AbstractState>() {

			@Override
			public AbstractState visit(RTLAlloc stmt) {
				ValuationState post = new ValuationState(iState);
				Writable lhs = stmt.getPointer();

				MemoryRegion newRegion;
				if (stmt.getAllocationName() != null) {
					newRegion = MemoryRegion.create(stmt.getAllocationName());
				} else {
					// TODO: Detect whether this allocation is unique to allow strong updates
					newRegion = MemoryRegion.createAsSummary("alloc" + stmt.getLabel());
				}
				
				KSet basePointer = new KSet(bound, new BasedNumberElement(newRegion, 
						ExpressionFactory.getInstance().createNumber(0, 32)));
				
				if (lhs instanceof RTLVariable) {
					post.setVariableValue((RTLVariable)lhs, basePointer); 
				} else {
					RTLMemoryLocation m = (RTLMemoryLocation)lhs;
					AbstractDomainElement evaledAddress = iState.abstractEval(m.getAddress());
					post.setMemoryValue(evaledAddress, m.getBitWidth(), basePointer);
				}

				return post;
			}

			@Override
			public AbstractState visit(RTLVariableAssignment stmt) {
				ValuationState post = new ValuationState(iState);
				AbstractDomainElement evaledRhs = iState.abstractEval(stmt.getRightHandSide());
				post.setVariableValue(stmt.getLeftHandSide(), evaledRhs);
				return post;
			}
			
			@Override
			public AbstractState visit(RTLMemoryAssignment stmt) {
				ValuationState post = new ValuationState(iState);
				AbstractDomainElement evaledRhs = iState.abstractEval(stmt.getRightHandSide());
				RTLMemoryLocation m = stmt.getLeftHandSide();
				AbstractDomainElement evaledAddress = iState.abstractEval(m.getAddress());
				post.setMemoryValue(evaledAddress, m.getBitWidth(), evaledRhs);
				return post;
			}

			@Override
			public AbstractState visit(RTLAssume stmt) {
				if (stmt.getAssumption() instanceof RTLOperation) {
					RTLOperation operation = (RTLOperation)stmt.getAssumption(); 
					if (operation.getOperator() == Operator.EQUAL) {					
						RTLExpression lhs = operation.getOperands()[0];
						RTLExpression rhs = operation.getOperands()[1];
						AbstractDomainElement evaledRhs = iState.abstractEval(rhs);
						if (lhs instanceof RTLVariable) {
							ValuationState post = new ValuationState(iState);
							post.setVariableValue((RTLVariable)lhs, evaledRhs);
							return post;
						} else if (lhs instanceof RTLMemoryLocation) {
							ValuationState post = new ValuationState(iState);
							RTLMemoryLocation m = (RTLMemoryLocation)lhs;
							AbstractDomainElement evaledAddress = iState.abstractEval(m.getAddress());
							post.setMemoryValue(evaledAddress, m.getBitWidth(), evaledRhs);
							return post;
						}
					}
				}
				return iState;
			}

			@Override
			public AbstractState visit(RTLAssert stmt) {
				return iState;
			}

			@Override
			public AbstractState visit(RTLDealloc stmt) {
				return iState;
			}

			@Override
			public AbstractState visit(RTLHavoc stmt) {
				return iState;
			}

			@Override
			public AbstractState visit(RTLSkip stmt) {
				return iState;
			}			
		}));

	}

	@Override
	public Pair<AbstractState, Precision> prec(AbstractState s,
			Precision precision, ReachedSet reached) {
		return Pair.create(s, precision);
	}

	@Override
	public boolean stop(AbstractState s, ReachedSet reached, Precision precision) {
		return CPAOperators.stopJoin(s, reached, precision);
	}

	@Override
	public AbstractState strengthen(AbstractState s,
			Iterable<AbstractState> otherStates, CFAEdge cfaEdge,
			Precision precision) {
		return s;
	}

}

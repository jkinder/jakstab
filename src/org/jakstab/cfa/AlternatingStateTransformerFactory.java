/*
 * AlternatingStateTransformerFactory.java - This file is part of the Jakstab project.
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
package org.jakstab.cfa;

import java.util.Collections;
import java.util.Set;

import org.jakstab.Program;
import org.jakstab.analysis.AbstractState;
import org.jakstab.analysis.UnderApproximateState;
import org.jakstab.analysis.composite.DualCompositeState;
import org.jakstab.asm.AbsoluteAddress;
import org.jakstab.cfa.CFAEdge.Kind;
import org.jakstab.rtl.Context;
import org.jakstab.rtl.RTLLabel;
import org.jakstab.rtl.expressions.ExpressionFactory;
import org.jakstab.rtl.expressions.RTLExpression;
import org.jakstab.rtl.expressions.RTLNumber;
import org.jakstab.rtl.statements.*;
import org.jakstab.util.FastSet;
import org.jakstab.util.Logger;
import org.jakstab.util.Tuple;

/**
 * Provides state transformers without assumptions about procedures. Call instructions
 * are treated as push / jmp combinations and return instructions as indirect jumps. If
 * the target of a jump cannot be resolved, plug in values from an under-approximation
 * to create MUST edges.
 * 
 * For regular statements, produces MUST edges if there is an under-approximate witness.  
 * 
 * @author Johannes Kinder
 */
public class AlternatingStateTransformerFactory extends ResolvingTransformerFactory {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AlternatingStateTransformerFactory.class);

	private ExpressionFactory factory = ExpressionFactory.getInstance();
	
	@Override
	public Set<CFAEdge> getTransformers(final AbstractState a) {
		RTLStatement stmt = Program.getProgram().getStatement((RTLLabel)a.getLocation());

		Set<CFAEdge> transformers = stmt.accept(new DefaultStatementVisitor<Set<CFAEdge>>() {

			@Override
			protected Set<CFAEdge> visitDefault(RTLStatement stmt) {
				
				CFAEdge.Kind edgeKind = Kind.MAY;
	
				// If any under-approximate component is not BOT, then we have a witness
				// and this is a MUST edge.
				DualCompositeState dcs = (DualCompositeState)a;
				for (int i = 0; i < dcs.numComponents(); i++) {
					AbstractState componentState = dcs.getComponent(i);
					if (componentState instanceof UnderApproximateState &&
							!componentState.isBot()) {
						edgeKind = Kind.MUST;
						break;
					}
				}
				
				return Collections.singleton(new CFAEdge(stmt.getLabel(), stmt.getNextLabel(), stmt, edgeKind));
			}

			@Override
			public Set<CFAEdge> visit(RTLGoto stmt) {
				assert stmt.getCondition() != null;
				Set<CFAEdge> results = new FastSet<CFAEdge>();
				
				// Add all edges from under-approximation
				addEdges(results, stmt, ((DualCompositeState)a).projection(
						stmt.getCondition(), stmt.getTargetExpression()), CFAEdge.Kind.MUST);

				// Add all edges from over-approximation
				addEdges(results, stmt, a.projectionFromConcretization(
						stmt.getCondition(), stmt.getTargetExpression()), CFAEdge.Kind.MAY);

				return results;
			}

			@Override
			public Set<CFAEdge> visit(RTLHalt stmt) {
				return Collections.emptySet();
			}

		});		

		saveNewEdges(transformers, a.getLocation());

		return transformers;
	}

	private void addEdges(Set<CFAEdge> results, final RTLGoto stmt, Set<Tuple<RTLNumber>> valuePairs, CFAEdge.Kind kind) {
		
		for (Tuple<RTLNumber> pair : valuePairs) {
			RTLNumber conditionValue = pair.get(0);
			RTLNumber targetValue = pair.get(1);
			RTLLabel nextLabel;
			// Start building the assume expression: assume correct condition case 
			assert conditionValue != null;
			RTLExpression assumption = 
				factory.createEqual(stmt.getCondition(), conditionValue);
			if (conditionValue.equals(factory.FALSE)) {
				// assume (condition = false), and set next statement to fallthrough
				nextLabel = stmt.getNextLabel();
			} else {
				if (targetValue == null) {
					logger.debug("No value from " + kind + "-analysis at " + stmt.getLabel());
					if (kind.equals(CFAEdge.Kind.MAY)) { 
						sound = false;
						unresolvedBranches.add(stmt.getLabel());
					}
					continue;
				} else {
					// assume (condition = true AND targetExpression = targetValue)
					assumption = factory.createAnd(
							assumption,
							factory.createEqual(
									stmt.getTargetExpression(),
									targetValue)
					);
					// set next label to jump target
					nextLabel = new RTLLabel(new AbsoluteAddress(targetValue));
				}
			}
			assumption = assumption.evaluate(new Context());
			RTLAssume assume = new RTLAssume(assumption, stmt);
			assume.setLabel(stmt.getLabel());
			assume.setNextLabel(nextLabel);
			// Target address sanity check
			if (nextLabel.getAddress().getValue() < 10L) {
				logger.warn("Control flow from " + stmt.getLabel() + " reaches address " + nextLabel.getAddress() + "!");
			}

			results.add(new CFAEdge(assume.getLabel(), assume.getNextLabel(), assume, kind));
		}

	}

	@Override
	protected Set<CFAEdge> resolveGoto(AbstractState a, RTLGoto stmt) {
		throw new UnsupportedOperationException("Not used");
	}
}

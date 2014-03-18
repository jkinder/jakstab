/*
 * ProcedureAnalysis.java - This file is part of the Jakstab project.
 * Copyright 2007-2014 Johannes Kinder <jk@jakstab.org>
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
package org.jakstab.analysis.procedures;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jakstab.AnalysisProperties;
import org.jakstab.analysis.*;
import org.jakstab.cfa.CFAEdge;
import org.jakstab.cfa.Location;
import org.jakstab.cfa.StateTransformer;
import org.jakstab.rtl.statements.DefaultStatementVisitor;
import org.jakstab.rtl.statements.RTLAssume;
import org.jakstab.rtl.statements.RTLCallReturn;
import org.jakstab.rtl.statements.RTLStatement;
import org.jakstab.util.FastSet;
import org.jakstab.util.Logger;
import org.jakstab.util.Pair;

/**
 * @author Johannes Kinder
 */
public class ProcedureAnalysis implements ConfigurableProgramAnalysis {

	public static void register(AnalysisProperties p) {
		p.setName("Procedure detection");
		p.setDescription("Detect procedures using call-return matching.");
		p.setShortHand('o');
	}

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ProcedureAnalysis.class);
	
	private final Set<Pair<Location, Location>> callSites;
	private final Set<Location> callees;

	public ProcedureAnalysis() {
		callSites = new HashSet<Pair<Location, Location>>();
		callees = new HashSet<Location>();
	}

	@Override
	public Precision initPrecision(Location location, StateTransformer transformer) {
		return null;
	}

	@Override
	public AbstractState initStartState(Location location) {
		return new ProcedureState(new FastSet<Location>(location));
	}

	@Override
	public AbstractState merge(AbstractState s1, AbstractState s2,
			Precision precision) {
		return CPAOperators.mergeJoin(s1, s2, precision);
	}

	@Override
	public Set<AbstractState> post(final AbstractState state, final CFAEdge edge,
			Precision precision) {
		
		final RTLStatement stmt = (RTLStatement)edge.getTransformer();
		
		return stmt.accept(new DefaultStatementVisitor<Set<AbstractState>>() {

			@Override
			public Set<AbstractState> visit(RTLAssume assume) {
				if (assume.getSource() != null) {
					AbstractState post;
					switch (assume.getSource().getType()) {
					case CALL:
						callSites.add(Pair.create(edge.getSource(), edge.getTarget()));
						callees.add(edge.getTarget());
						post = new ProcedureState(new FastSet<Location>(edge.getTarget()));
						return Collections.singleton(post);
					case RETURN:
						//post = new ProcedureState(new FastSet<Location>(((Location)edge.getTarget())));
						// FIXME: We would need to have a callstack to determine the procedure we are returning to
						// So currently this works only with OptimisticTransformerFactory
						// post = getProcedureState((getCallStackElement().getLabel()))
						// Returning empty set means that the target is unreachable (because the analysis has to be
						// over-approximate), which isn't true.
						//return Collections.emptySet();
						// 
						// return BOT procedure state so it gets merged with fall-through of call 
						post = new ProcedureState(new FastSet<Location>());
						return Collections.singleton(post);
					default: 
						// nothing
					}
				}
				return Collections.singleton(state);
			}

			@Override
			public Set<AbstractState> visit(RTLCallReturn stmt) {
				// Keep current procedure
				return Collections.singleton(state);
			}

			@Override
			protected Set<AbstractState> visitDefault(RTLStatement stmt) {
				// Else just keep the current procedure, includes UnknownProcedureCall edges!
				return Collections.singleton(state);
			}
			
		});
	}

	@Override
	public AbstractState strengthen(AbstractState s, Iterable<AbstractState> otherStates,
			CFAEdge cfaEdge, Precision precision) {
		return s;
	}

	@Override
	public Pair<AbstractState, Precision> prec(AbstractState s,
			Precision precision, ReachedSet reached) {
		return Pair.create(s, precision);
	}
	
	public Set<Location> getCallees() {
		return callees;
	}
	
	public Set<Pair<Location,Location>> getCallSites() {
		return callSites;
	}
	
	public String toString() {
		return getCallees().toString();
	}

	@Override
	public boolean stop(AbstractState s, ReachedSet reached, Precision precision) {
		return CPAOperators.stopSep(s, reached, precision);
	}
}

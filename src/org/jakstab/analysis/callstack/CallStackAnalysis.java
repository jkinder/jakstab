/*
 * CallStackAnalysis.java - This file is part of the Jakstab project.
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
package org.jakstab.analysis.callstack;

import java.util.Collections;
import java.util.Set;

import org.jakstab.analysis.*;
import org.jakstab.cfa.CFAEdge;
import org.jakstab.cfa.Location;
import org.jakstab.cfa.StateTransformer;
import org.jakstab.util.Logger;
import org.jakstab.util.Pair;

/**
 * @author Johannes Kinder
 */
public class CallStackAnalysis implements ConfigurableProgramAnalysis {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CallStackAnalysis.class);

	/*
	 * @see org.jakstab.analysis.ConfigurableProgramAnalysis#initPrecision()
	 */
	@Override
	public Precision initPrecision(Location location, StateTransformer transformer) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * @see org.jakstab.analysis.ConfigurableProgramAnalysis#initStartState(org.jakstab.rtl.RTLLabel)
	 */
	@Override
	public AbstractState initStartState(Location label) {
		return new CallStackState();
	}

	/*
	 * @see org.jakstab.analysis.ConfigurableProgramAnalysis#merge(org.jakstab.analysis.AbstractState, org.jakstab.analysis.AbstractState, org.jakstab.analysis.Precision)
	 */
	@Override
	public AbstractState merge(AbstractState s1, AbstractState s2,
			Precision precision) {
		return CPAOperators.mergeSep(s1, s2, precision);
	}

	/*
	 * @see org.jakstab.analysis.ConfigurableProgramAnalysis#post(org.jakstab.analysis.AbstractState, org.jakstab.analysis.StateTransformer, org.jakstab.analysis.Precision)
	 */
	@Override
	public Set<AbstractState> post(AbstractState state,
			CFAEdge cfaEdge, Precision precision) {
		return Collections.singleton(((CallStackState)state).abstractPost(cfaEdge.getTransformer(), precision));
	}

	@Override
	public AbstractState strengthen(AbstractState s, Iterable<AbstractState> otherStates,
			CFAEdge cfaEdge, Precision precision) {
		return s;
	}

	/*
	 * @see org.jakstab.analysis.ConfigurableProgramAnalysis#prec(org.jakstab.analysis.AbstractState, org.jakstab.analysis.Precision, org.jakstab.analysis.ReachedSet)
	 */
	@Override
	public Pair<AbstractState, Precision> prec(AbstractState s,
			Precision precision, ReachedSet reached) {
		return Pair.create(s, precision);
	}

	/*
	 * @see org.jakstab.analysis.ConfigurableProgramAnalysis#stop(org.jakstab.analysis.AbstractState, org.jakstab.analysis.ReachedSet, org.jakstab.analysis.Precision)
	 */
	@Override
	public boolean stop(AbstractState s, ReachedSet reached, Precision precision) {
		return CPAOperators.stopSep(s, reached, precision);
	}

}

/*
 * ResolvingTransformerFactory.java - This file is part of the Jakstab project.
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

import java.util.*;

import org.jakstab.Program;
import org.jakstab.analysis.AbstractState;
import org.jakstab.rtl.RTLLabel;
import org.jakstab.rtl.statements.*;
import org.jakstab.util.FastSet;
import org.jakstab.util.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Abstract class for all resolving state transformer factories, that is, factories implementing 
 * the resolve-operator from "Kinder, Veith, Zuleger - An abstract interpretation-based 
 * framework for control flow reconstruction from binaries, VMCAI 2009".
 * 
 * @author Johannes Kinder
 */
public abstract class ResolvingTransformerFactory implements
StateTransformerFactory {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ResolvingTransformerFactory.class);

	protected final Set<Location> unresolvedBranches = new FastSet<Location>();
	protected boolean sound = true;
	protected SetMultimap<Location,CFAEdge> outEdges = HashMultimap.create();

	public boolean isSound() {
		return sound;
	}

	public Set<Location> getUnresolvedBranches() {
		return unresolvedBranches;
	}

	@Override
	public Set<CFAEdge> getTransformers(final AbstractState a) {
		RTLStatement stmt = Program.getProgram().getStatement((RTLLabel)a.getLocation());

		Set<CFAEdge> transformers = stmt.accept(new DefaultStatementVisitor<Set<CFAEdge>>() {

			@Override
			protected Set<CFAEdge> visitDefault(RTLStatement stmt) {
				return Collections.singleton(new CFAEdge(stmt.getLabel(), stmt.getNextLabel(), stmt));
			}

			@Override
			public Set<CFAEdge> visit(RTLGoto stmt) {
				// Call resolve function of subclass
				return resolveGoto(a, stmt);
			}

			@Override
			public Set<CFAEdge> visit(RTLHalt stmt) {
				return Collections.emptySet();
			}

		});		

		saveNewEdges(transformers, a.getLocation());

		return transformers;
	}

	protected void saveNewEdges(Set<CFAEdge> transformers, Location l) {
		// Make sure we only add new edges. Edges are mutable so we cannot just implement
		// hashCode and equals and add everything into a HashSet.
		for (CFAEdge edge : transformers) {
			boolean found = false;
			// We check for this in the loop, because transformers may contain duplicate edges
			// that only differ in their kind. So we check them against each other for upgrading
			if (outEdges.containsKey(l)) {
				for (CFAEdge existingEdge : outEdges.get(l)) {
					if (existingEdge.getTarget().equals(edge.getTarget())) {

						// There is an edge with the same target
						found = true;

						// If the new kind is greater than the existing, upgrade to new kind
						if (!existingEdge.getKind().equals(edge.getKind()) && existingEdge.getKind().lessOrEqual(edge.getKind())) {
							logger.debug("Upgrading existing edge " + existingEdge + " from " + 
									existingEdge.getKind() + " to " + edge.getKind());
							existingEdge.setKind(edge.getKind());
						}
						// Incomparable edge kinds cannot happen with current logic
						assert edge.getKind().lessOrEqual(existingEdge.getKind()) : "Incomparable edge kinds!";

						break;
					}
				}
			}
			if (!found) outEdges.put(l,  edge); //newEdges.add(edge);
		}

	}

	public Set<CFAEdge> getExistingOutEdges(Location l) {
		return outEdges.get(l);
	}

	public Set<CFAEdge> getCFA() {
		Set<CFAEdge> cfa = new HashSet<CFAEdge>();
		for (CFAEdge edge : outEdges.values()) {
			cfa.add(edge);
		}
		return cfa;
	}

	protected abstract Set<CFAEdge> resolveGoto(final AbstractState a, final RTLGoto stmt);

	@Override
	public Location getInitialLocation() {
		return Program.getProgram().getStart();
	}
}

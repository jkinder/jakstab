/*
 * FineGrainedCFG.java - This file is part of the Jakstab project.
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
package org.jakstab.cfa;

import java.util.Set;

import org.jakstab.rtl.statements.RTLAssume;
import org.jakstab.rtl.statements.RTLCallReturn;

/**
 * Control flow graph that splits basic blocks after every assume, including
 * calls, returns, and jumps.  
 */
public class FineGrainedCFG extends ControlFlowGraph {
	
	public FineGrainedCFG(Set<CFAEdge> edges) {
		super(edges);
	}

	protected boolean isBasicBlockHead(Location l) {
		if (super.isBasicBlockHead(l))
			return true;
		
		// Split blocks at any assume / callReturn
		
		CFAEdge e = getInEdges(l).iterator().next();
		
		if (e.getTransformer() instanceof RTLAssume)
			return true;
		
		if (e.getTransformer() instanceof RTLCallReturn)
			return true;

		return false;
	}

}

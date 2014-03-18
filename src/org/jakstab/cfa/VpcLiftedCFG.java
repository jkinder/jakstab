/*
 * VpcLiftedCFG.java - This file is part of the Jakstab project.
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
import org.jakstab.util.Logger;

public class VpcLiftedCFG extends ControlFlowGraph {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(VpcLiftedCFG.class);
	
	public VpcLiftedCFG(Set<CFAEdge> edges) {
		super(edges);
	}

	protected boolean isBasicBlockHead(Location l) {
		if (super.isBasicBlockHead(l))
			return true;
		
		CFAEdge e = getInEdges(l).iterator().next();
		
		// Split blocks at calls / returns / callReturns
		
		if (e.getTransformer() instanceof RTLAssume) {
			RTLAssume a = (RTLAssume)e.getTransformer();
			return a.isCall() || a.isReturn();
		}
		
		if (e.getTransformer() instanceof RTLCallReturn)
			return true;

		return false;
	}
	
}

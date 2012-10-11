/*
 * AbstractReachabilityTree.java - This file is part of the Jakstab project.
 * Copyright 2007-2012 Johannes Kinder <jk@jakstab.org>
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
package org.jakstab.analysis;

import java.util.*;

import org.jakstab.cfa.CFAEdge;
import org.jakstab.util.*;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Stores relationships among states in the abstract state space. 
 * FIXME: This is no longer a tree and not used as an ART. The functionality
 * of this class needs to be merged with ReachedSet
 * 
 * @author Johannes Kinder
 */
public class AbstractReachabilityTree {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractReachabilityTree.class);
	
	private AbstractState root;
	private SetMultimap<AbstractState, Pair<CFAEdge, AbstractState>> parentChildrenMap;
	private Map<AbstractState, AbstractState> childParentMap;
	
	public AbstractReachabilityTree() {
		parentChildrenMap = HashMultimap.create();
		childParentMap = new HashMap<AbstractState, AbstractState>();
	}
	
	/**
	 * Get the ART root.
	 * 
	 * @return the root of the ART.
	 */
	public AbstractState getRoot() {
		return root;
	}
	
	public void setRoot(AbstractState root) {
		this.root = root;
		addChild(null, null, root);
	}
	
	public Set<Pair<CFAEdge, AbstractState>> getChildren(AbstractState parent) {
		return parentChildrenMap.get(parent);
	}
	
	public AbstractState getParent(AbstractState child) {
		return childParentMap.get(child);
	}
	
	public void addChild(AbstractState parent, CFAEdge e, AbstractState child) {
		//logger.debug("Adding child " + child.getIdentifier() + " to " + (parent == null ? "null" : parent.getIdentifier()));
		assert childParentMap.isEmpty() || parent != null;
		
		parentChildrenMap.put(parent, Pair.create(e, child));
		childParentMap.put(child, parent);
		//assert (isTree()) : "ART lost tree property!";
	}
	
	public boolean isInChildParentMap(AbstractState a) {
		return childParentMap.containsKey(a);
	}
	
	public boolean isInParentChildrenMap(AbstractState a) {
		return parentChildrenMap.containsKey(a);
	}

}

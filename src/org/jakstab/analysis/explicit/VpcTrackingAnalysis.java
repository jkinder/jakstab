/*
 * VpcTrackingAnalysis.java - This file is part of the Jakstab project.
 * Copyright 2007-2013 Johannes Kinder <jk@jakstab.org>
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
package org.jakstab.analysis.explicit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jakstab.AnalysisProperties;
import org.jakstab.JOption;
import org.jakstab.Program;
import org.jakstab.analysis.AbstractState;
import org.jakstab.analysis.CPAOperators;
import org.jakstab.analysis.ConfigurableProgramAnalysis;
import org.jakstab.analysis.MemoryReference;
import org.jakstab.analysis.MemoryRegion;
import org.jakstab.analysis.PartitionedMemory;
import org.jakstab.analysis.Precision;
import org.jakstab.analysis.ReachedSet;
import org.jakstab.analysis.ValueContainer;
import org.jakstab.asm.AbsoluteAddress;
import org.jakstab.cfa.CFAEdge;
import org.jakstab.cfa.Location;
import org.jakstab.cfa.StateTransformer;
import org.jakstab.rtl.expressions.RTLVariable;
import org.jakstab.rtl.statements.DefaultStatementVisitor;
import org.jakstab.rtl.statements.RTLAssume;
import org.jakstab.rtl.statements.RTLGoto;
import org.jakstab.rtl.statements.RTLStatement;
import org.jakstab.ssl.Architecture;
import org.jakstab.util.Logger;
import org.jakstab.util.Pair;
import org.jakstab.util.MapMap.EntryIterator;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;

public class VpcTrackingAnalysis implements ConfigurableProgramAnalysis {
	
	private final static Logger logger = Logger.getLogger(VpcTrackingAnalysis.class);

	public static void register(AnalysisProperties p) {
		p.setShortHand('v');
		p.setName("BAT-VPC");
		p.setDescription("VPC-sensitive version of Bounded Address Tracking.");
		p.setExplicit(true);
	}
	public static JOption<String> vpcName = JOption.create("vpc", "r", "esi", "Register to be used as virtual program counter.");
	
	private static boolean procSensitiveVpc = true; 
	private Map<Location, ValueContainer> vpcMap;
	private Map<AbsoluteAddress, Location> procedureMap;
	
	private Architecture arch;
	
	public VpcTrackingAnalysis() {
		vpcMap = new HashMap<Location, ValueContainer>();
		procedureMap = new HashMap<AbsoluteAddress, Location>();
		arch = Program.getProgram().getArchitecture();
	}
	
	@Override
	public AbstractState merge(AbstractState s1, AbstractState s2, Precision precision) {
		// Reduces states, but makes it harder to reconstruct the trace that lead to a certain state
		if (s2.lessOrEqual(s1)) return s1;
		return CPAOperators.mergeSep(s1, s2, precision);
	}

	@Override
	public boolean stop(AbstractState s, ReachedSet reached, Precision precision) {
		return CPAOperators.stopSep(s, reached, precision);
	}

	@Override
	public Set<AbstractState> post(AbstractState state, final CFAEdge cfaEdge, Precision precision) {
		
		if (state.isBot())
			return Collections.singleton(state);
		
		BasedNumberValuation b = (BasedNumberValuation)state;
		VpcPrecision vprec = (VpcPrecision)precision;
		
		final RTLStatement stmt = (RTLStatement)cfaEdge.getTransformer();
		
		/* Do procedure analysis - for now, this is inlined here, should be made its own analysis */
		stmt.accept(new DefaultStatementVisitor<Void>() {

			private void copyOldProcToTarget(Location target) {
				if (getProcedure(target) != null) 
					return;
				Location oldProc = getProcedure(cfaEdge.getSource());
				if (oldProc != null)
					setProcedure(target, oldProc);
			}


			@Override
			public Void visit(RTLAssume stmt) {
				if (stmt.isCall()) {
					// start new procedure unless we're calling into the middle of an existing one
					if (getProcedure(cfaEdge.getTarget()) == null)
						setProcedure(cfaEdge.getTarget(), cfaEdge.getTarget());

					// Fall through edge in current proc
					RTLGoto gotoStmt = stmt.getSource();
					if (gotoStmt.getNextLabel() != null) {
						copyOldProcToTarget(gotoStmt.getNextLabel());
					}

				} else if (stmt.isReturn()) {
					// do nothing
				} else {
					// stay in same procedure
					copyOldProcToTarget(cfaEdge.getTarget());
				}

				return null;
			}

			@Override
			protected Void visitDefault(RTLStatement stmt) {
				// includes call-return
				copyOldProcToTarget(cfaEdge.getTarget());
				return null;
			}

		});
		//logger.debug(cfaEdge.getTarget() + " is in procedure " + procedureMap.get(cfaEdge.getTarget()));

		// Will not hold if --basicblocks is enabled - precision is reused there for several locs
		//assert (cfaEdge.getTarget().equals(vprec.getLocation()));
		
		BasedNumberElement vpcValue = getVpcValue(b, getVpc(cfaEdge.getTarget()));
		ExplicitPrecision eprec = vprec.getPrecision(vpcValue);

		return b.abstractPost(stmt, eprec);
	}
	
	@Override
	public AbstractState strengthen(AbstractState s, Iterable<AbstractState> otherStates,
			CFAEdge cfaEdge, Precision precision) {
		return s;
	}
	
	public Location getProcedure(Location location) {
		return procedureMap.get(location.getAddress());
	}
	
	private void setProcedure(Location location, Location procHead) {
		procedureMap.put(location.getAddress(), procHead);
	}
	
	public ValueContainer getVpc(Location location) {
		
		// No VPC for harness code
		if (Program.getProgram().getModule(location.getAddress()) == null)
			return null;
		
		if (procSensitiveVpc)
			location = getProcedure(location);

		return vpcMap.get(location);
	}

	private void setVpc(Location location, ValueContainer vpc) {
		if (procSensitiveVpc)
			location = getProcedure(location);
		
		vpcMap.put(location, vpc);
	}
	
	private BasedNumberElement getVpcValue(BasedNumberValuation s, ValueContainer vpc) {
		if (vpc == null)
			return BasedNumberElement.getTop(32);
		else
			return s.getValue(vpc);
	}

	@Override
	public Pair<AbstractState, Precision> prec(AbstractState s, Precision precision, ReachedSet reached) {
		
		if (s.isBot())
			return Pair.create(s, precision);
		
		// This method uses the fact that there is only 1 precision per location
		
		VpcPrecision vprec = (VpcPrecision)precision;
		BasedNumberValuation widenedState = (BasedNumberValuation)s;
		Location loc = vprec.getLocation();
		BasedNumberElement vpcValue = getVpcValue(widenedState, getVpc(loc));
		ExplicitPrecision eprec = vprec.getPrecision(vpcValue);
		
		// If we don't have a VPC yet, first try to determine one from value counts.
		if (getVpc(loc) == null) {
			
			Multimap<Integer, ValueContainer> candidates = HashMultimap.create();

			// No support for heap-based VPCs at the moment (merging heap contents speeds up convergence) 
			int vpcThreshold = BoundedAddressTracking.varThreshold.getValue();
					/*Math.min(BoundedAddressTracking.varThreshold.getValue(), 
					BoundedAddressTracking.heapThreshold.getValue());*/

			// Only check value counts if we have at least enough states to reach it
			if (reached.size() > vpcThreshold) {
				
				// Check value counts for variables
				for (RTLVariable v : eprec.varMap.keySet()) {
					Set<BasedNumberElement> existingValues = eprec.varMap.get(v);
					
					// Check first whether we should promote this var to VPC
					if (arch.isRegister(v) && 
							existingValues.size() >= 2) {

						if (v.getName().equals("eax"))
							continue;
						
						candidates.put(existingValues.size(), v);
					}
				}

				// Check value counts for store
				PartitionedMemory<BasedNumberElement> sStore = ((BasedNumberValuation)s).getStore();
				for (EntryIterator<MemoryRegion, Long, BasedNumberElement> entryIt = sStore.entryIterator(); entryIt.hasEntry(); entryIt.next()) {
					MemoryRegion region = entryIt.getLeftKey();
					Long offset = entryIt.getRightKey();
					SetMultimap<Long, BasedNumberElement> memoryMap = eprec.regionMaps.get(region);
					if (memoryMap == null) continue;
					
					Set<BasedNumberElement> existingValues = memoryMap.get(offset);
					
					if (existingValues.size() >= 2) {
						candidates.put(existingValues.size(), new MemoryReference(entryIt.getLeftKey(), 
								entryIt.getRightKey(), existingValues.iterator().next().getBitWidth()));
					}

				}
			}
			
			if (!candidates.isEmpty()) {
				ArrayList<Integer> counts = new ArrayList<Integer>(candidates.keySet());
				Collections.sort(counts, Collections.reverseOrder());
				
				if (counts.get(0) >= vpcThreshold) {				
					logger.verbose("Value threshold reached, choosing VPC for location " + loc + ". Candidates:");
					for (Integer c : counts)
						logger.verbose("  " + c + ": " + candidates.get(c));

					setVpc(loc, candidates.get(counts.get(0)).iterator().next());
					logger.verbose(loc + ": Set VPC to " + getVpc(loc));

					// Reload explicit precision for new VPC
					vpcValue = getVpcValue(widenedState, getVpc(loc));
					eprec = vprec.getPrecision(vpcValue);
				}
			}
		}

		
		// Only check value counts if we have at least enough states to reach it
		if (reached.size() > Math.min(BoundedAddressTracking.varThreshold.getValue(), 
				BoundedAddressTracking.heapThreshold.getValue())) {
			
			boolean changed = false;

			// Check value counts for variables
			for (RTLVariable v : eprec.varMap.keySet()) {
				//BasedNumberElement currentValue = ((BasedNumberValuation)s).getValue(v);
				Set<BasedNumberElement> existingValues = eprec.varMap.get(v);
				
				int threshold = eprec.getThreshold(v);
				
				if (existingValues.size() > threshold) {
					// Lower precisions and widen the value in this state, too.
					// This avoids values accumulating at join points (where they are not
					// intercepted by the precision-aware setValue)
					if (countRegions(existingValues) > threshold) {
						eprec.stopTracking(v);
						if (!changed) {
							widenedState = new BasedNumberValuation(widenedState);
							changed = true;
						}
						widenedState.setValue(v, BasedNumberElement.getTop(v.getBitWidth()));
					} else {
						eprec.trackRegionOnly(v);
						if (!changed) {
							widenedState = new BasedNumberValuation(widenedState);
							changed = true;
						}
						logger.debug("Only tracking region of " + v + ", values were " + existingValues);
						widenedState.setValue(v, new BasedNumberElement(
								widenedState.getValue(v).getRegion(), 
								NumberElement.getTop(v.getBitWidth())));
					}
				}
			}

			
			// Check value counts for store
			PartitionedMemory<BasedNumberElement> sStore = ((BasedNumberValuation)s).getStore();
			for (EntryIterator<MemoryRegion, Long, BasedNumberElement> entryIt = sStore.entryIterator(); entryIt.hasEntry(); entryIt.next()) {
				MemoryRegion region = entryIt.getLeftKey();
				Long offset = entryIt.getRightKey();
				BasedNumberElement value = entryIt.getValue();
				SetMultimap<Long, BasedNumberElement> memoryMap = eprec.regionMaps.get(region);
				if (memoryMap == null) continue;
				
				//BasedNumberElement currentValue = entry.getValue();
				Set<BasedNumberElement> existingValues = memoryMap.get(offset);

				int threshold = eprec.getStoreThreshold(region, offset);
				if (existingValues.size() > threshold) {
					if (countRegions(existingValues) > 5*threshold) {
						eprec.stopTracking(region, offset);
						if (!changed) {
							widenedState = new BasedNumberValuation(widenedState);
							changed = true;
						}
						widenedState.getStore().set(region, 
								offset, value.getBitWidth(), 
								BasedNumberElement.getTop(value.getBitWidth()));
					} else {
						eprec.trackRegionOnly(region, offset);
						if (!changed) {
							widenedState = new BasedNumberValuation(widenedState);
							changed = true;
						}
						widenedState.getStore().set(region, offset, value.getBitWidth(), 
								new BasedNumberElement(value.getRegion(), NumberElement.getTop(value.getBitWidth())));
					}
				}
			}
		}
		
		// Collect all values for all variables
		for (Map.Entry<RTLVariable, BasedNumberElement> entry : widenedState.getVariableValuation()) {
			RTLVariable var = entry.getKey();
			eprec.varMap.put(var, entry.getValue());
		}

		// Collect all values for all memory areas
		PartitionedMemory<BasedNumberElement> store = widenedState.getStore();
		for (EntryIterator<MemoryRegion, Long, BasedNumberElement> entryIt = store.entryIterator(); entryIt.hasEntry(); entryIt.next()) {
			SetMultimap<Long, BasedNumberElement> memoryMap = eprec.regionMaps.get(entryIt.getLeftKey());
			if (memoryMap == null) {
				memoryMap = HashMultimap.create();
				eprec.regionMaps.put(entryIt.getLeftKey(), memoryMap);
			}
			memoryMap.put(entryIt.getRightKey(), entryIt.getValue());
		}

		// If it was changed, widenedState is now a new state
		return Pair.create((AbstractState)widenedState, precision);
	}

	@Override
	public AbstractState initStartState(Location location) {
		return BasedNumberValuation.createInitialState();
	}

	@Override
	public Precision initPrecision(Location location, StateTransformer transformer) {		
		VpcPrecision vpcPrec = new VpcPrecision(location);
		// Store precision locally in map so we can retrieve VPCs later
		return vpcPrec;
	}
	
	private int countRegions(Set<BasedNumberElement> values) {
		Set<MemoryRegion> regions = new HashSet<MemoryRegion>();
		for (BasedNumberElement e : values)
			regions.add(e.getRegion());
		return regions.size();
	}

}

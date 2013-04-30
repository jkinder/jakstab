/*
 * VpcTrackingAnalysis.java - This file is part of the Jakstab project.
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
package org.jakstab.analysis.explicit;

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
	
	private Map<Location, VpcPrecision> vpcPrecisionMap;
	private Map<Location, Location> procedureMap;
	
	private Architecture arch;
	
	public VpcTrackingAnalysis() {
		vpcPrecisionMap = new HashMap<Location, VpcPrecision>();
		procedureMap = new HashMap<Location, Location>();
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
		BasedNumberValuation b = (BasedNumberValuation)state;
		VpcPrecision vprec = (VpcPrecision)precision;
		
		final RTLStatement stmt = (RTLStatement)cfaEdge.getTransformer();
		
		if (!procedureMap.containsKey(cfaEdge.getTarget())) {
			stmt.accept(new DefaultStatementVisitor<Void>() {

				@Override
				public Void visit(RTLAssume stmt) {
					RTLGoto gotoStmt = stmt.getSource();
					if (gotoStmt.getType() == RTLGoto.Type.CALL) {
						// start new procedure
						procedureMap.put(cfaEdge.getTarget(), cfaEdge.getTarget());
						
						// Fall through edge in current proc
						if (!procedureMap.containsKey(gotoStmt.getNextLabel())) {
							Location oldProc = procedureMap.get(cfaEdge.getSource());
							procedureMap.put(gotoStmt.getNextLabel(), oldProc);
						}
						
					} else if (stmt.getSource().getType() == RTLGoto.Type.RETURN) {
						// do nothing
					} else {
						// stay in same procedure
						procedureMap.put(cfaEdge.getTarget(), cfaEdge.getSource());
					}

					return null;
				}

				@Override
				protected Void visitDefault(RTLStatement stmt) {
					// includes call-return
					Location oldProc = procedureMap.get(cfaEdge.getSource());
					if (oldProc != null)
						procedureMap.put(cfaEdge.getTarget(), oldProc);
					return null;
				}

			});
			//logger.debug(cfaEdge.getTarget() + " is in procedure " + procedureMap.get(cfaEdge.getTarget()));
		}
		
		return b.abstractPost(stmt, vprec.getPrecision(b));
	}
	
	@Override
	public AbstractState strengthen(AbstractState s, Iterable<AbstractState> otherStates,
			CFAEdge cfaEdge, Precision precision) {
		return s;
	}

	@Override
	public Pair<AbstractState, Precision> prec(AbstractState s, Precision precision, ReachedSet reached) {
		
		// This method uses the fact that there is only 1 precision per location
		
		VpcPrecision vprec = (VpcPrecision)precision;
		BasedNumberValuation widenedState = (BasedNumberValuation)s;
		//BasedNumberElement vpcValue = widenedState.getValue(vpc);
		ExplicitPrecision eprec = vprec.getPrecision(widenedState);
		
		
		if (vprec.getVpc() == null) {

			// Make it -1 so that VPC detection triggers before any intermediate vars are widened 
			// (e.g., tmp in add VPC, x) 
			int vpcThreshold = Math.min(BoundedAddressTracking.varThreshold.getValue(), 
					BoundedAddressTracking.heapThreshold.getValue());
			// Only check value counts if we have at least enough states to reach it
			if (reached.size() > vpcThreshold) {
				
				// Check value counts for variables
				for (RTLVariable v : eprec.varMap.keySet()) {
					//BasedNumberElement currentValue = ((BasedNumberValuation)s).getValue(v);
					Set<BasedNumberElement> existingValues = eprec.varMap.get(v);
					
					// Check first whether we should promote this var to VPC
					if (arch.isRegister(v) && 
							existingValues.size() > vpcThreshold) {
						vprec.setVpc(v);
						logger.debug("Set VPC to " + vprec.getVpc());
						// increase threshold for others
						vpcThreshold = existingValues.size();
					}
				}

				// Check value counts for store
				PartitionedMemory<BasedNumberElement> sStore = ((BasedNumberValuation)s).getStore();
				for (EntryIterator<MemoryRegion, Long, BasedNumberElement> entryIt = sStore.entryIterator(); entryIt.hasEntry(); entryIt.next()) {
					MemoryRegion region = entryIt.getLeftKey();
					Long offset = entryIt.getRightKey();
					SetMultimap<Long, BasedNumberElement> memoryMap = eprec.regionMaps.get(region);
					if (memoryMap == null) continue;
					
					//BasedNumberElement currentValue = entry.getValue();
					Set<BasedNumberElement> existingValues = memoryMap.get(offset);
					
					if (existingValues.size() > vpcThreshold) {
						vprec.setVpc(new MemoryReference(entryIt.getLeftKey(), 
								entryIt.getRightKey(), existingValues.iterator().next().getBitWidth()));
						logger.debug("Set VPC to " + vprec.getVpc());

						vpcThreshold = existingValues.size();
					}

				}
			}
			
			// Reload explicit precision if VPC was set
			if (vprec.getVpc() != null)
				eprec = vprec.getPrecision(widenedState);
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
		VpcPrecision vpcPrec = new VpcPrecision();
		// Store precision locally in map so we can retrieve VPCs later
		vpcPrecisionMap.put(location, vpcPrec);
		return vpcPrec;
	}
	
	private int countRegions(Set<BasedNumberElement> values) {
		Set<MemoryRegion> regions = new HashSet<MemoryRegion>();
		for (BasedNumberElement e : values)
			regions.add(e.getRegion());
		return regions.size();
	}

	public ValueContainer getVPC(Location l) {
		VpcPrecision vpcPrec =  vpcPrecisionMap.get(l);
		if (vpcPrec == null) {
			logger.info("No VPC found for requested location " + l);
			return null;
		} else {
			return vpcPrec.getVpc();
		}
	}

}

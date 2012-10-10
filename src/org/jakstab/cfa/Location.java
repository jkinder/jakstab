package org.jakstab.cfa;

import org.jakstab.asm.AbsoluteAddress;

public interface Location extends Comparable<Location> {
	
	public AbsoluteAddress getAddress();

}

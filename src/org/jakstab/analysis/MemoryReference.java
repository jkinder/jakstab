package org.jakstab.analysis;

import org.jakstab.rtl.BitVectorType;

/**
 * A fixed reference to a memory location in a certain memory region.
 * 
 * @author Johannes Kinder
 */
public class MemoryReference implements ValueContainer, BitVectorType {

	private final MemoryRegion region;
	private final long offset;
	private final int bitwidth;
	
	public MemoryReference(MemoryRegion region, long offset, int bitwidth) {
		super();
		this.region = region;
		this.offset = offset;
		this.bitwidth = bitwidth;
	}

	public MemoryRegion getRegion() {
		return region;
	}

	public long getOffset() {
		return offset;
	}

	@Override
	public int getBitWidth() {
		return bitwidth;
	}

	@Override
	public String toString() {
		return "m" + bitwidth + "[" + region + " + " + offset + "]";
	}
}

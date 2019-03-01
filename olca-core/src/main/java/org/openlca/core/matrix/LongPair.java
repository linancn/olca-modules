package org.openlca.core.matrix;

/**
 * A LongPair saves just a pair of longs. We use this typically to efficiently
 * store the IDs of a process and a product or waste flow as pair that we then
 * use in indices to build matrix structures for the calculation. In this case,
 * the first value should be always the process ID and the second the flow ID.
 */
public class LongPair implements Comparable<LongPair> {

	public long first;
	public long second;

	public LongPair(long first, long second) {
		this.first = first;
		this.second = second;
	}

	public static LongPair of(long first, long second) {
		return new LongPair(first, second);
	}

	@Override
	public String toString() {
		return "LongPair[first=" + first
				+ ", second=" + second + "]";
	}

	@Override
	public int hashCode() {
		long h = first * 79 + second;
		int hash = (int) ((h >> 32) ^ h);
		return hash;
	}

	public boolean equals(long first, long second) {
		return this.first == first && this.second == second;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (!(obj instanceof LongPair))
			return false;
		LongPair other = (LongPair) obj;
		return this.first == other.first && this.second == other.second;
	}

	@Override
	public int compareTo(LongPair other) {
		if (other == null)
			return 1;
		int c = compare(this.first, other.first);
		if (c != 0)
			return c;
		return compare(this.second, other.second);
	}

	private int compare(long long1, long long2) {
		if (long1 < long2)
			return -1;
		if (long1 > long2)
			return 1;
		return 0;
	}

}

package ch.njol.util;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;

public class ArraySet<T> extends AbstractSet<T> {
	
	private final T[] values;
	
	@SafeVarargs
	public ArraySet(final T... values) {
		this.values = values;
	}
	
	@Override
	public Iterator<T> iterator() {
		return Arrays.asList(values).iterator();
	}
	
	@Override
	public int size() {
		return values.length;
	}
	
}

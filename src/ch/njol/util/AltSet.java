package ch.njol.util;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.jdt.annotation.Nullable;

public abstract class AltSet<T> extends AbstractSet<T> {
	
	abstract int hashCode(T value);
	
	abstract boolean valuesEqual(T value1, T value2);
	
	private final HashSet<Entry> internalSet = new HashSet<>();
	
	private class Entry {
		private final T value;
		private final int hash;
		
		public Entry(final T value) {
			this.value = value;
			this.hash = AltSet.this.hashCode(value);
		}
		
		@Override
		public int hashCode() {
			return hash;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(@Nullable final Object obj) {
			if (!(obj instanceof AltSet.Entry))
				return false;
			final AltSet<T>.Entry other = (AltSet<T>.Entry) obj;
			return hash == other.hash && valuesEqual(value, other.value);
		}
	}
	
	@Override
	public boolean add(final T e) {
		return internalSet.add(new Entry(e));
	}
	
	@Override
	public int size() {
		return internalSet.size();
	}
	
	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			Iterator<Entry> entryIterator = internalSet.iterator();
			
			@Override
			public boolean hasNext() {
				return entryIterator.hasNext();
			}
			
			@Override
			public T next() {
				return entryIterator.next().value;
			}
		};
	}
	
}

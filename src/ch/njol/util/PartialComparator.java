package ch.njol.util;

public interface PartialComparator<T> {
	
	public PartialRelation compare(T t1, T t2);
	
}

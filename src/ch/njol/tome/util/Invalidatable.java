package ch.njol.tome.util;

public interface Invalidatable extends Watchable {
	
	public boolean isValid();
	
	// TODO add listener methods for invalidation only?
	
}

package ch.njol.tome.util;

/**
 * Objects of this type may become invalid. On becoming invalid, they call their listeners' {@link ModificationListener#onModification(Watchable) onModification} one last time, and then remove all their listeners.
 */
public interface Invalidatable extends Watchable {
	
	public boolean isValid();
	
	// TODO add listener methods for invalidation only?
	
}

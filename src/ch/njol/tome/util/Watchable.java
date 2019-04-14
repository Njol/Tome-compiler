package ch.njol.tome.util;

/**
 * Objects of this type may change, and can inform listeners of that fact.
 */
public interface Watchable {
	
	/**
	 * Registers a listener to be notified when this object changes.
	 * 
	 * @param listener
	 */
	void addModificationListener(ModificationListener listener);
	
	/**
	 * Removes a listener from this object. Useful if the listener itself became invalid.
	 * 
	 * @param listener
	 */
	void removeModificationListener(ModificationListener listener);
	
}

package ch.njol.tome.common;

/**
 * Object of this type may become invalid, and can inform listeners of that fact.
 * <p>
 * An invalid object may or may not be able to become valid again
 */
public interface Invalidatable {
	
//	// TODO actually this method should probably not be part of this interface
//	void invalidate();
	
	/**
	 * @return Whether this object is valid
	 */
	boolean isValid();
	
	/**
	 * Registers a listener to be notified when this object becomes invalid.
	 * 
	 * @param listener
	 */
	void registerInvalidateListener(InvalidateListener listener);
	
	/**
	 * Removes a listener from this object. Useful if the listener itself became invalid.
	 * 
	 * @param listener
	 */
	void removeInvalidateListener(InvalidateListener listener);
	
}

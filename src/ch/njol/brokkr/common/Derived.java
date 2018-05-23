package ch.njol.brokkr.common;

/**
 * Objects of this type can be derived from other objects and may be deleted to save memory or automatically recreated when any source object is modified or otherwise {@link Invalidatable#invalidate() invalidated}.
 */
public interface Derived extends Invalidatable, InvalidateListener {
	
}

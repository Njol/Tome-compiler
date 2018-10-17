package ch.njol.tome.util;

/**
 * Objects of this type can be derived from other objects and may be deleted to save memory or automatically updated or recreated when any source object is {@link Watchable#modified() modified}.
 */
public interface Derived extends Watchable, ModificationListener {
	
}

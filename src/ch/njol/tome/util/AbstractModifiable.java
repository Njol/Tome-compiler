package ch.njol.tome.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * This class is thread-safe.
 */
public abstract class AbstractModifiable implements Modifiable {
	
	private volatile @Nullable List<ModificationListener> modificationListeners = null;
	
	@Override
	public final synchronized void addModificationListener(final ModificationListener listener) {
		List<ModificationListener> modificationListeners = this.modificationListeners;
		if (modificationListeners == null)
			modificationListeners = this.modificationListeners = new ArrayList<>(4);
		modificationListeners.add(listener);
	}
	
	@Override
	public final synchronized void removeModificationListener(final ModificationListener listener) {
		final List<ModificationListener> modificationListeners = this.modificationListeners;
		if (modificationListeners != null)
			modificationListeners.removeIf(l -> l == listener); // remove by reference
	}
	
	/**
	 * Notifies listeners about a change in this object. If overridden, must call the super implementation.
	 */
	protected synchronized void modified() {
		final List<ModificationListener> modificationListeners = this.modificationListeners;
		if (modificationListeners != null) {
			// make a copy in case a listener unregisters itself on being notified
			List<ModificationListener> modificationListenersCopy = new ArrayList<>(modificationListeners);
			for (final ModificationListener l : modificationListenersCopy)
				l.onModification(this);
		}
	}
	
}

package ch.njol.tome.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

/**
 * This class is thread-safe.
 */
public abstract class AbstractWatchable implements Watchable {
	
	protected volatile @Nullable Set<ModificationListener> modificationListeners = null;
	
	@Override
	public synchronized void addModificationListener(final ModificationListener listener) {
		Set<ModificationListener> modificationListeners = this.modificationListeners;
		if (modificationListeners == null)
			modificationListeners = this.modificationListeners = Collections.newSetFromMap(new IdentityHashMap<>());
		modificationListeners.add(listener);
	}
	
	@Override
	public synchronized void removeModificationListener(final ModificationListener listener) {
		final Set<ModificationListener> modificationListeners = this.modificationListeners;
		if (modificationListeners != null)
			modificationListeners.remove(listener);
	}
	
	/**
	 * Notifies listeners about a change in this object. If overridden, must call the super implementation.
	 */
	protected synchronized void modified() {
		final Set<ModificationListener> modificationListeners = this.modificationListeners;
		if (modificationListeners != null) {
			// make a copy in case a listener unregisters itself on being notified
			final Set<ModificationListener> modificationListenersCopy = new HashSet<>(modificationListeners);
			for (final ModificationListener l : modificationListenersCopy)
				l.onModification(this);
		}
	}
	
}

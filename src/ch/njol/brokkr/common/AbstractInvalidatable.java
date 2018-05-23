package ch.njol.brokkr.common;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * This class is thread-safe.
 */
public abstract class AbstractInvalidatable implements Invalidatable {
	
	private volatile boolean valid = true;
	
	private volatile @Nullable List<InvalidateListener> invalidateListeners = null;
	
	@Override
	public final synchronized void registerInvalidateListener(final InvalidateListener listener) {
		if (!valid) {
			listener.onInvalidate(this);
			return;
		}
		List<InvalidateListener> invalidateListeners = this.invalidateListeners;
		if (invalidateListeners == null)
			invalidateListeners = this.invalidateListeners = new ArrayList<>();
		invalidateListeners.add(listener);
	}
	
	@Override
	public final synchronized void removeInvalidateListener(final InvalidateListener listener) {
		final List<InvalidateListener> invalidateListeners = this.invalidateListeners;
		if (invalidateListeners != null)
			invalidateListeners.removeIf(l -> l == listener); // remove by reference
	}
	
	/**
	 * Invalidates this AbstractInvalidatable. If overridden, must call the super implementation.
	 */
	protected synchronized void invalidate() {
		if (!valid)
			return;
		valid = false;
		final List<InvalidateListener> invalidateListeners = this.invalidateListeners;
		if (invalidateListeners != null) {
			this.invalidateListeners = null; // clear references
			for (final InvalidateListener l : invalidateListeners)
				l.onInvalidate(this);
		}
	}
	
	@Override
	public final boolean isValid() {
		return valid;
	}
	
}

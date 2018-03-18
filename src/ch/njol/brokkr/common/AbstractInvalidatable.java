package ch.njol.brokkr.common;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractInvalidatable implements Invalidatable {
	
	private boolean valid = true;
	
	private final List<InvalidateListener> invalidateListeners = new ArrayList<>();
	
	@Override
	public final void registerInvalidateListener(final InvalidateListener listener) {
		assert valid;
		invalidateListeners.add(listener);
	}
	
	@Override
	public final void removeInvalidateListener(final InvalidateListener listener) {
		assert valid;
		invalidateListeners.removeIf(l -> l == listener); // remove by reference
	}
	
	@Override
	public final void invalidate() {
		if (!valid)
			return;
		valid = false;
		for (final InvalidateListener l : invalidateListeners)
			l.onInvalidate(this);
		// clear references
		invalidateListeners.clear();
		invalidateInternal();
	}
	
	protected void invalidateInternal() {}
	
	@Override
	public final boolean isValid() {
		return valid;
	}
	
}

package ch.njol.tome.util;

public abstract class AbstractInvalidatable extends AbstractWatchable implements Invalidatable {
	
	private volatile boolean valid = true;
	
	@Override
	public boolean isValid() {
		return valid;
	}
	
	protected void invalidate() {
		valid = false;
		modified();
		modificationListeners = null;
	}
	
	@Override
	public synchronized void addModificationListener(ModificationListener listener) {
		if (!valid) {
			listener.onModification(this);
			return;
		}
		super.addModificationListener(listener);
	}
	
}

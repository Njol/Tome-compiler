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
	}
	
}

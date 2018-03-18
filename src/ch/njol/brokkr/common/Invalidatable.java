package ch.njol.brokkr.common;

public interface Invalidatable {
	
	void invalidate();
	
	boolean isValid();
	
	void registerInvalidateListener(InvalidateListener listener);
	
	void removeInvalidateListener(InvalidateListener listener);
	
}

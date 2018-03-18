package ch.njol.brokkr.common;

/**
 * A type which can be viewed in a hover info popup, e.g. an attribute or variable.
 * <p>
 * This interface is likely only implemented by IR classes, as ASTElement has a similar function which also takes a Token instead (and usually redirects to this interface).
 */
public interface HoverInfo {
	
	// TODO should return HTML or such in the future
	public String hoverInfo();
	
}

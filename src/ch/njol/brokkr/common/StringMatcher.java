package ch.njol.brokkr.common;

@FunctionalInterface
public interface StringMatcher {
	
	public boolean matches(String string);
	
}

package ch.njol.tome.util;

@FunctionalInterface
public interface StringMatcher {
	
	public boolean matches(String string);
	
}

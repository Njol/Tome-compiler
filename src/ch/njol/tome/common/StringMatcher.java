package ch.njol.tome.common;

@FunctionalInterface
public interface StringMatcher {
	
	public boolean matches(String string);
	
}

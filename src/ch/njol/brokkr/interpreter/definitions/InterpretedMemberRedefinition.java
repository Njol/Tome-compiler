package ch.njol.brokkr.interpreter.definitions;

public interface InterpretedMemberRedefinition {
	
	/**
	 * @return The name of this member, as of this (re)definition.
	 */
	String name();
	
	InterpretedMemberDefinition definition();
	
}

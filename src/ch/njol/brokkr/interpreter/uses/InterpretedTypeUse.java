package ch.njol.brokkr.interpreter.uses;

/**
 * A type use is any use of a type apart from its own definition.
 * TODO A fully defined type use can also be an object - how? maybe make another (Java) type for those?
 */
public interface InterpretedTypeUse {
	
	public boolean equalsType(InterpretedTypeUse other);
	
	public boolean isSubtypeOfOrEqual(InterpretedTypeUse other);
	
	public boolean isSupertypeOfOrEqual(InterpretedTypeUse other);
	
}

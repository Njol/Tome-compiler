package ch.njol.brokkr.interpreter.definitions;

public interface InterpretedResultRedefinition extends InterpretedVariableRedefinition {

	@Override
	InterpretedResultDefinition definition();
	
}

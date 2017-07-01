package ch.njol.brokkr.interpreter;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.definitions.InterpretedVariableDefinition;

public class InterpreterContext {
	
	public boolean isReturning = true;
	
	private final @Nullable InterpretedObject thisObject;
	
	private final Map<InterpretedVariableDefinition, @Nullable InterpretedObject> localVariableValues = new HashMap<>();
	
	public InterpreterContext(final InterpretedObject thisObject) {
		this.thisObject = thisObject;
	}
	
	public InterpretedObject getThisObject() {
		final InterpretedObject thisObject = this.thisObject;
		if (thisObject == null)
			throw new InterpreterException("use of 'this' in static method, or call of instance method as static one");
		return thisObject;
	}
	
	public @Nullable InterpretedObject getLocalVariableValue(final InterpretedVariableDefinition localVar) {
		if (!localVariableValues.containsKey(localVar))
			throw new InterpreterException("use of unset local variable");
		return localVariableValues.get(localVar);
	}
	
	public void setLocalVariableValue(final InterpretedVariableDefinition localVar, final InterpretedObject value) {
		localVariableValues.put(localVar, value);
	}
	
	public void defineLocalVariable(final InterpretedVariableDefinition localVar) {
		localVariableValues.put(localVar, null);
	}
	
	public void defineLocalVariable(final InterpretedVariableDefinition localVar, final @Nullable InterpretedObject value) {
		localVariableValues.put(localVar, value);
	}
	
}

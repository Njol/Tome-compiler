package ch.njol.brokkr.interpreter;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ir.definitions.IRVariableDefinition;

public class InterpreterContext {
	
	public boolean isReturning = true;
	
	private final @Nullable InterpretedObject thisObject;
	
	private final Map<IRVariableDefinition, InterpretedObject> localVariableValues = new HashMap<>();
	
	public InterpreterContext(final InterpretedObject thisObject) {
		this.thisObject = thisObject;
	}
	
	public InterpretedObject getThisObject() {
		final InterpretedObject thisObject = this.thisObject;
		if (thisObject == null)
			throw new InterpreterException("use of 'this' in static method, or call of instance method as static one");
		return thisObject;
	}
	
	public InterpretedObject getLocalVariableValue(final IRVariableDefinition localVar) {
		final InterpretedObject val = localVariableValues.get(localVar);
		if (val == null)
			throw new InterpreterException("use of undefined local variable " + localVar);
		return val;
	}
	
	public void setLocalVariableValue(final IRVariableDefinition localVar, final InterpretedObject value) {
		if (!localVariableValues.containsKey(localVar))
			throw new InterpreterException("Use of undefined local variable " + localVar);
		localVariableValues.put(localVar, value);
	}
	
	public void defineLocalVariable(final IRVariableDefinition localVar) {
		localVariableValues.put(localVar, new InterpretedNullConstant());
	}
	
	public void defineLocalVariable(final IRVariableDefinition localVar, final @Nullable InterpretedObject value) {
		localVariableValues.put(localVar, value == null ? new InterpretedNullConstant() : value);
	}
	
	// FIXME actually use this
	// TODO alternatively, could create a new InterpreterContext for each scope that inherits local variables
	public void undefineLocalVariable(final IRVariableDefinition localVar) {
		localVariableValues.remove(localVar); // doesn't check for existence, as a variable isn't necessarily defined in all paths until it gets out of scope
	}
	
}

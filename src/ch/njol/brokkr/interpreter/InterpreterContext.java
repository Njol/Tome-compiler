package ch.njol.brokkr.interpreter;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.definitions.IRAttributeRedefinition;
import ch.njol.brokkr.ir.definitions.IRVariableDefinition;

// TODO add type information to this? (i.e. allow looking up types by module + name)
public class InterpreterContext {
	
	/**
	 * Whether a return statement has been reached. If this is set, execution of statements should go back to the most recent method call. Result values will be stored in the
	 * topmost context for the call.
	 * TODO can this be made better (i.e. with direct recursion or such)?
	 */
	public boolean isReturning = true;
	
	public final IRContext irContext;
	
	private final @Nullable InterpreterContext parent;
	private final @Nullable InterpretedNormalObject thisObject;
	
	/**
	 * Local variables defined in this scope. The value of a variable is null if it has been defined but not assigned yet.
	 */
	private final Map<IRVariableDefinition, @Nullable InterpretedObject> localVariableValues = new HashMap<>();
	
	/**
	 * Creates a completely new context. // TODO add type information? or add type information later (useful for scoped types)?
	 * 
	 * @param thisObject
	 */
	public InterpreterContext(final IRContext irContext, final @Nullable InterpretedNormalObject thisObject) { // FIXME why normal object?
		this.irContext = irContext;
		parent = null;
		this.thisObject = thisObject;
	}
	
	/**
	 * Creates a new context that inherits from the given context. Variables and types defined in this new context will not be defined in the parent scope, but will be local to
	 * this scope.
	 * 
	 * @param parent
	 */
	public InterpreterContext(final InterpreterContext parent) {
		irContext = parent.irContext;
		this.parent = parent;
		thisObject = parent.thisObject;
	}
	
	public IRAttributeRedefinition getAttributebyName(final String module, final String type, final String attribute) throws InterpreterException {
		final IRAttributeRedefinition attr = irContext.getTypeDefinition(module, type).getAttributeByName(attribute);
		if (attr == null)
			throw new InterpreterException("Cannot find attribute " + attribute + " in the type " + module + "." + type);
		return attr;
	}
	
	/**
	 * @return The value of 'this' in this context
	 * @throws InterpreterException If this is a static context (i.e. if 'this' is not defined)
	 */
	public InterpretedNormalObject getThisObject() throws InterpreterException {
		final InterpretedNormalObject thisObject = this.thisObject;
		if (thisObject == null)
			throw new InterpreterException("use of 'this' in static method, or call of instance method as static one");
		return thisObject;
	}
	
	/**
	 * @param localVar
	 * @return
	 * @throws InterpreterException If the local variable has not been defined in this scope or any parent, or if it has not been assigned a value yet.
	 */
	public InterpretedObject getLocalVariableValue(final IRVariableDefinition localVar) throws InterpreterException {
		final InterpretedObject val = localVariableValues.get(localVar);
		if (val == null) { // not defined or not set yet
			if (parent != null)
				return parent.getLocalVariableValue(localVar); // will cause an error for unset variables too, as they cannot be defined in the parent score
			throw new InterpreterException("use of undefined/unassigned local variable " + localVar);
		}
		return val;
	}
	
	/**
	 * Sets a previously defined local variable's value.
	 * 
	 * @param localVar The variable
	 * @param value The new value of the variable
	 * @throws InterpreterException
	 */
	public void setLocalVariableValue(final IRVariableDefinition localVar, final InterpretedObject value) throws InterpreterException {
		if (!localVariableValues.containsKey(localVar)) {
			if (parent != null) {
				parent.setLocalVariableValue(localVar, value);
				return;
			}
			throw new InterpreterException("Use of undefined local variable " + localVar);
		}
		localVariableValues.put(localVar, value);
	}
	
	/**
	 * Defines a local variable without an initial value.
	 * 
	 * @param localVar The variable
	 */
	public void defineLocalVariable(final IRVariableDefinition localVar) {
		// TODO check for definition in parent scope?
		localVariableValues.put(localVar, null);
	}
	
	/**
	 * Defines a local variable, optionally with an initial value.
	 * 
	 * @param localVar The variable
	 * @param value Initial value of the variable, or null to keep the variable unassigned for now
	 */
	public void defineLocalVariable(final IRVariableDefinition localVar, final @Nullable InterpretedObject value) {
		localVariableValues.put(localVar, value);
	}
	
	// FIXME actually use this
	// TODO alternatively, could create a new InterpreterContext for each scope that inherits local variables
	public void undefineLocalVariable(final IRVariableDefinition localVar) {
		localVariableValues.remove(localVar); // doesn't check for existence, as a variable isn't necessarily defined in all paths until it gets out of scope
	}
	
}

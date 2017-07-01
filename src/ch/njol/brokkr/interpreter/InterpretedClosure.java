package ch.njol.brokkr.interpreter;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeImplementation;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeClassDefinition;

public abstract class InterpretedClosure implements InterpretedAttributeDefinition, InterpretedAttributeImplementation, InterpretedObject {
	
	private final List<InterpretedParameter> parameters;
	private final List<InterpretedResult> results;
	private final boolean isModifying;
	
	public InterpretedClosure(final List<InterpretedParameter> parameters, final List<InterpretedResult> results, final boolean isModifying) {
		this.parameters = parameters;
		this.results = results;
		this.isModifying = isModifying;
	}
	
	@Override
	public List<InterpretedParameter> parameters() {
		return parameters;
	}
	
	@Override
	public List<InterpretedResult> results() {
		return results;
	}
	
	@Override
	public boolean isModifying() {
		return isModifying;
	}
	
	@Override
	public boolean isVariable() {
		return false;
	}
	
	@Override
	public String name() {
		return "<closure>";
	}
	
	@Override
	public InterpretedAttributeDefinition definition() {
		return this;
	}
	
	@Override
	public InterpretedObject interpretImplementation(final InterpretedObject thisObject, final Map<InterpretedParameter, InterpretedObject> arguments, final boolean allResults) {
		return interpret(arguments);
	}
	
	public abstract InterpretedObject interpret(Map<InterpretedParameter, InterpretedObject> arguments);
	
	// this is not actually an attribute - maybe make another interface?
	// TODO think about this class more thoroughly
	@Override
	public boolean equalsAttribute(final InterpretedAttributeDefinition other) {
		return false;
	}
	
	public final static class InterpretedClosureClass implements InterpretedNativeClassDefinition {

		public InterpretedClosureClass(InterpretedClosure interpretedClosure) {
			// TODO Auto-generated constructor stub
		}

		@Override
		public @Nullable InterpretedAttributeImplementation getAttributeImplementation(InterpretedAttributeDefinition definition) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	@Override
	public InterpretedNativeClassDefinition nativeClass() {
		return new InterpretedClosureClass(this);
	}
	
}

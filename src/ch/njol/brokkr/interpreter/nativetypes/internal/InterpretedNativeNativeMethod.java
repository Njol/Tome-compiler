package ch.njol.brokkr.interpreter.nativetypes.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ast.ElementPart;
import ch.njol.brokkr.interpreter.InterpretedError;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedAttributeImplementation;
import ch.njol.brokkr.interpreter.definitions.InterpretedMemberRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedParameterDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedParameterRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedResultDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedResultRedefinition;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeObject;
import ch.njol.brokkr.interpreter.uses.InterpretedSimpleTypeUse;
import ch.njol.brokkr.interpreter.uses.InterpretedTypeUse;

public class InterpretedNativeNativeMethod implements InterpretedAttributeImplementation, InterpretedAttributeDefinition {
	
	private final Method method;
	private final List<InterpretedParameterRedefinition> parameters = new ArrayList<>();
	private final InterpretedResultRedefinition result;
	private final String name;
	
	@SuppressWarnings({"null", "unchecked"})
	public InterpretedNativeNativeMethod(final Method method, final String name) {
		this.method = method;
		this.name = name;
		final Class<?>[] parameterTypes = method.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++)
			parameters.add(new Parameter(i, InterpretedNativeSimpleNativeClass.get((Class<? extends InterpretedNativeObject>) parameterTypes[i])));
		result = new Result("result", InterpretedNativeSimpleNativeClass.get((Class<? extends InterpretedNativeObject>) method.getReturnType()));
	}
	
	private class Parameter implements InterpretedParameterDefinition {
		
		private final int index;
		private final InterpretedNativeSimpleNativeClass type;
		
		public Parameter(final int index, final InterpretedNativeSimpleNativeClass type) {
			this.index = index;
			this.type = type;
		}
		
		@Override
		public String name() {
			return "" + index;
		}
		
		@Override
		public InterpretedTypeUse type() {
			return new InterpretedSimpleTypeUse(type);
		}

		@Override
		public @Nullable InterpretedObject defaultValue(InterpreterContext context) {
			return null; // Java has no default parameter values, though they could be added via annotations
		}
		
	}
	
	private class Result implements InterpretedResultDefinition {
		
		private final String name;
		private final InterpretedNativeSimpleNativeClass type;
		
		public Result(final String name, final InterpretedNativeSimpleNativeClass type) {
			this.name = name;
			this.type = type;
		}
		
		@Override
		public String name() {
			return name;
		}
		
		@Override
		public InterpretedTypeUse type() {
			return new InterpretedSimpleTypeUse(type);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public InterpretedTypeUse targetType() {
		return new InterpretedSimpleTypeUse(InterpretedNativeSimpleNativeClass.get((Class<? extends InterpretedNativeObject>) method.getDeclaringClass()));
	}
	
	@Override
	public List<InterpretedParameterRedefinition> parameters() {
		return parameters;
	}
	
	@Override
	public List<InterpretedResultRedefinition> results() {
		return Collections.singletonList(result);
	}
	
	@Override
	public List<InterpretedError> errors() {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public boolean isModifying() {
		return true; // TODO
	}
	
	@Override
	public boolean isVariable() {
		return false;
	}
	
	@Override
	public @Nullable InterpretedObject interpretImplementation(final InterpretedObject thisObject, final Map<InterpretedParameterDefinition, InterpretedObject> arguments, final boolean allResults) {
		assert !allResults;
		final Object[] args = new Object[method.getParameterCount()];
		for (final Entry<InterpretedParameterDefinition, InterpretedObject> e : arguments.entrySet()) {
			args[Integer.parseInt(e.getKey().name())] = e.getValue(); // TODO native params have names if defined in Brokkr code (or are those redefinitions?)
		}
		try {
			final InterpretedObject o = (InterpretedObject) method.invoke(thisObject, args);
			assert o != null : method;
			return o;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String name() {
		return name;
	}
	
	@Override
	public @Nullable ElementPart getLinked() {
		return null; // TODO find declaration in Brokkr code
	}
	
	@Override
	public int hashCode() {
		return method.hashCode();
	}
	
	@Override
	public boolean equals(@Nullable final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final InterpretedNativeNativeMethod other = (InterpretedNativeNativeMethod) obj;
		return method.equals(other.method);
	}
	
	@Override
	public boolean equalsMember(final InterpretedMemberRedefinition other) {
		if (getClass() != other.getClass())
			return false;
		return method.equals(((InterpretedNativeNativeMethod) other).method);
	}
	
}

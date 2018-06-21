package ch.njol.brokkr.ir.nativetypes.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTElementPart;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeObject;
import ch.njol.brokkr.ir.AbstractIRElement;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.IRError;
import ch.njol.brokkr.ir.definitions.IRAttributeDefinition;
import ch.njol.brokkr.ir.definitions.IRAttributeImplementation;
import ch.njol.brokkr.ir.definitions.IRAttributeRedefinition;
import ch.njol.brokkr.ir.definitions.IRMemberRedefinition;
import ch.njol.brokkr.ir.definitions.IRParameterDefinition;
import ch.njol.brokkr.ir.definitions.IRParameterRedefinition;
import ch.njol.brokkr.ir.definitions.IRResultDefinition;
import ch.njol.brokkr.ir.definitions.IRResultRedefinition;
import ch.njol.brokkr.ir.definitions.IRTypeDefinition;
import ch.njol.brokkr.ir.expressions.IRExpression;
import ch.njol.brokkr.ir.uses.IRSimpleTypeUse;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public class IRNativeMethod extends AbstractIRElement implements IRAttributeImplementation, IRAttributeDefinition {
	
	private final Method method;
	private final List<IRParameterRedefinition> parameters = new ArrayList<>();
	private final IRResultRedefinition result;
	private final String name;
	private final IRContext irContext;
	
	public IRNativeMethod(final Method method, final String name, final IRContext irContext) {
		this.method = method;
		this.name = name;
		this.irContext = irContext;
		final @NonNull Class<?>[] parameterTypes = method.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++)
			parameters.add(new Parameter(i, IRNativeTypeClassDefinition.get(irContext, checkInterpretedNativeObjectClass(parameterTypes[i]))));
		result = new Result("result", IRNativeTypeClassDefinition.get(irContext, checkInterpretedNativeObjectClass(method.getReturnType())));
	}
	
	@SuppressWarnings("unchecked")
	private final static Class<? extends InterpretedNativeObject> checkInterpretedNativeObjectClass(@Nullable final Class<?> c) {
		if (c != null && InterpretedNativeObject.class.isAssignableFrom(c))
			return (Class<? extends InterpretedNativeObject>) c;
		throw new RuntimeException("Not a subtype of InterpretedNativeObject: " + c);
	}
	
	private class Parameter extends AbstractIRElement implements IRParameterDefinition {
		
		private final int index;
		private final IRNativeTypeClassDefinition type;
		
		public Parameter(final int index, final IRNativeTypeClassDefinition type) {
			this.index = index;
			this.type = type;
		}
		
		@Override
		public String name() {
			return "" + index;
		}
		
		@Override
		public IRTypeUse type() {
			return new IRSimpleTypeUse(type);
		}
		
		@Override
		public @Nullable InterpretedObject defaultValue(final InterpreterContext context) {
			return null; // Java has no default parameter values, though they could be added via annotations
		}
		
		@Override
		public IRAttributeRedefinition attribute() {
			return IRNativeMethod.this;
		}
		
		@Override
		public IRContext getIRContext() {
			return irContext;
		}
		
		@Override
		public int hashCode() {
			return parameterHashCode();
		}
		
		@Override
		public boolean equals(@Nullable final Object other) {
			return other instanceof IRParameterRedefinition ? equalsParameter((IRParameterRedefinition) other) : false;
		}
		
		@Override
		public String hoverInfo() {
			return type + " " + name;
		}
		
	}
	
	private class Result extends AbstractIRElement implements IRResultDefinition {
		
		private final String name;
		private final IRNativeTypeClassDefinition type;
		
		public Result(final String name, final IRNativeTypeClassDefinition type) {
			this.name = name;
			this.type = type;
		}
		
		@Override
		public String name() {
			return name;
		}
		
		@Override
		public IRTypeUse type() {
			return new IRSimpleTypeUse(type);
		}
		
		@Override
		public IRAttributeRedefinition attribute() {
			return IRNativeMethod.this;
		}
		
		@Override
		public IRContext getIRContext() {
			return irContext;
		}
		
		@Override
		public String hoverInfo() {
			return type + " " + name;
		}
		
		@Override
		public @Nullable IRExpression defaultValue() {
			return null;
		}
		
	}
	
	@Override
	public List<IRParameterRedefinition> parameters() {
		return parameters;
	}
	
	@Override
	public List<IRResultRedefinition> results() {
		return Collections.singletonList(result);
	}
	
	@Override
	public List<IRError> errors() {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public boolean isModifying() {
		return true; // TODO annotation? definition in Brokkr code? or both just to make sure?
	}
	
	@Override
	public boolean isVariable() {
		return false;
	}
	
	@Override
	public boolean isStatic() {
		return Modifier.isStatic(method.getModifiers());
	}
	
	@Override
	public @Nullable InterpretedObject interpretImplementation(final InterpretedObject thisObject, final Map<IRParameterDefinition, InterpretedObject> arguments, final boolean allResults) {
		assert !allResults;
		final Object[] args = new Object[method.getParameterCount()];
		for (final Entry<IRParameterDefinition, InterpretedObject> e : arguments.entrySet()) {
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
	public String documentation() {
		return "Native method " + declaringType() + "." + name;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IRTypeDefinition declaringType() {
		return IRNativeTypeClassDefinition.get(irContext, (Class<? extends InterpretedNativeObject>) method.getDeclaringClass());
	}
	
	@Override
	public @Nullable ASTElementPart getLinked() {
		return null; // TODO find declaration in Brokkr code
	}
	
	@Override
	public int hashCode() {
		return memberHashCode();
	}
	
	@Override
	public boolean equals(@Nullable final Object other) {
		return other instanceof IRMemberRedefinition ? equalsMember((IRMemberRedefinition) other) : false;
	}
	
	@Override
	public IRContext getIRContext() {
		return irContext;
	}
	
	@Override
	public @Nullable IRAttributeRedefinition parentRedefinition() {
		return null;
	}
	
}

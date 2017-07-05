package ch.njol.brokkr.interpreter.definitions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ast.ElementPart;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalParameter;
import ch.njol.brokkr.compiler.ast.Interfaces.TypeDeclaration;
import ch.njol.brokkr.compiler.ast.Members.Constructor;
import ch.njol.brokkr.compiler.ast.Members.ConstructorFieldParameter;
import ch.njol.brokkr.compiler.ast.Members.SimpleParameter;
import ch.njol.brokkr.compiler.ast.TopLevelElements.ClassDeclaration;
import ch.njol.brokkr.interpreter.InterpretedError;
import ch.njol.brokkr.interpreter.InterpretedNormalObject;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedNativeBrokkrClass;
import ch.njol.brokkr.interpreter.uses.InterpretedSimpleClassUse;
import ch.njol.brokkr.interpreter.uses.InterpretedSimpleTypeUse;
import ch.njol.brokkr.interpreter.uses.InterpretedTypeUse;

public class InterpretedBrokkrConstructor implements InterpretedAttributeDefinition, InterpretedAttributeImplementation {
	
	private final Constructor constructor;
	private final List<InterpretedParameterRedefinition> parameters;
//	private final List<InterpretedError> errors = new ArrayList<>();
	private final InterpretedNativeBrokkrClass type;
	
	// TODO can a constructor override another attribute? then overridden parameters need to be considered too.
	@SuppressWarnings("null")
	public InterpretedBrokkrConstructor(final Constructor constructor) {
		this.constructor = constructor;
		parameters = constructor.parameters.stream().map(p -> p.interpreted(this)).collect(Collectors.toList());
		type = constructor.getParentOfType(ClassDeclaration.class).interpreted();
//		for (ErrorDeclaration e : constructor.errors)
//			errors.add(e.interpreted());
	}
	
	// TODO shouldn't this be more general?
	@SuppressWarnings("null")
	@Override
	public InterpretedTypeUse targetType() {
		return new InterpretedSimpleTypeUse(constructor.getParentOfType(TypeDeclaration.class).interpreted().nativeClass());
	}
	
	@Override
	public List<InterpretedParameterRedefinition> parameters() {
		return parameters;
	}
	
	@Override
	public List<InterpretedResultRedefinition> results() {
		return Collections.singletonList(new ImplicitConstructorResult());
	}
	
	private final class ImplicitConstructorResult implements InterpretedResultDefinition {
		@Override
		public String name() {
			return "result";
		}
		
		@Override
		public InterpretedTypeUse type() {
			return new InterpretedSimpleTypeUse(type);
		}
	}
	
	@Override
	public List<InterpretedError> errors() {
		return Collections.EMPTY_LIST; // currently, constructors cannot throw errors
//		return errors;
	}
	
	@Override
	public boolean isModifying() {
		return true; // TODO which one is better? or remove this completely?
	}
	
	@Override
	public boolean isVariable() {
		return false; // would make no sense
	}
	
	@SuppressWarnings("null")
	@Override
	public String name() {
		return constructor.name.word;
	}
	
	@Override
	public @Nullable ElementPart getLinked() {
		return constructor;
	}
	
	@SuppressWarnings("null")
	@Override
	public InterpretedObject interpretImplementation(final InterpretedObject ignored, final Map<InterpretedParameterDefinition, InterpretedObject> arguments, final boolean allResults) {
		final InterpretedNormalObject thisObject = new InterpretedNormalObject(new InterpretedSimpleClassUse(type));
		final InterpreterContext localContext = new InterpreterContext(thisObject);
		for (final InterpretedParameterRedefinition p : parameters) {
			final InterpretedParameterDefinition pd = p.definition();
			InterpretedObject value = arguments.get(pd);
			if (p instanceof InterpretedBrokkrConstructorFieldParameter) {
				thisObject.setAttributeValue(((InterpretedBrokkrConstructorFieldParameter) p).field.definition(), value);
				// don't set local variable (TODO or maybe do? )
			} else {
				final SimpleParameter sp = (SimpleParameter) p;
				if (value == null && sp.defaultValue != null)
					value = sp.defaultValue.interpret(localContext);
				if (value == null)
					throw new InterpreterException("Parameter '" + p.name() + "' is not defined");
				localContext.defineLocalVariable(pd, value);
			}
		}
		constructor.body.interpret(localContext);
		return thisObject;
	}
	
	@Override
	public boolean equalsMember(final InterpretedMemberRedefinition other) {
		if (getClass() != other.getClass())
			return false;
		final InterpretedBrokkrConstructor c = (InterpretedBrokkrConstructor) other;
		return c.constructor == constructor; // TODO correct?
	}
	
}

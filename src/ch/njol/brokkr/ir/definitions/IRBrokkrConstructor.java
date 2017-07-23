package ch.njol.brokkr.ir.definitions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTElementPart;
import ch.njol.brokkr.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.brokkr.ast.ASTMembers.ASTConstructor;
import ch.njol.brokkr.ast.ASTMembers.ASTSimpleParameter;
import ch.njol.brokkr.ast.ASTTopLevelElements.ASTClassDeclaration;
import ch.njol.brokkr.interpreter.InterpretedNormalObject;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.IRError;
import ch.njol.brokkr.ir.uses.IRSimpleClassUse;
import ch.njol.brokkr.ir.uses.IRSimpleTypeUse;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public class IRBrokkrConstructor implements IRAttributeDefinition, IRAttributeImplementation {
	
	private final ASTConstructor constructor;
	private final List<IRParameterRedefinition> parameters;
//	private final List<IRError> errors = new ArrayList<>();
	private final IRBrokkrClass type;
	
	// TODO can a constructor override another attribute? then overridden parameters need to be considered too.
	@SuppressWarnings("null")
	public IRBrokkrConstructor(final ASTConstructor constructor) {
		this.constructor = constructor;
		parameters = constructor.parameters.stream().map(p -> p.interpreted(this)).collect(Collectors.toList());
		type = constructor.getParentOfType(ASTClassDeclaration.class).getIR();
//		for (ErrorDeclaration e : constructor.errors)
//			errors.add(e.interpreted());
	}
	
	// TODO shouldn't this be more general?
	@SuppressWarnings("null")
	@Override
	public IRTypeUse targetType() {
		return new IRSimpleTypeUse(constructor.getParentOfType(ASTTypeDeclaration.class).getIR().nativeClass());
	}
	
	@Override
	public List<IRParameterRedefinition> parameters() {
		return parameters;
	}
	
	@Override
	public List<IRResultRedefinition> results() {
		return Collections.singletonList(new ImplicitConstructorResult());
	}
	
	private final class ImplicitConstructorResult implements IRResultDefinition {
		@Override
		public String name() {
			return "result";
		}
		
		@Override
		public IRTypeUse type() {
			return new IRSimpleTypeUse(type);
		}
	}
	
	@Override
	public List<IRError> errors() {
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
	
	@Override
	public boolean isStatic() {
		return true; // static determines how the member is used, not internal variations
	}
	
	@SuppressWarnings("null")
	@Override
	public String name() {
		return constructor.name.word;
	}
	
	@Override
	public @Nullable ASTElementPart getLinked() {
		return constructor;
	}
	
	@SuppressWarnings("null")
	@Override
	public InterpretedObject interpretImplementation(final InterpretedObject ignored, final Map<IRParameterDefinition, InterpretedObject> arguments, final boolean allResults) {
		final InterpretedNormalObject thisObject = new InterpretedNormalObject(new IRSimpleClassUse(type));
		final InterpreterContext localContext = new InterpreterContext(thisObject);
		for (final IRParameterRedefinition p : parameters) {
			final IRParameterDefinition pd = p.definition();
			InterpretedObject value = arguments.get(pd);
			if (p instanceof IRBrokkrConstructorFieldParameter) {
				thisObject.setAttributeValue(((IRBrokkrConstructorFieldParameter) p).field.definition(), value);
				// don't set local variable (TODO or maybe do? )
			} else {
				final ASTSimpleParameter sp = (ASTSimpleParameter) p;
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
	public boolean equalsMember(final IRMemberRedefinition other) {
		if (getClass() != other.getClass())
			return false;
		final IRBrokkrConstructor c = (IRBrokkrConstructor) other;
		return c.constructor == constructor; // TODO correct?
	}
	
}

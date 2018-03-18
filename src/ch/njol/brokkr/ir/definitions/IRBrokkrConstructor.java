package ch.njol.brokkr.ir.definitions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
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
import ch.njol.brokkr.ir.AbstractIRElement;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.IRError;
import ch.njol.brokkr.ir.expressions.IRExpression;
import ch.njol.brokkr.ir.uses.IRSimpleClassUse;
import ch.njol.brokkr.ir.uses.IRSimpleTypeUse;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public class IRBrokkrConstructor extends AbstractIRElement implements IRAttributeDefinition, IRAttributeImplementation {
	
	private final ASTConstructor ast;
	private final IRTypeDefinition type;
	
	// TODO can a constructor override another attribute? then overridden parameters need to be considered too.
	public IRBrokkrConstructor(final ASTConstructor constructor) {
		ast = constructor;
		final ASTClassDeclaration classDeclaration = constructor.getParentOfType(ASTClassDeclaration.class);
		type = classDeclaration != null ? classDeclaration.getIR() : new IRUnknownTypeDefinition(ast.getIRContext(), "Constructor not in a class", ast);
	}
	
	@Override
	public IRContext getIRContext() {
		return ast.getIRContext();
	}
	
	private @Nullable List<IRParameterRedefinition> parameters;
	
	@Override
	public List<IRParameterRedefinition> parameters() {
		if (parameters != null)
			return parameters;
		final List<IRParameterRedefinition> parameters = ast.parameters.stream().map(p -> p.getIR()).collect(Collectors.toList());
		this.parameters = parameters;
		return parameters;
	}
	
	@Override
	public List<IRResultRedefinition> results() {
		return Collections.singletonList(new ImplicitConstructorResult());
	}
	
	private final class ImplicitConstructorResult extends AbstractIRElement implements IRResultDefinition {
		@Override
		public String name() {
			return "result";
		}
		
		@Override
		public String hoverInfo() {
			return "result"; // TODO
		}
		
		@Override
		public IRTypeUse type() {
			return new IRSimpleTypeUse(type);
		}
		
		@Override
		public IRAttributeRedefinition attribute() {
			return IRBrokkrConstructor.this;
		}
		
		@Override
		public @Nullable IRExpression defaultValue() {
			return null; // set natively at end of constructor
		}
		
		@Override
		public IRContext getIRContext() {
			return IRBrokkrConstructor.this.getIRContext();
		}
		
	}
	
//	private final List<IRError> errors = new ArrayList<>();
	@Override
	public List<IRError> errors() {
		return Collections.EMPTY_LIST; // currently, constructors cannot throw errors // what about preconditions?
//		for (ErrorDeclaration e : constructor.errors)
//			errors.add(e.interpreted());
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
	
	@Override
	public String toString() {
		return ast.getParentOfType(ASTTypeDeclaration.class) + "." + name();
	}
	
	@Override
	public String hoverInfo() {
		return "Constructor " + declaringType() + "." + name();
	}
	
	@SuppressWarnings("null")
	@Override
	public String name() {
		return ast.name.word;
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
	public @NonNull IRTypeDefinition declaringType() {
		final ASTTypeDeclaration type = ast.getParentOfType(ASTTypeDeclaration.class);
		if (type == null)
			return new IRUnknownTypeDefinition(getIRContext(), "Internal compiler error (constructor not in type: " + this + ")", ast);
		return type.getIR();
	}
	
	@Override
	public @NonNull ASTElementPart getLinked() {
		return ast;
	}
	
	@SuppressWarnings("null")
	@Override
	public InterpretedObject interpretImplementation(final InterpretedObject ignored, final Map<IRParameterDefinition, InterpretedObject> arguments, final boolean allResults) throws InterpreterException {
		if (!(type instanceof IRBrokkrClassDefinition))
			throw new InterpreterException("Constructor not in class");
		final InterpretedNormalObject thisObject = new InterpretedNormalObject(new IRSimpleClassUse((IRBrokkrClassDefinition) type));
		final InterpreterContext localContext = new InterpreterContext(getIRContext(), thisObject);
		for (final IRParameterRedefinition p : parameters) {
			final IRParameterDefinition pd = p.definition();
			InterpretedObject value = arguments.get(pd);
			if (p instanceof IRBrokkrConstructorFieldParameter) {
				thisObject.setAttributeValue(((IRBrokkrConstructorFieldParameter) p).field.definition(), value);
				// don't set local variable (TODO or maybe do? )
			} else {
				final ASTSimpleParameter sp = (ASTSimpleParameter) p;
				if (value == null && sp.defaultValue != null)
					value = sp.defaultValue.getIR().interpret(localContext);
				if (value == null)
					throw new InterpreterException("Parameter '" + p.name() + "' is not defined");
				localContext.defineLocalVariable(pd, value);
			}
		}
		ast.body.getIR().interpret(localContext);
		return thisObject;
	}
	
}

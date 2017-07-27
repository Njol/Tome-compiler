package ch.njol.brokkr.ir.definitions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTElementPart;
import ch.njol.brokkr.ast.ASTExpressions.ASTBlock;
import ch.njol.brokkr.ast.ASTInterfaces.ASTResult;
import ch.njol.brokkr.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.brokkr.ast.ASTMembers.ASTAttributeDeclaration;
import ch.njol.brokkr.ast.ASTMembers.ASTErrorDeclaration;
import ch.njol.brokkr.ast.ASTMembers.ASTSimpleParameter;
import ch.njol.brokkr.common.MethodModifiability;
import ch.njol.brokkr.interpreter.InterpretedNormalObject;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.IRError;
import ch.njol.brokkr.ir.nativetypes.IRTuple;
import ch.njol.brokkr.ir.nativetypes.IRTuple.IRNativeTupleValueAndEntry;

public abstract class AbstractIRBrokkrAttribute implements IRAttributeRedefinition {
	
	public final ASTAttributeDeclaration declaration;
	
	public AbstractIRBrokkrAttribute(final ASTAttributeDeclaration declaration) {
		this.declaration = declaration;
		
		final IRAttributeRedefinition parent = parentRedefinition();
		if (parent == null && (declaration.hasParameterDots || declaration.hasResultDots))
			throw new InterpreterException("Use of '...' in non-overriding method");
		
	}
	
	@Override
	public String toString() {
		return declaration.getParentOfType(ASTTypeDeclaration.class) + "." + declaration.name();
	}
	
	private @Nullable List<IRParameterRedefinition> parameters = null;
	
	@SuppressWarnings("null")
	@Override
	public List<IRParameterRedefinition> parameters() {
		if (parameters == null) {
			parameters = new ArrayList<>();
			final IRAttributeRedefinition parent = parentRedefinition();
			if ((declaration.hasParameterDots || !declaration.hasParameterDefinitions) && parent != null)
				parameters.addAll(parent.parameters());
			// FIXME overridden parameters
			for (final ASTSimpleParameter p : declaration.parameters)
				parameters.add(p.interpreted(this));
		}
		return parameters;
	}
	
	private @Nullable List<IRResultRedefinition> results = null;
	
	@SuppressWarnings("null")
	@Override
	public List<IRResultRedefinition> results() {
		if (results == null) {
			results = new ArrayList<>();
			final IRAttributeRedefinition parent = parentRedefinition();
			if ((declaration.hasResultDots || !declaration.hasResultDefinitions) && parent != null)
				results.addAll(parent.results());
			// FIXME overridden results
			for (final ASTResult r : declaration.results)
				results.add(r.interpreted(this));
		}
		return results;
	}
	
	private @Nullable List<IRError> errors = null;
	
	@SuppressWarnings("null")
	@Override
	public List<IRError> errors() {
		if (errors == null) {
			errors = new ArrayList<>();
			for (final ASTErrorDeclaration e : declaration.errors)
				errors.add(e.getIR(this));
		}
		return errors;
	}
	
	@Override
	public boolean isModifying() {
		final MethodModifiability modifiability = declaration.modifiers.modifiability;
		if (modifiability != null)
			return modifiability == MethodModifiability.MODIFYING;
		final IRAttributeRedefinition parentRedefinition = parentRedefinition();
		return parentRedefinition != null ? parentRedefinition.isModifying() : false; // default modifiability is nonmodifying
	}
	
	@Override
	public boolean isVariable() {
		return declaration.modifiers.var; // TOCO check that this is only set in classes? or can this actually be defined in interfaces too? (probably not)
	}
	
	@Override
	public boolean isStatic() {
		return declaration.modifiers.isStatic;
	}
	
	@Override
	public String name() {
		return "" + declaration.name();
	}
	
	@Override
	public @NonNull IRTypeDefinition declaringType() {
		final ASTTypeDeclaration type = declaration.getParentOfType(ASTTypeDeclaration.class);
		if (type == null)
			throw new InterpreterException("Attribute not in type: " + this);
		return type.getIR();
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
	public @Nullable ASTElementPart getLinked() {
		return declaration;
	}
	
	@Override
	public @Nullable IRAttributeRedefinition parentRedefinition() {
		final IRMemberRedefinition m = declaration.modifiers.overridden.get();
		if (m instanceof IRAttributeRedefinition)
			return (IRAttributeRedefinition) m;
		return null;
	}
	
	protected @Nullable InterpretedObject interpretImplementation(final InterpretedObject thisObject, final Map<IRParameterDefinition, InterpretedObject> arguments, final boolean allResults) {
		if (!(thisObject instanceof InterpretedNormalObject))
			return null;
		final InterpreterContext localContext = new InterpreterContext((InterpretedNormalObject) thisObject); // new stack frame
		for (final @NonNull IRParameterRedefinition p : parameters()) {
			final IRParameterDefinition pd = p.definition();
			InterpretedObject value = arguments.get(pd);
			if (value == null) {
				value = p.defaultValue(localContext);
				if (value == null)
					throw new InterpreterException("Missing argument " + p);
			}
			localContext.defineLocalVariable(pd, value);
		}
		IRResultDefinition mainResult = null;
		for (final @NonNull IRResultRedefinition r : results()) {
			final IRResultDefinition rd = r.definition();
			localContext.defineLocalVariable(rd);
			if ("result".equals(r.name()))
				mainResult = rd;
		}
		final ASTBlock body = declaration.body;
		if (body == null)
			throw new InterpreterException("Missing method body in " + declaration);
		body.interpret(localContext);
		if (allResults) {
			final List<IRNativeTupleValueAndEntry> entries = new ArrayList<>();
			for (int i = 0; i < results().size(); i++) {
				final IRResultDefinition res = results().get(i).definition();
				final InterpretedObject resultValue = localContext.getLocalVariableValue(res);
				entries.add(new IRNativeTupleValueAndEntry(i, res.type(), res.name(), resultValue));
			}
			return IRTuple.newInstance(entries.stream());
		}
		if (mainResult != null)
			return localContext.getLocalVariableValue(mainResult);
		return null;
	}
	
}

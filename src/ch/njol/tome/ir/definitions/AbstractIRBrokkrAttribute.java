package ch.njol.tome.ir.definitions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.ast.ASTExpressions.ASTBlock;
import ch.njol.tome.ast.ASTInterfaces.ASTResult;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.ASTMembers.ASTAttributeDeclaration;
import ch.njol.tome.ast.ASTMembers.ASTErrorDeclaration;
import ch.njol.tome.ast.ASTMembers.ASTSimpleParameter;
import ch.njol.tome.common.MethodModifiability;
import ch.njol.tome.interpreter.InterpretedNormalObject;
import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpretedTuple;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.AbstractIRElement;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.IRError;
import ch.njol.tome.util.ASTCommentUtil;

public abstract class AbstractIRBrokkrAttribute extends AbstractIRElement implements IRAttributeRedefinition {
	
	public final ASTAttributeDeclaration ast;
	public final @Nullable IRAttributeRedefinition overridden;
	private final IRTypeDefinition declaringType;
	
	public AbstractIRBrokkrAttribute(final ASTAttributeDeclaration ast) {
		this(ast, null);
	}
	
	public AbstractIRBrokkrAttribute(final ASTAttributeDeclaration ast, @Nullable final IRAttributeRedefinition overridden) {
		this.ast = registerDependency(ast);
		assert (overridden == null) == (this instanceof IRAttributeDefinition);
		this.overridden = registerDependency(overridden);
		final ASTTypeDeclaration type = ast.getParentOfType(ASTTypeDeclaration.class);
		declaringType = registerDependency(type != null ? type.getIR() : new IRUnknownTypeDefinition(getIRContext(), "Internal compiler error (attribute not in type: " + this + ")", ast));
	}
	
	@Override
	public IRContext getIRContext() {
		return ast.getIRContext();
	}
	
	@Override
	public String toString() {
		return declaringType + "." + ast.name();
	}
	
	private @Nullable List<IRParameterRedefinition> parameters = null;
	
	@Override
	public List<IRParameterRedefinition> parameters() {
		if (parameters != null)
			return parameters;
		final List<IRParameterRedefinition> parameters = new ArrayList<>();
		if ((ast.hasParameterDots || !ast.hasParameterDefinitions) && overridden != null)
			parameters.addAll(overridden.parameters());
		// FIXME overridden parameters
		for (final ASTSimpleParameter p : ast.parameters)
			parameters.add(p.getIR());
		this.parameters = parameters;
		return parameters;
	}
	
	private @Nullable List<IRResultRedefinition> results = null;
	
	@Override
	public List<IRResultRedefinition> results() {
		if (results != null)
			return results;
		final List<IRResultRedefinition> results = new ArrayList<>();
		if ((ast.hasResultDots || !ast.hasResultDefinitions) && overridden != null)
			results.addAll(overridden.results());
		// FIXME overridden results
		for (final ASTResult r : ast.results)
			results.add(r.getIR());
		this.results = results;
		return results;
	}
	
	private @Nullable List<IRError> errors = null;
	
	@Override
	public List<IRError> errors() {
		if (errors != null)
			return errors;
		final List<IRError> errors = new ArrayList<>();
		for (final ASTErrorDeclaration e : ast.errors)
			errors.add(e.getIRError());
		this.errors = errors;
		return errors;
	}
	
	@Override
	public boolean isModifying() {
		final MethodModifiability modifiability = ast.modifiers.modifiability;
		if (modifiability != null)
			return modifiability == MethodModifiability.MODIFYING;
		final IRAttributeRedefinition parentRedefinition = parentRedefinition();
		return parentRedefinition != null ? parentRedefinition.isModifying() : false; // default modifiability is nonmodifying
	}
	
	@Override
	public boolean isVariable() {
		return ast.modifiers.var; // TOCO check that this is only set in classes? or can this actually be defined in interfaces too? (probably not)
	}
	
	@Override
	public boolean isStatic() {
		return ast.modifiers.isStatic;
	}
	
	@Override
	public String name() {
		return "" + ast.name();
	}
	
	@Override
	public String documentation() {
		final IRAttributeDefinition def = definition();
		return "Attribute " + declaringType() + "." + name()
				+ (def == this ? "" : " (defined in " + def.declaringType() + (def.name().equals(name()) ? "" : " as " + def.name()) + ")")
				+ "\n" + ASTCommentUtil.getCommentBefore(ast);
	}
	
	@Override
	public IRTypeDefinition declaringType() {
		return declaringType;
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
		return ast;
	}
	
	@Override
	public @Nullable IRAttributeRedefinition parentRedefinition() {
		return overridden;
	}
	
	protected @Nullable InterpretedObject interpretImplementation(final InterpretedObject thisObject, final Map<IRParameterDefinition, InterpretedObject> arguments, final boolean allResults) throws InterpreterException {
		if (!(thisObject instanceof InterpretedNormalObject))
			return null;
		final InterpreterContext localContext = new InterpreterContext(getIRContext(), (InterpretedNormalObject) thisObject); // new stack frame
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
		final ASTBlock body = ast.body;
		if (body == null)
			throw new InterpreterException("Missing method body in " + ast);
		body.getIR().interpret(localContext);
		if (allResults) {
//			final List<IRTupleEntryWithTypeUse> entries = new ArrayList<>();
			final List<InterpretedObject> values = new ArrayList<>();
			for (int i = 0; i < results().size(); i++) {
				final IRResultDefinition res = results().get(i).definition();
				final InterpretedObject resultValue = localContext.getLocalVariableValue(res);
//				entries.add(new IRTupleEntryWithTypeUse(i, res.type(), res.name(), resultValue));
				values.add(resultValue);
			}
			return new InterpretedTuple(allResultTypes(), values);
		}
		if (mainResult != null)
			return localContext.getLocalVariableValue(mainResult);
		return null;
	}
	
}

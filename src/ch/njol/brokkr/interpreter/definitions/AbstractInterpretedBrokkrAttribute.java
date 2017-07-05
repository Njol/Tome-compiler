package ch.njol.brokkr.interpreter.definitions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ast.ElementPart;
import ch.njol.brokkr.compiler.ast.Expressions.Block;
import ch.njol.brokkr.compiler.ast.Interfaces.Expression;
import ch.njol.brokkr.compiler.ast.Interfaces.FormalResult;
import ch.njol.brokkr.compiler.ast.Interfaces.TypeDeclaration;
import ch.njol.brokkr.compiler.ast.Members.AttributeDeclaration;
import ch.njol.brokkr.compiler.ast.Members.ErrorDeclaration;
import ch.njol.brokkr.compiler.ast.Members.SimpleParameter;
import ch.njol.brokkr.data.MethodModifiability;
import ch.njol.brokkr.interpreter.InterpretedError;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedTuple;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedTuple.InterpretedNativeTupleValueAndEntry;
import ch.njol.brokkr.interpreter.uses.InterpretedSimpleTypeUse;
import ch.njol.brokkr.interpreter.uses.InterpretedTypeUse;

public abstract class AbstractInterpretedBrokkrAttribute implements InterpretedAttributeRedefinition {
	
	public final AttributeDeclaration declaration;
	private final List<InterpretedParameterRedefinition> parameters = new ArrayList<>();
	private final List<InterpretedResultRedefinition> results = new ArrayList<>();
	private final List<InterpretedError> errors = new ArrayList<>();
	
	public AbstractInterpretedBrokkrAttribute(final AttributeDeclaration declaration) {
		this.declaration = declaration;
		
		final InterpretedAttributeRedefinition parent = parentRedefinition();
		if (parent == null && (declaration.hasParameterDots || declaration.hasResultDots))
			throw new InterpreterException("Use of '...' in non-overriding method");
		
		// parameters
		if (declaration.hasParameterDots && parent != null)
			parameters.addAll(parent.parameters());
		for (final SimpleParameter p : declaration.parameters)
			parameters.add(p.interpreted(this));
		
		// results
		if (declaration.hasResultDots && parent != null)
			results.addAll(parent.results());
		for (final FormalResult r : declaration.results)
			results.add(r.interpreted(this));
		
		for (final ErrorDeclaration e : declaration.errors)
			errors.add(e.interpreted(this));
	}
	
	@SuppressWarnings("null")
	@Override
	public InterpretedTypeUse targetType() {
		return new InterpretedSimpleTypeUse(declaration.getParentOfType(TypeDeclaration.class).interpreted());
	}
	
	@Override
	public List<InterpretedParameterRedefinition> parameters() {
		return parameters;
	}
	
	@Override
	public List<InterpretedResultRedefinition> results() {
		return results;
	}
	
	@Override
	public List<InterpretedError> errors() {
		return errors;
	}
	
	@Override
	public boolean isModifying() {
		final MethodModifiability modifiability = declaration.modifiers.modifiability;
		if (modifiability != null)
			return modifiability == MethodModifiability.MODIFYING;
		final InterpretedAttributeRedefinition parentRedefinition = parentRedefinition();
		return parentRedefinition != null ? parentRedefinition.isModifying() : false; // default modifiability is nonmodifying
	}
	
	@Override
	public boolean isVariable() {
		return declaration.modifiers.var; // TOCO check that this is only set in classes? or can this actually be defined in interfaces too? (probably not)
	}
	
	@Override
	public String name() {
		return "" + declaration.name();
	}
	
	@Override
	public @Nullable ElementPart getLinked() {
		return declaration;
	}
	
	@Override
	public @Nullable InterpretedAttributeRedefinition parentRedefinition() {
		final InterpretedMemberRedefinition m = declaration.modifiers.overridden.get();
		if (m instanceof InterpretedAttributeRedefinition)
			return (InterpretedAttributeRedefinition) m;
		return null;
	}
	
	protected @Nullable InterpretedObject interpretImplementation(final InterpretedObject thisObject, final Map<InterpretedParameterDefinition, InterpretedObject> arguments, final boolean allResults) {
		final InterpreterContext localContext = new InterpreterContext(thisObject); // new stack frame
		for (final @NonNull InterpretedParameterRedefinition p : parameters) {
			InterpretedParameterDefinition pd = p.definition();
			InterpretedObject value = arguments.get(pd);
			if (value == null) {
				value = p.defaultValue(localContext);
				if (value == null)
					throw new InterpreterException("Missing argument " + p);
			}
			localContext.defineLocalVariable(pd, value);
		}
		InterpretedResultDefinition mainResult = null;
		for (final @NonNull InterpretedResultRedefinition r : results) {
			final InterpretedResultDefinition rd = r.definition();
			localContext.defineLocalVariable(rd);
			if ("result".equals(r.name()))
				mainResult = rd;
		}
		final Block body = declaration.body;
		if (body == null)
			throw new InterpreterException("Missing method body in " + declaration);
		body.interpret(localContext);
		if (allResults) {
			final List<InterpretedNativeTupleValueAndEntry> entries = new ArrayList<>();
			for (int i = 0; i < results.size(); i++) {
				final InterpretedResultDefinition res = results.get(i).definition();
				final InterpretedObject resultValue = localContext.getLocalVariableValue(res);
				if (resultValue == null)
					throw new InterpreterException("Unset result variable " + res + " in " + declaration);
				entries.add(new InterpretedNativeTupleValueAndEntry(i, res.type(), res.name(), resultValue));
			}
			return InterpretedTuple.newInstance(entries.stream());
		}
		if (mainResult != null)
			return localContext.getLocalVariableValue(mainResult);
		return null;
	}
	
	@Override
	public boolean equalsMember(InterpretedMemberRedefinition other) {
		if (getClass() != other.getClass())
			return false;
		final AbstractInterpretedBrokkrAttribute a = (AbstractInterpretedBrokkrAttribute) other;
		return a.declaration == declaration; // TODO correct?
	}
	
}

package ch.njol.brokkr.ir.definitions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.IRError;
import ch.njol.brokkr.ir.nativetypes.IRTuple.IRTypeTuple;
import ch.njol.brokkr.ir.nativetypes.IRTuple.IRTypeTupleBuilder;
import ch.njol.brokkr.ir.uses.IRAttributeUse;
import ch.njol.brokkr.ir.uses.IRGenericTypeUse;
import ch.njol.brokkr.ir.uses.IRMemberUse;
import ch.njol.brokkr.ir.uses.IRTypeUse;
import ch.njol.brokkr.ir.uses.IRUnknownTypeUse;

/**
 * A (re)definition of an attribute,
 * which may either be the {@link IRAttributeDefinition the first definition} or a redefinition.
 * A redefinition may be an {@link IRAttributeImplementation implementation},
 * or may just change the signature (with e.g. more results or different flags.
 */
public interface IRAttributeRedefinition extends IRMemberRedefinition, IRVariableOrAttributeRedefinition {
	
	/**
	 * @return The next (re)definiton of this attribute up the chain, where the last one is a {@link IRAttributeDefinition}. If this is already a definition, this method
	 *         returns null.
	 */
	@Override
	@Nullable
	IRAttributeRedefinition parentRedefinition();
	
	/**
	 * @return The topmost definition of this attribute
	 */
	@Override
	default IRAttributeDefinition definition() {
		final IRAttributeRedefinition parentRedefinition = parentRedefinition();
		assert parentRedefinition != null : this; // IRAttributeDefinition and IRBrokkrTemplate override this method to return themselves
		return parentRedefinition.definition();
	}
	
	/**
	 * @return A complete list of all parameters, including inherited ones.
	 */
	List<IRParameterRedefinition> parameters();
	
	/**
	 * @return A complete list of all results, including inherited ones.
	 */
	List<IRResultRedefinition> results();
	
	/**
	 * @return A complete list of all possible errors, including inherited ones.
	 */
	List<IRError> errors();
	
	default @Nullable IRParameterRedefinition getParameterByName(final String name) {
		for (final IRParameterRedefinition p : parameters()) {
			if (p.name().equals(name))
				return p;
		}
		return null;
	}
	
	default @Nullable IRParameterRedefinition getParameterByPosition(final int index) {
		final List<IRParameterRedefinition> parameters = parameters();
		if (0 <= index && index < parameters.size())
			return parameters.get(index);
		return null;
	}
	
	default @Nullable IRResultRedefinition getResultByName(final String name) {
		for (final IRResultRedefinition r : results()) {
			if (r.name().equals(name))
				return r;
		}
		return null;
	}
	
	/**
	 * @return The single result called "result" of this attribute, or null if this attribute does not have exactly one result with that name.
	 */
	default @Nullable IRResultRedefinition getSingleResult() {
		final List<IRResultRedefinition> results = results();
		return results.size() == 1 && results.get(0).name().equals("result") ? results.get(0) : null;
	}
	
	default @Nullable IRError getErrorByName(final String name) {
		for (final IRError e : errors()) {
			if (e.name().equals(name))
				return e;
		}
		return null;
	}
	
	@Override
	default IRTypeUse mainResultType() {
		for (final IRResultRedefinition r : results()) {
			if (r.name().equals("result"))
				return r.type();
		}
		// no main result = void/empty tuple
		return IRTypeTuple.emptyTuple(getIRContext());
	}
	
	default IRTypeTuple allResultTypes() {
		final IRTypeTupleBuilder builder = new IRTypeTupleBuilder(getIRContext());
		for (final IRResultRedefinition result : results()) {
			builder.addEntry(result.name(), result.type());
		}
		return builder.build();
	}
	
	default IRTypeTuple allParameterTypes() {
		final IRTypeTupleBuilder builder = new IRTypeTupleBuilder(getIRContext());
		for (final IRParameterRedefinition parameter : parameters()) {
			builder.addEntry(parameter.name(), parameter.type());
		}
		return builder.build();
	}
	
	/**
	 * @return The modifiability of this attribute (re)definition, which may be inherited.
	 */
	boolean isModifying();
	
	/**
	 * @return Whether this attribute is variable, i.e. its values may vary per object (if false, the value may only vary per class, which is used for static methods and fields, as
	 *         well as "normal" methods).
	 */
	boolean isVariable();
	
	@SuppressWarnings("null")
	default InterpretedObject interpretDispatched(final InterpretedObject thisObject, final Map<IRParameterDefinition, InterpretedObject> arguments, final boolean allResults) throws InterpreterException {
		return thisObject.nativeClass().getAttributeImplementation(definition()).interpretImplementation(thisObject, arguments, allResults);
	}
	
	@Override
	default @NonNull IRMemberUse getUse(@Nullable final IRTypeUse targetType, final Map<IRGenericTypeDefinition, IRTypeUse> genericArguments) {
		final Map<IRParameterDefinition, IRTypeUse> argumentTypes = new HashMap<>();
		for (final IRParameterRedefinition p : parameters()) {
			if (p.type() instanceof IRGenericTypeUse) {
				IRTypeUse gt = genericArguments.get(((IRGenericTypeUse) p.type()).definition());
				if (gt == null)
					gt = new IRUnknownTypeUse(getIRContext());
				argumentTypes.put(p.definition(), gt);
			} else {
				argumentTypes.put(p.definition(), p.type());
			}
		}
		return new IRAttributeUse(this, targetType, argumentTypes);
	}
	
}

package ch.njol.brokkr.interpreter.definitions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.nativetypes.InterpretedTuple.InterpretedNativeTupleValueAndEntry;
import ch.njol.brokkr.interpreter.nativetypes.InterpretedTuple.InterpretedTypeTuple;
import ch.njol.brokkr.interpreter.uses.InterpretedTypeUse;
import ch.njol.brokkr.interpreter.InterpretedError;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterException;

/**
 * A (re)definition of an attribute,
 * which may either be the {@link InterpretedAttributeDefinition the first definition} or a redefinition.
 * A redefinition may be an {@link InterpretedAttributeImplementation implementation},
 * or may just change the signature (with e.g. more results or different {@link #modifiability() modifiability}).
 */
public interface InterpretedAttributeRedefinition extends InterpretedMemberRedefinition, InterpretedVariableOrAttributeRedefinition {
	
	/**
	 * @return The next (re)definiton of this attribute up the chain, where the last one is a {@link InterpretedAttributeDefinition}. If this is already a definition, this method
	 *         returns null.
	 */
	@Nullable
	InterpretedAttributeRedefinition parentRedefinition();
	
	/**
	 * @return The topmost definition of this attribute
	 */
	@Override
	@SuppressWarnings("null") // InterpretedAttributeDefinition overrides this to return itself
	default InterpretedAttributeDefinition definition() {
		return parentRedefinition().definition();
	}
	
	/**
	 * @return A complete list of all parameters, including inherited ones.
	 */
	List<InterpretedParameterRedefinition> parameters();
	
	/**
	 * @return A complete list of all results, including inherited ones.
	 */
	List<InterpretedResultRedefinition> results();
	
	/**
	 * @return A complete list of all possible errors, including inherited ones.
	 */
	List<InterpretedError> errors();
	
	default @Nullable InterpretedParameterRedefinition getParameterByName(final String name) {
		for (final InterpretedParameterRedefinition p : parameters()) {
			if (p.name().equals(name))
				return p;
		}
		return null;
	}
	
	default @Nullable InterpretedResultRedefinition getResultByName(final String name) {
		for (final InterpretedResultRedefinition r : results()) {
			if (r.name().equals(name))
				return r;
		}
		return null;
	}
	
	default @Nullable InterpretedError getErrorByName(final String name) {
		for (final InterpretedError e : errors()) {
			if (e.name().equals(name))
				return e;
		}
		return null;
	}
	
	@Override
	default InterpretedTypeUse mainResultType() {
		for (final InterpretedResultRedefinition r : results()) {
			if (r.name().equals("result"))
				return r.type();
		}
		throw new InterpreterException("missing main result");
	}
	
	default InterpretedTypeTuple allResultTypes() {
		final List<InterpretedResultRedefinition> results = results();
		final List<InterpretedNativeTupleValueAndEntry> entries = new ArrayList<>();
		for (int i = 0; i < results.size(); i++) {
			final InterpretedResultRedefinition result = results.get(i);
			final InterpretedTypeUse type = result.type();
			entries.add(new InterpretedNativeTupleValueAndEntry(i, type.nativeClass(), result.name(), type));
		}
		return new InterpretedTypeTuple(entries);
	}
	
	default InterpretedTypeTuple allParameterTypes() {
		final List<InterpretedParameterRedefinition> parameters = parameters();
		final List<InterpretedNativeTupleValueAndEntry> entries = new ArrayList<>();
		for (int i = 0; i < parameters.size(); i++) {
			final InterpretedParameterRedefinition parameter = parameters.get(i);
			final InterpretedTypeUse type = parameter.type();
			entries.add(new InterpretedNativeTupleValueAndEntry(i, type.nativeClass(), parameter.name(), type));
		}
		return new InterpretedTypeTuple(entries);
	}
	
	/**
	 * @return The modifiability of this attribute (re)definition, which may be inherited.
	 */
	boolean isModifying();
	
	/**
	 * @return Whether this attribute is variable, i.e. its values may vary per object (if false, the value may only vary per class, which is used for static methods and field, as
	 *         well as instance methods).
	 */
	boolean isVariable();
	
	@SuppressWarnings("null")
	default InterpretedObject interpretDispatched(final InterpretedObject thisObject, final Map<InterpretedParameterDefinition, InterpretedObject> arguments, final boolean allResults) {
		return thisObject.nativeClass().getAttributeImplementation(definition()).interpretImplementation(thisObject, arguments, allResults);
	}
	
}

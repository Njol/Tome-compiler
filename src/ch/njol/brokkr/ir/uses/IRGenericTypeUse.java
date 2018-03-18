package ch.njol.brokkr.ir.uses;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.InterpretedTypeUse;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.IRElement;
import ch.njol.brokkr.ir.definitions.IRGenericTypeDefinition;
import ch.njol.brokkr.ir.definitions.IRGenericTypeRedefinition;
import ch.njol.brokkr.ir.definitions.IRMemberRedefinition;
import ch.njol.brokkr.ir.definitions.IRSyntheticGenericTypeRedefinition;
import ch.njol.brokkr.ir.definitions.IRTypeDefinition;

/**
 * The use of a generic type, with or without known exact type
 */
public class IRGenericTypeUse extends AbstractIRTypeUse implements IRMemberUse {
	
	private final IRGenericTypeRedefinition redefinition;
	/**
	 * The known exact type of this generic type, or null if not known (TODO is that an error?)
	 */
	private final @Nullable IRTypeUse knownExactType;
	
	public IRGenericTypeUse(final IRGenericTypeRedefinition redefinition, final @Nullable IRTypeUse knownExactType) {
		if (knownExactType != null)
			IRElement.assertSameIRContext(redefinition, knownExactType);
//		if (knownExactType != null)
//			assert redefinition.upperBound().isSupertypeOfOrEqual(knownExactType); // TODO make this a semantic check
		this.redefinition = redefinition;
		this.knownExactType = knownExactType;
	}
	
	@Override
	public IRContext getIRContext() {
		return redefinition.getIRContext();
	}
	
	@Override
	public IRGenericTypeRedefinition redefinition() {
		return redefinition;
	}
	
	@Override
	public IRGenericTypeDefinition definition() {
		return redefinition.definition();
	}
	
	@Override
	public IRMemberRedefinition getRedefinitionFor(final IRTypeDefinition forType) {
		if (knownExactType != null)
			return new IRSyntheticGenericTypeRedefinition(redefinition, forType, knownExactType);
		return redefinition;
	}
	
//	@Override
//	public IRClassUse nativeClass() {
//		return (typeUse != null ? typeUse : redefinition.upperBound()).nativeClass();
//	}
	
	@Override
	public boolean equalsType(final IRTypeUse other) {
		return other instanceof IRGenericTypeUse && definition().equalsMember(((IRGenericTypeUse) other).definition());
	}
	
	@Override
	public int compareTo(final IRTypeUse other) {
		if (other instanceof IRGenericTypeUse) {
			return redefinition.definition().compareTo(((IRGenericTypeUse) other).redefinition.definition());
		}
		return IRTypeUse.compareTypeUseClasses(this.getClass(), other.getClass());
	}
	
	@Override
	public int typeHashCode() {
		return definition().memberHashCode();
	}
	
	@Override
	public boolean isSubtypeOfOrEqual(final IRTypeUse other) {
		// TODO also check generic types that have this type as bound
		return equalsType(other) || (knownExactType != null ? knownExactType : redefinition.upperBound()).isSubtypeOfOrEqual(other);
	}
	
	@Override
	public boolean isSupertypeOfOrEqual(final IRTypeUse other) {
		// TODO same as above
		return equalsType(other) || (knownExactType != null ? knownExactType : redefinition.upperBound()).isSupertypeOfOrEqual(other);
	}
	
	@Override
	public List<? extends IRMemberUse> members() {
		return (knownExactType != null ? knownExactType : redefinition.upperBound()).members();
	}
	
	@Override
	public Set<? extends IRTypeUse> allInterfaces() {
		return (knownExactType != null ? knownExactType : redefinition.upperBound()).allInterfaces();
	}
	
	@Override
	public String toString() {
		return knownExactType != null ? knownExactType.toString() : redefinition.name();
	}
	
	@Override
	public InterpretedTypeUse interpret(final InterpreterContext context) throws InterpreterException {
		throw new InterpreterException("not implemented"); // TODO
	}
	
}

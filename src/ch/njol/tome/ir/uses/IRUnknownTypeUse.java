package ch.njol.tome.ir.uses;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import ch.njol.tome.interpreter.InterpretedTypeUse;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.ir.IRContext;

/**
 * An unresolvable type (usually due to syntax or semantic errors in the code)
 */
public class IRUnknownTypeUse extends AbstractIRTypeUse {
	
	private final IRContext irContext;
	
	public IRUnknownTypeUse(final IRContext irContext) {
		this.irContext = irContext;
	}
	
	@Override
	public IRContext getIRContext() {
		return irContext;
	}
	
	@Override
	public boolean equalsType(final IRTypeUse other) {
		return false;
	}
	
	@Override
	public int compareTo(final IRTypeUse other) {
		if (other instanceof IRUnknownTypeUse) {
			return 0; // inconsistent with equals by choice - compareTo is only used for ordering types in and/or types
		}
		return IRTypeUse.compareTypeUseClasses(this.getClass(), other.getClass());
	}
	
	@Override
	public int typeHashCode() {
		return 0;
	}
	
	@Override
	public boolean isSubtypeOfOrEqual(final IRTypeUse other) {
		return false;
	}
	
	@Override
	public boolean isSupertypeOfOrEqual(final IRTypeUse other) {
		return false;
	}
	
	@Override
	public List<? extends IRMemberUse> members() {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public Set<? extends IRTypeUse> allInterfaces() {
		return Collections.singleton(irContext.getTypeUse("lang", "Any"));
	}
	
	@Override
	public String toString() {
		return "<unresolvable type>";
	}
	
	@Override
	public InterpretedTypeUse interpret(final InterpreterContext context) throws InterpreterException {
		throw new InterpreterException("unresolved type error");
	}
	
}

package ch.njol.brokkr.ir.uses;

import java.util.List;
import java.util.Set;

import ch.njol.brokkr.interpreter.InterpretedTypeUse;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.IRElement;

public class IRGenericTypeAccess extends AbstractIRTypeUse {
	
	private final IRTypeUse target;
	private final IRGenericTypeUse genericType;
	
	public IRGenericTypeAccess(final IRTypeUse target, final IRGenericTypeUse genericType) {
		IRElement.assertSameIRContext(target, genericType);
		this.target = target;
		this.genericType = genericType;
	}
	
	@Override
	public IRContext getIRContext() {
		return target.getIRContext();
	}
	
	@Override
	public int typeHashCode() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public boolean equalsType(final IRTypeUse other) {
		return compareTo(other) == 0;
	}
	
	@Override
	public int compareTo(final IRTypeUse other) {
		if (other instanceof IRGenericTypeAccess) {
			final IRGenericTypeAccess o = (IRGenericTypeAccess) other;
			final int c = target.compareTo(o.target);
			if (c != 0)
				return c;
			return genericType.compareTo(o.genericType);
		}
		return IRTypeUse.compareTypeUseClasses(this.getClass(), other.getClass());
	}
	
	@Override
	public boolean isSubtypeOfOrEqual(final IRTypeUse other) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isSupertypeOfOrEqual(final IRTypeUse other) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Set<? extends IRTypeUse> allInterfaces() {
		return genericType.allInterfaces();
	}
	
	@Override
	public List<? extends IRMemberUse> members() {
		return genericType.members();
	}
	
	@Override
	public InterpretedTypeUse interpret(final InterpreterContext context) throws InterpreterException {
		return target.interpret(context).getGenericType(genericType.definition());
	}
	
}

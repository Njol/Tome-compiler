package ch.njol.brokkr.ir.uses;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.InterpretedTypeUse;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.IRElement;
import ch.njol.brokkr.ir.definitions.IRMemberDefinition;

public class IROrTypeUse extends AbstractIRTypeUse {
	
	/**
	 * Canonical representation: t1 can be anything, t2 must not be an "or" type, and t1 < t2
	 */
	public final IRTypeUse t1, t2;
	
	private IROrTypeUse(final IRTypeUse t1, final IRTypeUse t2) {
		IRElement.assertSameIRContext(t1, t2);
		assert !(t2 instanceof IROrTypeUse);
		this.t1 = t1;
		this.t2 = t2;
	}
	
	public static IRTypeUse makeNew(final IRTypeUse t1, final IRTypeUse t2) {
		if (t2 instanceof IROrTypeUse)
			return makeNew(makeNew(t1, ((IROrTypeUse) t2).t1), ((IROrTypeUse) t2).t2);
		final int c = t1.compareTo(t2);
		if (c == 0)
			return t1;
		if (c < 0)
			return new IROrTypeUse(t1, t2);
		else
			return new IROrTypeUse(t2, t1);
	}
	
	public final static @Nullable IRMemberUse getMoreSpecificMemberUse(@Nullable final IRMemberUse m1, @Nullable final IRMemberUse m2) {
		if (m1 == null || m2 == null)
			return null;
		if (m1.definition().equalsMember(m2.definition())) {
			if (m1.redefinition().isRedefinitionOf(m2.redefinition()))
				return m1;
			if (m2.redefinition().isRedefinitionOf(m1.redefinition()))
				return m2;
		}
		return null;
	}
	
	@Override
	public IRContext getIRContext() {
		return t1.getIRContext();
	}
	
	@Override
	public @Nullable IRMemberUse getMemberByName(final String name) {
		return getMoreSpecificMemberUse(t1.getMemberByName(name), t2.getMemberByName(name));
	}
	
	@Override
	public @Nullable IRMemberUse getMember(final IRMemberDefinition definition) {
		return getMoreSpecificMemberUse(t1.getMember(definition), t2.getMember(definition));
	}
	
	@Override
	public List<? extends IRMemberUse> members() {
		final List<? extends IRMemberUse> ms1 = t1.members(), ms2 = t2.members();
		final @NonNull List<IRMemberUse> result = new ArrayList<>(ms1);
		outer: for (final IRMemberUse m2 : ms2) {
			for (final IRMemberUse m1 : ms1) {
				if (m1.definition().equalsMember(m2.definition())) {
					final IRMemberUse m = getMoreSpecificMemberUse(m1, m2);
					if (m != null)
						result.add(m);
					break outer;
				}
			}
			result.add(m2);
		}
		return result;
	}
	
	@Override
	public Set<? extends IRTypeUse> allInterfaces() {
		final Set<IRTypeUse> result = new HashSet<>();
		result.addAll(t1.allInterfaces());
		result.retainAll(t2.allInterfaces());
		return result;
	}
	
	@Override
	public boolean equalsType(final IRTypeUse other) {
		return other instanceof IROrTypeUse && t1.equalsType(((IROrTypeUse) other).t1) && t2.equalsType(((IROrTypeUse) other).t2);
	}
	
	@Override
	public int compareTo(final IRTypeUse other) {
		if (other instanceof IROrTypeUse) {
			final int c1 = t1.compareTo(((IROrTypeUse) other).t1);
			if (c1 != 0)
				return c1;
			return t2.compareTo(((IROrTypeUse) other).t2);
		}
		return IRTypeUse.compareTypeUseClasses(this.getClass(), other.getClass());
	}
	
	@Override
	public int typeHashCode() {
		return t1.hashCode() * 31 + t2.hashCode();
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
	public InterpretedTypeUse interpret(final InterpreterContext context) throws InterpreterException {
		throw new InterpreterException("not implemented"); // TODO
	}
	
	@Override
	public String toString() {
		return t1 + " | " + t2;
	}
	
}

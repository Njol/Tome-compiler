package ch.njol.brokkr.ir.uses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.InterpretedSimpleTypeUse;
import ch.njol.brokkr.interpreter.InterpretedTypeUse;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.IRElement;
import ch.njol.brokkr.ir.definitions.IRMemberDefinition;
import ch.njol.brokkr.ir.definitions.IRMemberRedefinition;

public class IRAndTypeUse extends AbstractIRTypeUse {
	
	/**
	 * Canonical representation: t1 must not be an "or" type, t2 must neither be an "and" nor "or" type, and t1 < t2 (unless t1 is an "and" type)
	 */
	private final IRTypeUse t1, t2;
	
	private IRAndTypeUse(final IRTypeUse t1, final IRTypeUse t2) {
		IRElement.assertSameIRContext(t1, t2);
		assert !(t1 instanceof IROrTypeUse);
		assert !(t2 instanceof IROrTypeUse) && !(t2 instanceof IRAndTypeUse);
		this.t1 = t1;
		this.t2 = t2;
	}
	
	// FIXME order left and right correctly! (also in "or")
	// TODO what if an interface is implemented multiple times, but with different generic arguments?
	// -> start with that being an error all the time, I can always allow it later on
	public static IRTypeUse makeNew(final IRTypeUse t1, final IRTypeUse t2) {
		if (t1 instanceof IROrTypeUse)
			return IROrTypeUse.makeNew(makeNew(((IROrTypeUse) t1).t1, t2), makeNew(((IROrTypeUse) t1).t2, t2));
		if (t2 instanceof IROrTypeUse)
			return IROrTypeUse.makeNew(makeNew(t1, ((IROrTypeUse) t2).t1), makeNew(t1, ((IROrTypeUse) t2).t2));
		if (t2 instanceof IRAndTypeUse)
			return makeNew(makeNew(t1, ((IRAndTypeUse) t2).t1), ((IRAndTypeUse) t2).t2);
		if (t1 instanceof IRAndTypeUse)
			return new IRAndTypeUse(t1, t2);
		final int c = t1.compareTo(t2);
		if (c == 0)
			return t1;
		if (c < 0)
			return new IRAndTypeUse(t1, t2);
		else
			return new IRAndTypeUse(t2, t1);
	}
	
	@Override
	public IRContext getIRContext() {
		return t1.getIRContext();
	}
	
	@Override
	public Set<? extends IRTypeUse> allInterfaces() {
		final Set<IRTypeUse> result = new HashSet<>();
		result.addAll(t1.allInterfaces());
		result.addAll(t2.allInterfaces());
		return result;
	}
	
	@Override
	public boolean equalsType(final IRTypeUse other) {
		return other instanceof IRAndTypeUse && t1.equalsType(((IRAndTypeUse) other).t1) && t2.equalsType(((IRAndTypeUse) other).t2);
	}
	
	@Override
	public int compareTo(final IRTypeUse other) {
		if (other instanceof IRAndTypeUse) {
			final int c1 = t1.compareTo(((IRAndTypeUse) other).t1);
			if (c1 != 0)
				return c1;
			return t2.compareTo(((IRAndTypeUse) other).t2);
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
	
	public final static @Nullable IRMemberUse getMoreSpecificMemberUse(@Nullable final IRMemberUse m1, @Nullable final IRMemberUse m2) {
		if (m1 == null)
			return m2;
		if (m2 == null)
			return m1;
		if (!m1.definition().equalsMember(m2.definition()))
			return null;
		if (m1.redefinition().isRedefinitionOf(m2.redefinition()))
			return m1;
		if (m2.redefinition().isRedefinitionOf(m1.redefinition()))
			return m2;
		return null;
	}
	
	// for a type like IF1 & IF2, conflicting definitions of a member may exist, so the most specific common super-member is used instead
	public final @Nullable IRMemberUse getMostSpecificCommonMemberUse(@Nullable final IRMemberUse m1, @Nullable final IRMemberUse m2) {
		if (m1 == null)
			return m2;
		if (m2 == null)
			return m1;
		if (!m1.definition().equalsMember(m2.definition()))
			return null;
		if (m1.redefinition().isRedefinitionOf(m2.redefinition()))
			return m1;
		if (m2.redefinition().isRedefinitionOf(m1.redefinition()))
			return m2;
		IRMemberRedefinition ms = m1.redefinition();
		while (ms != null && !m2.redefinition().isRedefinitionOf(ms))
			ms = ms.parentRedefinition();
		if (ms == null) {
			assert false : m1 + "; " + m2;
			return null;
		}
		return ms.getUse(this, Collections.EMPTY_MAP); // TODO use info from both uses? does that even work?
	}
	
	@Override
	public @Nullable IRMemberUse getMemberByName(final String name) {
		return getMostSpecificCommonMemberUse(t1.getMemberByName(name), t2.getMemberByName(name));
	}
	
	@Override
	public @Nullable IRMemberUse getMember(final IRMemberDefinition definition) {
		return getMostSpecificCommonMemberUse(t1.getMember(definition), t2.getMember(definition));
	}
	
	// does not work properly if one of the types is changed
//	private @Nullable List<? extends IRMemberUse> members;
	
	@Override
	public List<? extends IRMemberUse> members() {
//		if (members != null)
//			return members;
		final List<? extends IRMemberUse> ms1 = t1.members(), ms2 = t2.members();
		final @NonNull List<IRMemberUse> result = new ArrayList<>(ms1);
		outer: for (final IRMemberUse m2 : ms2) {
			for (final IRMemberUse m1 : ms1) {
				if (m1.definition().equalsMember(m2.definition())) {
					final IRMemberUse m = getMostSpecificCommonMemberUse(m1, m2);
					if (m != null) {
						result.remove(m1);
						result.add(m);
					}
					break outer;
				}
			}
			result.add(m2);
		}
//		members = result;
		return result;
	}
	
	@Override
	public InterpretedTypeUse interpret(final InterpreterContext context) throws InterpreterException {
		return new InterpretedSimpleTypeUse(this);
	}
	
	@Override
	public String toString() {
		return t1 + " & " + t2;
	}
	
}

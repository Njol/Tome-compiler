package ch.njol.brokkr.ir.uses;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ir.definitions.IRMemberDefinition;

public class IRAndTypeUse implements IRTypeUse {
	
	// TODO use an arbitrary amount of types?
	private final IRTypeUse t1, t2;
	
	public IRAndTypeUse(final IRTypeUse t1, final IRTypeUse t2) {
		this.t1 = t1;
		this.t2 = t2;
	}
	
	@Override
	public boolean equalsType(final IRTypeUse other) {
		// TODO Auto-generated method stub
		return false;
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
		if (m1.definition().equalsMember(m2.definition())) {
			if (m1.redefinition().isRedefinitionOf(m2.redefinition()))
				return m1;
			if (m2.redefinition().isRedefinitionOf(m1.redefinition()))
				return m2;
		}
		return null;
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
	
}

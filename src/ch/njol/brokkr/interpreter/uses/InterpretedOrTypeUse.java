package ch.njol.brokkr.interpreter.uses;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.interpreter.definitions.InterpretedMemberDefinition;

public class InterpretedOrTypeUse implements InterpretedTypeUse {
	
	// TODO use an arbitrary amount of types?
	private final InterpretedTypeUse t1, t2;
	
	public InterpretedOrTypeUse(final InterpretedTypeUse t1, final InterpretedTypeUse t2) {
		this.t1 = t1;
		this.t2 = t2;
	}
	
	public final static @Nullable InterpretedMemberUse getMoreSpecificMemberUse(@Nullable final InterpretedMemberUse m1, @Nullable final InterpretedMemberUse m2) {
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
	public @Nullable InterpretedMemberUse getMemberByName(final String name) {
		return getMoreSpecificMemberUse(t1.getMemberByName(name), t2.getMemberByName(name));
	}
	
	@Override
	public @Nullable InterpretedMemberUse getMember(final InterpretedMemberDefinition definition) {
		return getMoreSpecificMemberUse(t1.getMember(definition), t2.getMember(definition));
	}
	
	@Override
	public List<? extends InterpretedMemberUse> members() {
		final List<? extends InterpretedMemberUse> ms1 = t1.members(), ms2 = t2.members();
		final @NonNull List<InterpretedMemberUse> result = new ArrayList<>(ms1);
		outer: for (final InterpretedMemberUse m2 : ms2) {
			for (final InterpretedMemberUse m1 : ms1) {
				if (m1.definition().equalsMember(m2.definition())) {
					final InterpretedMemberUse m = getMoreSpecificMemberUse(m1, m2);
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
	public boolean equalsType(final InterpretedTypeUse other) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isSubtypeOfOrEqual(final InterpretedTypeUse other) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isSupertypeOfOrEqual(final InterpretedTypeUse other) {
		// TODO Auto-generated method stub
		return false;
	}
	
}

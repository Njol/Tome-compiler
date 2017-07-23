package ch.njol.brokkr.ir.definitions;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.SourceCodeLinkable;
import ch.njol.brokkr.ir.uses.IRMemberUse;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public interface IRMemberRedefinition extends SourceCodeLinkable {
	
	/**
	 * @return The name of this member, as of this (re)definition.
	 */
	String name();
	
	/**
	 * @return Whether this member is static or not, i.e. whether it is used like {@code Type.member} or {@code instance.member}.
	 */
	boolean isStatic();
	
	@Nullable
	IRMemberRedefinition parentRedefinition();
	
	default boolean isRedefinitionOf(final IRMemberRedefinition other) {
		IRMemberRedefinition r = this;
		do {
			if (r.equalsMember(other))
				return true;
			r = r.parentRedefinition();
		} while (r != null);
		return false;
	}
	
	IRMemberDefinition definition();
	
	boolean equalsMember(IRMemberRedefinition other);

	/**
	 * Gets the {@link IRMemberUse} for this member, if there is any, using the given type information.
	 * <p>
	 * If there is no member use for this member, the member will not be visible from any {@link IRTypeUse}s (useful for syntactic sugar like private inner classes).
	 * 
	 * @param targetType The type of the object this member is used on, or null for a static use.
	 * @param genericArguments TODO ???
	 * @return
	 */
	@Nullable IRMemberUse getUse(@Nullable IRTypeUse targetType, Map<IRGenericTypeDefinition, IRTypeUse> genericArguments);
	
}

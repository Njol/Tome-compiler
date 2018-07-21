package ch.njol.tome.ir.definitions;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.compiler.SourceCodeLinkable;
import ch.njol.tome.ir.IRDocumentedElement;
import ch.njol.tome.ir.uses.IRMemberUse;
import ch.njol.tome.ir.uses.IRTypeUse;

public interface IRMemberRedefinition extends SourceCodeLinkable, IRDocumentedElement {
	
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
	
	/**
	 * @return The type this member is declared in
	 */
	IRTypeDefinition declaringType();
	
	/**
	 * @param other
	 * @return Whether this member is the same as the given member, i.e. is declared in the same type and has the same name.
	 *         // TODO if there is a name conflict, what should this do? should one or both members get a synthetic name?
	 */
	default boolean equalsMember(final IRMemberRedefinition other) {
		return declaringType().equalsType(other.declaringType()) && name().equals(other.name());
	}
	
	default int memberHashCode() {
		final IRTypeDefinition declaringType = declaringType();
		return declaringType.typeHashCode() * 31 + name().hashCode();
	}
	
	/**
	 * Gets the {@link IRMemberUse} for this member, if there is any, using the given type information.
	 * <p>
	 * If there is no member use for this member, the member will not be visible from any {@link IRTypeUse}s (useful for syntactic sugar like private inner classes).
	 * 
	 * @param targetType The type of the object this member is used on, or null for a static use.
	 * @param genericArguments TODO ???
	 * @return
	 */
	@Nullable
	IRMemberUse getUse(@Nullable IRTypeUse targetType, Map<IRAttributeDefinition, IRTypeUse> genericArguments);
	
}

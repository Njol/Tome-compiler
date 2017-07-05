package ch.njol.brokkr.interpreter.uses;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.SourceCodeLinkable;
import ch.njol.brokkr.compiler.ast.ElementPart;
import ch.njol.brokkr.interpreter.definitions.InterpretedGenericTypeDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedMemberDefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedMemberRedefinition;
import ch.njol.brokkr.interpreter.definitions.InterpretedNativeTypeDefinition;

/**
 * A type object for "normal" types, i.e. types without special handling (like tuples and "and/or" types).
 */
public class InterpretedSimpleTypeUse implements InterpretedTypeUse, SourceCodeLinkable {
	
	private final InterpretedNativeTypeDefinition type;
	private final Map<InterpretedGenericTypeDefinition, InterpretedTypeUse> genericArguments = new HashMap<>();
	
	/**
	 * Creates a simple type object without generic type information
	 * 
	 * @param type
	 */
	public InterpretedSimpleTypeUse(final InterpretedNativeTypeDefinition type) {
		this.type = type;
	}
	
	public InterpretedSimpleTypeUse(final InterpretedNativeTypeDefinition base, final Map<InterpretedGenericTypeDefinition, InterpretedTypeUse> genericArguments) {
		type = base;
		this.genericArguments.putAll(genericArguments);
	}
	
	public InterpretedNativeTypeDefinition getBase() {
		return type;
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
	
	@Override
	public @Nullable ElementPart getLinked() {
		return type instanceof SourceCodeLinkable ? ((SourceCodeLinkable) type).getLinked() : null;
	}
	
	@Override
	public @Nullable InterpretedMemberUse getMemberByName(final String name) {
		final InterpretedMemberRedefinition memberRedefinition = type.getMemberByName(name);
		if (memberRedefinition == null)
			return null;
		return memberRedefinition.getUse(this, genericArguments);
	}
	
	@Override
	public @Nullable InterpretedMemberUse getMember(final InterpretedMemberDefinition definition) {
		final InterpretedMemberRedefinition memberRedefinition = type.getMember(definition);
		if (memberRedefinition == null)
			return null;
		return memberRedefinition.getUse(this, genericArguments);
	}
	
	@Override
	public List<InterpretedMemberUse> members() {
		return type.members().stream().map(m -> m.getUse(this, genericArguments)).collect(Collectors.toList());
	}
	
}

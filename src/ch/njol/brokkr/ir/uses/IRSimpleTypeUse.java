package ch.njol.brokkr.ir.uses;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.SourceCodeLinkable;
import ch.njol.brokkr.ast.ASTElementPart;
import ch.njol.brokkr.ir.definitions.IRGenericTypeDefinition;
import ch.njol.brokkr.ir.definitions.IRMemberDefinition;
import ch.njol.brokkr.ir.definitions.IRMemberRedefinition;
import ch.njol.brokkr.ir.definitions.IRTypeDefinition;

/**
 * A type object for "normal" types, i.e. types without special handling (like tuples and "and/or" types).
 */
public class IRSimpleTypeUse implements IRTypeUse, SourceCodeLinkable {
	
	private final IRTypeDefinition type;
	private final Map<IRGenericTypeDefinition, IRTypeUse> genericArguments = new HashMap<>();
	
	/**
	 * Creates a simple type object without generic type information
	 * 
	 * @param type
	 */
	public IRSimpleTypeUse(final IRTypeDefinition type) {
		this.type = type;
	}
	
	public IRSimpleTypeUse(final IRTypeDefinition base, final Map<IRGenericTypeDefinition, IRTypeUse> genericArguments) {
		type = base;
		this.genericArguments.putAll(genericArguments);
	}
	
	public IRTypeDefinition getBase() {
		return type;
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
	
	@Override
	public @Nullable ASTElementPart getLinked() {
		return type instanceof SourceCodeLinkable ? ((SourceCodeLinkable) type).getLinked() : null;
	}
	
	@Override
	public @Nullable IRMemberUse getMemberByName(final String name) {
		final IRMemberRedefinition memberRedefinition = type.getMemberByName(name);
		if (memberRedefinition == null)
			return null;
		return memberRedefinition.getUse(this, genericArguments);
	}
	
	@Override
	public @Nullable IRMemberUse getMember(final IRMemberDefinition definition) {
		final IRMemberRedefinition memberRedefinition = type.getMember(definition);
		if (memberRedefinition == null)
			return null;
		return memberRedefinition.getUse(this, genericArguments);
	}
	
	@SuppressWarnings("null")
	@Override
	public List<IRMemberUse> members() {
		return type.members().stream().map(m -> m.getUse(this, genericArguments)).filter(m -> m != null).collect(Collectors.toList());
	}
	
	@Override
	public String toString() {
		return type + (genericArguments.isEmpty() ? "" : "<" + String.join(", ", genericArguments.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.toList())) + ">");
	}
	
}

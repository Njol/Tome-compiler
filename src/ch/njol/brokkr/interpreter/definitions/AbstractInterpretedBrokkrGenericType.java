package ch.njol.brokkr.interpreter.definitions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.compiler.ast.ElementPart;
import ch.njol.brokkr.compiler.ast.Members.GenericTypeDeclaration;
import ch.njol.brokkr.compiler.ast.Members.MemberModifiers;

public abstract class AbstractInterpretedBrokkrGenericType implements InterpretedGenericTypeRedefinition {
	
	private final GenericTypeDeclaration declaration;
	
	public AbstractInterpretedBrokkrGenericType(final GenericTypeDeclaration declaration) {
		this.declaration = declaration;
	}
	
	@Override
	public String name() {
		return "" + declaration.name;
	}
	
	@Override
	public @Nullable ElementPart getLinked() {
		return declaration;
	}
	
	@Override
	public @Nullable InterpretedGenericTypeRedefinition parentRedefinition() {
		final MemberModifiers modifiers = declaration.modifiers;
		if (modifiers == null)
			return null;
		return (InterpretedGenericTypeRedefinition) modifiers.overridden.get();
	}
	
	@Override
	public boolean equalsMember(final InterpretedMemberRedefinition other) {
		return this.getClass() == other.getClass() && declaration == ((AbstractInterpretedBrokkrGenericType) other).declaration; // TODO correct?
	}
	
}

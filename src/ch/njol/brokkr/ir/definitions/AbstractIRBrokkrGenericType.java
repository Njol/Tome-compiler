package ch.njol.brokkr.ir.definitions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTElementPart;
import ch.njol.brokkr.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.brokkr.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.brokkr.ast.ASTMembers.ASTGenericTypeDeclaration;
import ch.njol.brokkr.ast.ASTMembers.ASTMemberModifiers;
import ch.njol.brokkr.ir.AbstractIRElement;
import ch.njol.brokkr.ir.IRContext;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public abstract class AbstractIRBrokkrGenericType extends AbstractIRElement implements IRGenericTypeRedefinition {
	
	private final ASTGenericTypeDeclaration ast;
	
	public AbstractIRBrokkrGenericType(final ASTGenericTypeDeclaration ast) {
		this.ast = ast;
	}
	
	@Override
	public String name() {
		return "" + ast.name;
	}
	
	@Override
	public IRContext getIRContext() {
		return ast.getIRContext();
	}
	
	@Override
	public String toString() {
		return ast.getParentOfType(ASTTypeDeclaration.class) + "." + name();
	}
	
	@Override
	public IRTypeDefinition declaringType() {
		final ASTTypeDeclaration type = ast.getParentOfType(ASTTypeDeclaration.class);
		if (type == null)
			return new IRUnknownTypeDefinition(getIRContext(), "Internal compiler error (Generic type definition not in type: " + this + ")", ast);
		return type.getIR();
	}
	
	@Override
	public @Nullable ASTElementPart getLinked() {
		return ast;
	}
	
	@Override
	public @Nullable IRGenericTypeRedefinition parentRedefinition() {
		final ASTMemberModifiers modifiers = ast.modifiers;
		if (modifiers == null)
			return null;
		return (IRGenericTypeRedefinition) modifiers.overridden.get();
	}
	
	@Override
	public IRTypeUse upperBound() {
		final ASTTypeUse extendedType = ast.extendedType;
		if (extendedType != null)
			return extendedType.getIRType();
		return getIRContext().getTypeUse("lang", "Any");
	}
	
	@Override
	public int hashCode() {
		return memberHashCode();
	}
	
	@Override
	public boolean equals(@Nullable final Object other) {
		return other instanceof IRMemberRedefinition && equalsMember((IRMemberRedefinition) other);
	}
	
}

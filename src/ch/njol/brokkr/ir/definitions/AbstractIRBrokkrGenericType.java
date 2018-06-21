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
import ch.njol.brokkr.util.ASTCommentUtil;

public abstract class AbstractIRBrokkrGenericType extends AbstractIRElement implements IRGenericTypeRedefinition {
	
	private final ASTGenericTypeDeclaration ast;
	private final IRTypeDefinition declaringType;
	
	public AbstractIRBrokkrGenericType(final ASTGenericTypeDeclaration ast) {
		this.ast = registerDependency(ast);
		final ASTTypeDeclaration type = ast.getParentOfType(ASTTypeDeclaration.class);
		declaringType = registerDependency(type != null ? type.getIR() : new IRUnknownTypeDefinition(getIRContext(), "Internal compiler error (Generic type definition not in type: " + this + ")", ast));
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
		return declaringType;
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
	
	/**
	 * @return The upper bound of this generic type, either as declared or {@code Any} if not.
	 */
	@Override
	public IRTypeUse upperBound() {
		final ASTTypeUse extendedType = ast.extendedType;
		if (extendedType != null)
			return extendedType.getIR();
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
	
	@Override
	public String documentation() {
		return declaringType + "." + ast.name() + "\n" + ASTCommentUtil.getCommentBefore(ast);
	}
	
}

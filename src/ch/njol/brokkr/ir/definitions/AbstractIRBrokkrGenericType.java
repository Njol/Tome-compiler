package ch.njol.brokkr.ir.definitions;

import java.util.Collections;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTElementPart;
import ch.njol.brokkr.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.brokkr.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.brokkr.ast.ASTMembers.ASTGenericTypeDeclaration;
import ch.njol.brokkr.ast.ASTMembers.ASTMemberModifiers;
import ch.njol.brokkr.ast.ASTTopLevelElements.ASTBrokkrFile;
import ch.njol.brokkr.interpreter.InterpreterException;
import ch.njol.brokkr.ir.uses.IRTypeUse;

public abstract class AbstractIRBrokkrGenericType implements IRGenericTypeRedefinition {
	
	private final ASTGenericTypeDeclaration declaration;
	
	public AbstractIRBrokkrGenericType(final ASTGenericTypeDeclaration declaration) {
		this.declaration = declaration;
	}
	
	@Override
	public String name() {
		return "" + declaration.name;
	}
	
	@Override
	public String toString() {
		return declaration.getParentOfType(ASTTypeDeclaration.class) + "." + name();
	}
	
	@Override
	public @Nullable IRTypeDefinition declaringType() {
		final ASTTypeDeclaration type = declaration.getParentOfType(ASTTypeDeclaration.class);
		if (type == null)
			throw new InterpreterException("Generic type definition not in type: " + this);
		return type.getIR();
	}
	
	@Override
	public @Nullable ASTElementPart getLinked() {
		return declaration;
	}
	
	@Override
	public @Nullable IRGenericTypeRedefinition parentRedefinition() {
		final ASTMemberModifiers modifiers = declaration.modifiers;
		if (modifiers == null)
			return null;
		return (IRGenericTypeRedefinition) modifiers.overridden.get();
	}
	
	@SuppressWarnings("null")
	@Override
	public IRTypeUse upperBound() {
		final ASTTypeUse extendedType = declaration.extendedType;
		if (extendedType != null)
			return extendedType.getIRType();
		return ASTBrokkrFile.getModule(declaration).modules.getType("lang", "Any").getUse(Collections.EMPTY_MAP);
	}
	
	@Override
	public int hashCode() {
		return memberHashCode();
	}
	
	@Override
	public boolean equals(@Nullable final Object other) {
		return other instanceof IRMemberRedefinition ? equalsMember((IRMemberRedefinition) other) : false;
	}
	
}

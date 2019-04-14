package ch.njol.tome.ir.definitions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.toplevel.ASTGenericParameterDeclaration;
import ch.njol.tome.ir.AbstractIRElement;
import ch.njol.tome.ir.IRContext;

public abstract class AbstractIRBrokkrGenericParameter extends AbstractIRElement implements IRGenericParameter {
	
	protected abstract ASTGenericParameterDeclaration<?> ast();
	
	@Override
	public IRContext getIRContext() {
		return ast().getIRContext();
	}
	
	@Override
	public IRTypeDefinition declaringType() {
		final ASTTypeDeclaration<?> astTypeDeclaration = ast().getParentOfType(ASTTypeDeclaration.class);
		return astTypeDeclaration == null ? new IRUnknownTypeDefinition(getIRContext(), "<internal compiler error>", ast()) : astTypeDeclaration.getIR();
	}
	
	@Override
	public String name() {
		return ast().name();
	}
	
	@Override
	public boolean equals(@Nullable final Object obj) {
		return obj instanceof IRGenericTypeParameter && equalsGenericParameter((IRGenericTypeParameter) obj);
	}
	
	@Override
	public int hashCode() {
		return genericParameterHashCode();
	}
	
}

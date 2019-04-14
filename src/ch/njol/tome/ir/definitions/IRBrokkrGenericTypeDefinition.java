package ch.njol.tome.ir.definitions;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTElementPart;
import ch.njol.tome.ast.ASTInterfaces.ASTTypeDeclaration;
import ch.njol.tome.ast.members.ASTGenericTypeDeclaration;
import ch.njol.tome.ir.AbstractIRElement;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.uses.IRMemberUse;
import ch.njol.tome.ir.uses.IRTypeUse;

public class IRBrokkrGenericTypeDefinition extends AbstractIRElement implements IRGenericTypeDefinition {
	
	private final ASTGenericTypeDeclaration ast;
	
	public IRBrokkrGenericTypeDefinition(final ASTGenericTypeDeclaration ast) {
		this.ast = ast;
	}
	
	@Override
	public String name() {
		return "" + ast.nameToken;
	}
	
	@Override
	public String toString() {
		return name();
	}
	
	@Override
	public boolean isStatic() {
		return true;
	}
	
	@Override
	public IRTypeDefinition declaringType() {
		ASTTypeDeclaration<?> type = ast.getParentOfType(ASTTypeDeclaration.class);
		return type != null ? type.getIR() : new IRUnknownTypeDefinition(getIRContext(), "<internal compiler error>", ast);
	}
	
	@Override
	public @Nullable IRMemberUse getUse(@Nullable final IRTypeUse targetType, final Map<IRAttributeDefinition, IRTypeUse> genericArguments) {
		return null; // TODO?
	}
	
	@Override
	public @Nullable ASTElementPart getLinked() {
		return ast;
	}
	
	@Override
	public String documentation() {
		return "";
	}
	
	@Override
	public IRContext getIRContext() {
		return ast.getIRContext();
	}
	
	@Override
	public @Nullable IRTypeUse extends_() {
		return ast.extends_ != null ? ast.extends_.getIR() : null;
	}
	
	@Override
	public @Nullable IRTypeUse super_() {
		return ast.super_ != null ? ast.super_.getIR() : null;
	}
	
}

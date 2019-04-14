package ch.njol.tome.ir;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTTypeUse;
import ch.njol.tome.ir.expressions.IRExpression;

public class IRTypeBoundGenericArgument extends AbstractIRElement implements IRGenericArgument {
	
	private final @Nullable IRExpression extends_;
	private final @Nullable IRExpression super_;
	private final IRContext irContext;
	
	public IRTypeBoundGenericArgument(@Nullable final IRExpression extends_, @Nullable final IRExpression super_, final IRContext irContext) {
		this.extends_ = extends_;
		this.super_ = super_;
		this.irContext = irContext;
	}
	
	public @Nullable IRExpression getExtends() {
		return extends_;
	}
	
	public @Nullable IRExpression getSuper() {
		return super_;
	}
	
	@Override
	public IRContext getIRContext() {
		return irContext;
	}
	
	public static IRTypeBoundGenericArgument fromBounds(@Nullable final ASTTypeUse<?> extends_, @Nullable final ASTTypeUse<?> super_, final IRContext irContext) {
		return new IRTypeBoundGenericArgument(extends_ == null ? null : extends_.getIR(), super_ == null ? null : super_.getIR(), irContext);
	}
	
}

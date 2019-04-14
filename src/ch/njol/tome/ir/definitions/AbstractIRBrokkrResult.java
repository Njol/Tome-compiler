package ch.njol.tome.ir.definitions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.members.ASTNormalResult;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.expressions.IRExpression;

public abstract class AbstractIRBrokkrResult extends AbstractIRBrokkrVariable implements IRResultRedefinition {
	
	private final IRAttributeRedefinition attribute;
	
	public AbstractIRBrokkrResult(final ASTNormalResult ast, final IRAttributeRedefinition attribute) {
		super(ast);
		this.attribute = registerDependency(attribute);
	}
	
	@Override
	public IRContext getIRContext() {
		return ast.getIRContext();
	}
	
	@Override
	public IRAttributeRedefinition attribute() {
		return attribute;
	}
	
	@Override
	public @Nullable IRExpression defaultValue() {
		final ASTExpression<?> defaultValue = ((ASTNormalResult) ast).defaultValue;
		return defaultValue == null ? null : defaultValue.getIR();
	}
	
}

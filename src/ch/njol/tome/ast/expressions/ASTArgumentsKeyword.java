package ch.njol.tome.ast.expressions;

import ch.njol.tome.ast.ASTInterfaces.ASTAttribute;
import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ir.expressions.IRArgumentsKeyword;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRUnknownExpression;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;

/**
 * The keyword 'arguments', representing a tuple of all arguments to the current method.
 */
public class ASTArgumentsKeyword extends AbstractASTElementWithIR<IRExpression> implements ASTExpression<IRExpression> {
	
	@Override
	public String toString() {
		return "arguments";
	}
	
	public static ASTArgumentsKeyword parse(final Parser parent) {
		return parent.one(p -> {
			final ASTArgumentsKeyword ast = new ASTArgumentsKeyword();
			p.one("arguments");
			return ast;
		});
	}
	
	@Override
	public IRTypeUse getIRType() {
		final ASTAttribute attribute = getParentOfType(ASTAttribute.class);
		return attribute == null ? new IRUnknownTypeUse(getIRContext()) : attribute.getIR().allParameterTypes();
	}
	
	@Override
	protected IRExpression calculateIR() {
		final ASTAttribute attribute = getParentOfType(ASTAttribute.class);
		if (attribute == null)
			return new IRUnknownExpression("Internal compiler error", this);
		return new IRArgumentsKeyword(attribute.getIR());
	}
	
}

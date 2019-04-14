package ch.njol.tome.ast.statements;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.AbstractASTElementWithIR;
import ch.njol.tome.ast.statements.ASTStatements.ASTStatement;
import ch.njol.tome.ir.statements.IRExpressionStatement;
import ch.njol.tome.parser.Parser;

public class ASTExpressionStatement extends AbstractASTElementWithIR<IRExpressionStatement> implements ASTStatement<IRExpressionStatement> {
	
	public final ASTExpression<?> expression;
	
	public ASTExpressionStatement(final ASTExpression<?> expression) {
		this.expression = expression;
	}
	
	@Override
	public String toString() {
		return expression + ";";
	}
	
	public static ASTExpressionStatement finishParsing(final Parser p, final ASTExpression<?> expression) {
		final ASTExpressionStatement ast = new ASTExpressionStatement(expression);
		p.one(';');
//			expectedFatal("';' to complete statement"); // TODO better error message
		return p.done(ast);
	}
	
	@Override
	protected IRExpressionStatement calculateIR() {
		return new IRExpressionStatement(expression.getIR());
	}
	
}

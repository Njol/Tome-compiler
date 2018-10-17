package ch.njol.tome.ast.expressions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.compiler.Token.SymbolToken;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRIf;
import ch.njol.tome.ir.expressions.IRUnknownExpression;
import ch.njol.tome.ir.uses.IROrTypeUse;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.ir.uses.IRUnknownTypeUse;
import ch.njol.tome.parser.Parser;

public class ASTTernaryIf extends AbstractASTElement implements ASTExpression {
	public ASTExpression condition;
	public @Nullable ASTExpression then, otherwise;
	
	public ASTTernaryIf(final ASTExpression condition, final SymbolToken questionMark) {
		this.condition = condition;
	}
	
	@Override
	public String toString() {
		return condition + " ? " + then + " : " + otherwise;
	}
	
	public static ASTTernaryIf finishParsing(final Parser p, final ASTExpression condition, final SymbolToken questionMark) {
		final ASTTernaryIf ast = new ASTTernaryIf(condition, questionMark);
		ast.then = ASTExpressions.parse(p);
		p.one(':');
		ast.otherwise = ASTExpressions.parse(p);
		return p.done(ast);
	}
	
	@Override
	public IRTypeUse getIRType() {
		return IROrTypeUse.makeNew(then != null ? then.getIRType() : new IRUnknownTypeUse(getIRContext()), otherwise != null ? otherwise.getIRType() : new IRUnknownTypeUse(getIRContext()));
	}
	
	@Override
	public IRExpression getIR() {
		return new IRIf(condition.getIR(), then != null ? then.getIR() : new IRUnknownExpression("Syntax error. Correct syntax: [test ? then : otherwise]", this), otherwise != null ? otherwise.getIR() : null);
	}
}
